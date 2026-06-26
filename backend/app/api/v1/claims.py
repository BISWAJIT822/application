"""Claims API - Insurance claim filing, review, and settlement."""
import uuid
import math
from datetime import datetime, timezone
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func

from app.database import get_db
from app.models.claim import Claim, ClaimStatus
from app.models.goat import Goat, GoatStatus
from app.models.policy import Policy
from app.models.audit_log import AuditLog
from app.schemas.claim import (
    ClaimCreate, ClaimUpdate, ClaimReview, ClaimVerification,
    ClaimSettlement, ClaimResponse, ClaimListResponse
)
from app.schemas.common import ResponseModel, PaginatedResponse, PaginationParams
from app.core.security import get_current_user, require_roles
from app.core.exceptions import NotFoundException, BadRequestException
from app.api.deps import get_pagination

router = APIRouter(prefix="/claims", tags=["Claims"])


@router.post("/", response_model=ResponseModel)
async def file_claim(
    data: ClaimCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """File a new insurance claim (death notification)."""
    # Verify policy exists and is active
    policy_result = await db.execute(select(Policy).where(Policy.id == data.policy_id))
    policy = policy_result.scalar_one_or_none()
    if not policy:
        raise NotFoundException("Policy", str(data.policy_id))
    if policy.status.value != "active":
        raise BadRequestException("Policy is not active")

    claim_number = f"CLM-{datetime.now().strftime('%Y%m%d')}-{uuid.uuid4().hex[:6].upper()}"

    claim = Claim(
        claim_number=claim_number,
        policy_id=data.policy_id,
        goat_id=data.goat_id,
        farmer_id=data.farmer_id,
        death_date=data.death_date,
        death_cause=data.death_cause,
        death_description=data.death_description,
        death_location=data.death_location,
        death_photos=data.death_photos,
        carcass_photos=data.carcass_photos,
        latitude=data.latitude,
        longitude=data.longitude,
        claim_amount=data.claim_amount or policy.sum_insured,
        status=ClaimStatus.SUBMITTED,
        filed_by=current_user["id"],
    )
    db.add(claim)
    await db.flush()

    # Update goat status
    goat_result = await db.execute(select(Goat).where(Goat.id == data.goat_id))
    goat = goat_result.scalar_one_or_none()
    if goat:
        goat.status = GoatStatus.CLAIMED

    db.add(AuditLog(
        user_id=current_user["id"], action="FILE_CLAIM",
        entity_type="claim", entity_id=claim.id,
        description=f"Filed claim {claim_number} for policy {policy.policy_number}"
    ))

    return ResponseModel(
        message="Claim filed successfully",
        data=ClaimResponse.model_validate(claim).model_dump()
    )


@router.get("/", response_model=ResponseModel)
async def list_claims(
    pagination: PaginationParams = Depends(get_pagination),
    status: Optional[str] = Query(None),
    farmer_id: Optional[int] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """List claims with filtering and pagination."""
    query = select(Claim)

    if status:
        query = query.where(Claim.status == status)
    if farmer_id:
        query = query.where(Claim.farmer_id == farmer_id)

    count_query = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_query)).scalar() or 0

    sort_column = getattr(Claim, pagination.sort_by, Claim.created_at)
    if pagination.sort_order == "desc":
        query = query.order_by(sort_column.desc())
    else:
        query = query.order_by(sort_column.asc())

    offset = (pagination.page - 1) * pagination.page_size
    query = query.offset(offset).limit(pagination.page_size)

    result = await db.execute(query)
    claims = result.scalars().all()
    total_pages = math.ceil(total / pagination.page_size) if total > 0 else 1

    return ResponseModel(
        data=PaginatedResponse(
            items=[ClaimListResponse.model_validate(c).model_dump() for c in claims],
            total=total, page=pagination.page, page_size=pagination.page_size,
            total_pages=total_pages,
            has_next=pagination.page < total_pages,
            has_previous=pagination.page > 1,
        ).model_dump()
    )


@router.get("/{claim_id}", response_model=ResponseModel)
async def get_claim(
    claim_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get claim details."""
    result = await db.execute(select(Claim).where(Claim.id == claim_id))
    claim = result.scalar_one_or_none()
    if not claim:
        raise NotFoundException("Claim", str(claim_id))

    return ResponseModel(data=ClaimResponse.model_validate(claim).model_dump())


@router.put("/{claim_id}/verify", response_model=ResponseModel)
async def verify_claim(
    claim_id: int,
    data: ClaimVerification,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator"]))
):
    """Verify carcass for a claim."""
    result = await db.execute(select(Claim).where(Claim.id == claim_id))
    claim = result.scalar_one_or_none()
    if not claim:
        raise NotFoundException("Claim", str(claim_id))

    claim.carcass_verified = data.carcass_verified
    claim.verification_date = datetime.now(timezone.utc)
    claim.verified_by = current_user["id"]
    claim.verification_remarks = data.verification_remarks
    if data.latitude:
        claim.latitude = data.latitude
    if data.longitude:
        claim.longitude = data.longitude
    claim.status = ClaimStatus.VERIFICATION

    # Simple AI assessment simulation
    claim.ai_assessment_score = 0.85 if data.carcass_verified else 0.3
    claim.ai_assessment_remarks = (
        "Carcass verified. Claim appears legitimate based on evidence."
        if data.carcass_verified
        else "Carcass not verified. Manual review recommended."
    )

    db.add(AuditLog(
        user_id=current_user["id"], action="VERIFY",
        entity_type="claim", entity_id=claim.id,
        description=f"Carcass verification for claim {claim.claim_number}: {'Verified' if data.carcass_verified else 'Not verified'}"
    ))

    return ResponseModel(
        message="Claim verification updated",
        data=ClaimResponse.model_validate(claim).model_dump()
    )


@router.put("/{claim_id}/review", response_model=ResponseModel)
async def review_claim(
    claim_id: int,
    data: ClaimReview,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator"]))
):
    """Review a claim: approve, reject, or put on hold."""
    result = await db.execute(select(Claim).where(Claim.id == claim_id))
    claim = result.scalar_one_or_none()
    if not claim:
        raise NotFoundException("Claim", str(claim_id))

    status_map = {
        "approved": ClaimStatus.APPROVED,
        "rejected": ClaimStatus.REJECTED,
        "on_hold": ClaimStatus.ON_HOLD,
    }

    claim.status = status_map[data.status]
    claim.reviewed_by = current_user["id"]
    claim.review_date = datetime.now(timezone.utc)
    claim.review_remarks = data.review_remarks
    if data.approved_amount:
        claim.approved_amount = data.approved_amount

    # If rejected, update goat status back
    if data.status == "rejected":
        goat_result = await db.execute(select(Goat).where(Goat.id == claim.goat_id))
        goat = goat_result.scalar_one_or_none()
        if goat:
            goat.status = GoatStatus.DECEASED

    db.add(AuditLog(
        user_id=current_user["id"], action=f"REVIEW_{data.status.upper()}",
        entity_type="claim", entity_id=claim.id,
        description=f"Claim {claim.claim_number} {data.status}"
    ))

    return ResponseModel(
        message=f"Claim {data.status}",
        data=ClaimResponse.model_validate(claim).model_dump()
    )


@router.put("/{claim_id}/settle", response_model=ResponseModel)
async def settle_claim(
    claim_id: int,
    data: ClaimSettlement,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """Settle an approved claim."""
    result = await db.execute(select(Claim).where(Claim.id == claim_id))
    claim = result.scalar_one_or_none()
    if not claim:
        raise NotFoundException("Claim", str(claim_id))

    if claim.status != ClaimStatus.APPROVED:
        raise BadRequestException("Only approved claims can be settled")

    claim.approved_amount = data.approved_amount
    claim.payment_mode = data.payment_mode
    claim.settlement_reference = data.settlement_reference
    claim.settlement_date = datetime.now(timezone.utc)
    claim.status = ClaimStatus.SETTLED

    # Update policy status
    policy_result = await db.execute(select(Policy).where(Policy.id == claim.policy_id))
    policy = policy_result.scalar_one_or_none()
    if policy:
        from app.models.policy import PolicyStatus
        policy.status = PolicyStatus.CLAIMED

    db.add(AuditLog(
        user_id=current_user["id"], action="SETTLE",
        entity_type="claim", entity_id=claim.id,
        description=f"Claim {claim.claim_number} settled: ₹{data.approved_amount}"
    ))

    return ResponseModel(
        message="Claim settled successfully",
        data=ClaimResponse.model_validate(claim).model_dump()
    )
