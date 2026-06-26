"""Enrollments API - Multi-step enrollment workflow."""
import uuid
import math
from datetime import datetime, date, timezone, timedelta
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func

from app.database import get_db
from app.models.enrollment import Enrollment, EnrollmentStatus
from app.models.farmer import Farmer
from app.models.goat import Goat, GoatStatus
from app.models.policy import Policy, PolicyStatus
from app.models.premium import Premium, PremiumStatus
from app.models.audit_log import AuditLog
from app.schemas.enrollment import (
    EnrollmentCreate, EnrollmentStepUpdate, EnrollmentResponse,
    EnrollmentListResponse, EnrollmentFinalizeRequest
)
from app.schemas.common import ResponseModel, PaginatedResponse, PaginationParams
from app.core.security import get_current_user, require_roles
from app.core.exceptions import NotFoundException, BadRequestException
from app.api.deps import get_pagination

router = APIRouter(prefix="/enrollments", tags=["Enrollments"])


def _generate_enrollment_number() -> str:
    return f"ENR-{datetime.now().strftime('%Y%m%d')}-{uuid.uuid4().hex[:6].upper()}"


def _generate_policy_number() -> str:
    return f"POL-{datetime.now().strftime('%Y%m%d')}-{uuid.uuid4().hex[:8].upper()}"


@router.post("/", response_model=ResponseModel)
async def create_enrollment(
    data: EnrollmentCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Start a new enrollment process."""
    enrollment = Enrollment(
        enrollment_number=_generate_enrollment_number(),
        current_step=1,
        status=EnrollmentStatus.DRAFT,
        is_synced=True,
        offline_id=data.offline_id,
        created_by=current_user["id"],
    )
    db.add(enrollment)
    await db.flush()

    db.add(AuditLog(
        user_id=current_user["id"], action="CREATE",
        entity_type="enrollment", entity_id=enrollment.id,
        description=f"Started enrollment: {enrollment.enrollment_number}"
    ))

    return ResponseModel(
        message="Enrollment started",
        data=EnrollmentResponse.model_validate(enrollment).model_dump()
    )


@router.put("/{enrollment_id}/step", response_model=ResponseModel)
async def update_enrollment_step(
    enrollment_id: int,
    step_data: EnrollmentStepUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Update a specific step of the enrollment."""
    result = await db.execute(select(Enrollment).where(Enrollment.id == enrollment_id))
    enrollment = result.scalar_one_or_none()
    if not enrollment:
        raise NotFoundException("Enrollment", str(enrollment_id))

    if enrollment.status == EnrollmentStatus.COMPLETED:
        raise BadRequestException("Enrollment already completed")

    if step_data.step < 1 or step_data.step > 7:
        raise BadRequestException("Step must be between 1 and 7")

    # Store step data
    step_field = f"step_{step_data.step}_data"
    setattr(enrollment, step_field, step_data.data)

    # Update current step and status
    status_map = {
        1: EnrollmentStatus.STEP_1_FARMER,
        2: EnrollmentStatus.STEP_2_GOAT,
        3: EnrollmentStatus.STEP_3_PHOTOS,
        4: EnrollmentStatus.STEP_4_QR_TAG,
        5: EnrollmentStatus.STEP_5_VACCINATION,
        6: EnrollmentStatus.STEP_6_PREMIUM,
        7: EnrollmentStatus.STEP_7_POLICY,
    }

    if step_data.step >= enrollment.current_step:
        enrollment.current_step = step_data.step + 1 if step_data.step < 7 else 7
        enrollment.status = status_map.get(step_data.step, enrollment.status)

    # Step 1: Create/update farmer
    if step_data.step == 1 and not enrollment.farmer_id:
        farmer = Farmer(
            name=step_data.data.get("name", ""),
            phone=step_data.data.get("phone", ""),
            village=step_data.data.get("village", ""),
            full_address=step_data.data.get("address"),
            aadhaar_number=step_data.data.get("aadhaar_number"),
            latitude=step_data.data.get("latitude"),
            longitude=step_data.data.get("longitude"),
            photo_url=step_data.data.get("photo_url"),
            registered_by=current_user["id"],
        )
        db.add(farmer)
        await db.flush()
        enrollment.farmer_id = farmer.id

    # Step 2: Create goat record
    if step_data.step == 2 and enrollment.farmer_id and not enrollment.goat_id:
        goat = Goat(
            farmer_id=enrollment.farmer_id,
            breed=step_data.data.get("breed", ""),
            age_months=step_data.data.get("age_months"),
            gender=step_data.data.get("gender", "male"),
            weight_kg=step_data.data.get("weight_kg"),
            color=step_data.data.get("color"),
            identification_marks=step_data.data.get("identification_marks"),
            registered_by=current_user["id"],
        )
        db.add(goat)
        await db.flush()
        enrollment.goat_id = goat.id

    # Step 3: Update goat photos
    if step_data.step == 3 and enrollment.goat_id:
        goat_result = await db.execute(select(Goat).where(Goat.id == enrollment.goat_id))
        goat = goat_result.scalar_one_or_none()
        if goat:
            goat.photo_left = step_data.data.get("photo_left")
            goat.photo_right = step_data.data.get("photo_right")
            goat.photo_front = step_data.data.get("photo_front")
            goat.photo_back = step_data.data.get("photo_back")
            goat.photo_top = step_data.data.get("photo_top")
            goat.photo_face = step_data.data.get("photo_face")

    # Step 4: QR tag assignment
    if step_data.step == 4 and enrollment.goat_id:
        goat_result = await db.execute(select(Goat).where(Goat.id == enrollment.goat_id))
        goat = goat_result.scalar_one_or_none()
        if goat:
            goat.qr_code = step_data.data.get("qr_code")
            goat.ear_tag_number = step_data.data.get("ear_tag_number")
            goat.tag_number = step_data.data.get("tag_number")

    return ResponseModel(
        message=f"Step {step_data.step} saved successfully",
        data=EnrollmentResponse.model_validate(enrollment).model_dump()
    )


@router.post("/{enrollment_id}/finalize", response_model=ResponseModel)
async def finalize_enrollment(
    enrollment_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Finalize enrollment and generate policy."""
    result = await db.execute(select(Enrollment).where(Enrollment.id == enrollment_id))
    enrollment = result.scalar_one_or_none()
    if not enrollment:
        raise NotFoundException("Enrollment", str(enrollment_id))

    if not enrollment.farmer_id or not enrollment.goat_id:
        raise BadRequestException("Enrollment is incomplete — farmer and goat are required")

    # Create policy
    policy_number = _generate_policy_number()
    today = date.today()
    policy = Policy(
        policy_number=policy_number,
        enrollment_id=enrollment.id,
        farmer_id=enrollment.farmer_id,
        goat_id=enrollment.goat_id,
        start_date=today,
        end_date=today + timedelta(days=365),
        sum_insured=enrollment.step_6_data.get("sum_insured", 5000.0) if enrollment.step_6_data else 5000.0,
        premium_amount=enrollment.step_6_data.get("premium_amount", 100.0) if enrollment.step_6_data else 100.0,
        status=PolicyStatus.ACTIVE,
        issued_by=current_user["id"],
    )
    db.add(policy)
    await db.flush()

    # Create premium record if payment data exists
    if enrollment.step_6_data:
        premium = Premium(
            policy_id=policy.id,
            farmer_id=enrollment.farmer_id,
            enrollment_id=enrollment.id,
            amount=enrollment.step_6_data.get("premium_amount", 100.0),
            payment_mode=enrollment.step_6_data.get("payment_mode", "cash"),
            status=PremiumStatus.PAID,
            receipt_number=f"RCP-{uuid.uuid4().hex[:8].upper()}",
            collected_by=current_user["id"],
        )
        db.add(premium)
        await db.flush()
        enrollment.premium_id = premium.id

    # Update enrollment status
    enrollment.policy_id = policy.id
    enrollment.status = EnrollmentStatus.COMPLETED
    enrollment.completed_at = datetime.now(timezone.utc)

    # Update goat status
    goat_result = await db.execute(select(Goat).where(Goat.id == enrollment.goat_id))
    goat = goat_result.scalar_one_or_none()
    if goat:
        goat.status = GoatStatus.INSURED
        goat.insured_value = policy.sum_insured

    db.add(AuditLog(
        user_id=current_user["id"], action="FINALIZE",
        entity_type="enrollment", entity_id=enrollment.id,
        description=f"Enrollment {enrollment.enrollment_number} completed. Policy: {policy_number}"
    ))

    return ResponseModel(
        message="Enrollment finalized, policy generated",
        data={
            "enrollment": EnrollmentResponse.model_validate(enrollment).model_dump(),
            "policy_number": policy_number,
            "policy_id": policy.id,
        }
    )


@router.get("/", response_model=ResponseModel)
async def list_enrollments(
    pagination: PaginationParams = Depends(get_pagination),
    status: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """List enrollments with pagination."""
    query = select(Enrollment)
    if status:
        query = query.where(Enrollment.status == status)

    count_query = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_query)).scalar() or 0

    sort_column = getattr(Enrollment, pagination.sort_by, Enrollment.created_at)
    if pagination.sort_order == "desc":
        query = query.order_by(sort_column.desc())
    else:
        query = query.order_by(sort_column.asc())

    offset = (pagination.page - 1) * pagination.page_size
    query = query.offset(offset).limit(pagination.page_size)

    result = await db.execute(query)
    enrollments = result.scalars().all()
    total_pages = math.ceil(total / pagination.page_size) if total > 0 else 1

    return ResponseModel(
        data=PaginatedResponse(
            items=[EnrollmentListResponse.model_validate(e).model_dump() for e in enrollments],
            total=total, page=pagination.page, page_size=pagination.page_size,
            total_pages=total_pages,
            has_next=pagination.page < total_pages,
            has_previous=pagination.page > 1,
        ).model_dump()
    )


@router.get("/{enrollment_id}", response_model=ResponseModel)
async def get_enrollment(
    enrollment_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get enrollment details by ID."""
    result = await db.execute(select(Enrollment).where(Enrollment.id == enrollment_id))
    enrollment = result.scalar_one_or_none()
    if not enrollment:
        raise NotFoundException("Enrollment", str(enrollment_id))

    return ResponseModel(
        data=EnrollmentResponse.model_validate(enrollment).model_dump()
    )
