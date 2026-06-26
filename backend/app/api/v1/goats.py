"""Goats API - CRUD operations for goat management."""
import math
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, or_

from app.database import get_db
from app.models.goat import Goat, GoatStatus
from app.models.audit_log import AuditLog
from app.schemas.goat import GoatCreate, GoatUpdate, GoatResponse, GoatListResponse, GoatPhotoUpload, QRTagAssignment
from app.schemas.common import ResponseModel, PaginatedResponse, PaginationParams
from app.core.security import get_current_user, require_roles
from app.core.exceptions import NotFoundException, ConflictException
from app.api.deps import get_pagination

router = APIRouter(prefix="/goats", tags=["Goats"])


@router.post("/", response_model=ResponseModel)
async def create_goat(
    goat_data: GoatCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Register a new goat."""
    goat = Goat(
        **goat_data.model_dump(exclude_unset=True),
        registered_by=current_user["id"]
    )
    db.add(goat)
    await db.flush()

    db.add(AuditLog(
        user_id=current_user["id"], action="CREATE",
        entity_type="goat", entity_id=goat.id,
        description=f"Registered goat: {goat.breed} for farmer {goat.farmer_id}"
    ))

    return ResponseModel(
        message="Goat registered successfully",
        data=GoatResponse.model_validate(goat).model_dump()
    )


@router.get("/", response_model=ResponseModel)
async def list_goats(
    pagination: PaginationParams = Depends(get_pagination),
    farmer_id: Optional[int] = Query(None),
    status: Optional[str] = Query(None),
    breed: Optional[str] = Query(None),
    gender: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """List goats with pagination, search, and filtering."""
    query = select(Goat)

    if farmer_id:
        query = query.where(Goat.farmer_id == farmer_id)
    if status:
        query = query.where(Goat.status == status)
    if breed:
        query = query.where(Goat.breed.ilike(f"%{breed}%"))
    if gender:
        query = query.where(Goat.gender == gender)

    if pagination.search:
        search_term = f"%{pagination.search}%"
        query = query.where(
            or_(
                Goat.tag_number.ilike(search_term),
                Goat.breed.ilike(search_term),
                Goat.color.ilike(search_term),
                Goat.ear_tag_number.ilike(search_term),
            )
        )

    count_query = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_query)).scalar() or 0

    sort_column = getattr(Goat, pagination.sort_by, Goat.created_at)
    if pagination.sort_order == "desc":
        query = query.order_by(sort_column.desc())
    else:
        query = query.order_by(sort_column.asc())

    offset = (pagination.page - 1) * pagination.page_size
    query = query.offset(offset).limit(pagination.page_size)

    result = await db.execute(query)
    goats = result.scalars().all()
    total_pages = math.ceil(total / pagination.page_size) if total > 0 else 1

    return ResponseModel(
        data=PaginatedResponse(
            items=[GoatListResponse.model_validate(g).model_dump() for g in goats],
            total=total, page=pagination.page, page_size=pagination.page_size,
            total_pages=total_pages,
            has_next=pagination.page < total_pages,
            has_previous=pagination.page > 1,
        ).model_dump()
    )


@router.get("/{goat_id}", response_model=ResponseModel)
async def get_goat(
    goat_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get goat details by ID."""
    result = await db.execute(select(Goat).where(Goat.id == goat_id))
    goat = result.scalar_one_or_none()
    if not goat:
        raise NotFoundException("Goat", str(goat_id))

    return ResponseModel(data=GoatResponse.model_validate(goat).model_dump())


@router.put("/{goat_id}", response_model=ResponseModel)
async def update_goat(
    goat_id: int,
    goat_data: GoatUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Update goat details."""
    result = await db.execute(select(Goat).where(Goat.id == goat_id))
    goat = result.scalar_one_or_none()
    if not goat:
        raise NotFoundException("Goat", str(goat_id))

    update_data = goat_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(goat, field, value)

    db.add(AuditLog(
        user_id=current_user["id"], action="UPDATE",
        entity_type="goat", entity_id=goat.id,
        new_values=update_data,
        description=f"Updated goat: {goat.id}"
    ))

    return ResponseModel(
        message="Goat updated successfully",
        data=GoatResponse.model_validate(goat).model_dump()
    )


@router.put("/{goat_id}/photos", response_model=ResponseModel)
async def update_goat_photos(
    goat_id: int,
    photos: GoatPhotoUpload,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Update goat photos (6 angles)."""
    result = await db.execute(select(Goat).where(Goat.id == goat_id))
    goat = result.scalar_one_or_none()
    if not goat:
        raise NotFoundException("Goat", str(goat_id))

    photo_data = photos.model_dump(exclude_unset=True)
    for field, value in photo_data.items():
        if value is not None:
            setattr(goat, field, value)

    return ResponseModel(message="Photos updated successfully")


@router.post("/assign-qr", response_model=ResponseModel)
async def assign_qr_tag(
    assignment: QRTagAssignment,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Assign QR code and ear tag to a goat."""
    # Check if QR already assigned
    existing = await db.execute(
        select(Goat).where(Goat.qr_code == assignment.qr_code)
    )
    if existing.scalar_one_or_none():
        raise ConflictException("QR code already assigned to another goat")

    result = await db.execute(select(Goat).where(Goat.id == assignment.goat_id))
    goat = result.scalar_one_or_none()
    if not goat:
        raise NotFoundException("Goat", str(assignment.goat_id))

    goat.qr_code = assignment.qr_code
    if assignment.ear_tag_number:
        goat.ear_tag_number = assignment.ear_tag_number

    db.add(AuditLog(
        user_id=current_user["id"], action="QR_ASSIGN",
        entity_type="goat", entity_id=goat.id,
        description=f"Assigned QR {assignment.qr_code} to goat {goat.id}"
    ))

    return ResponseModel(message="QR tag assigned successfully")


@router.delete("/{goat_id}", response_model=ResponseModel)
async def delete_goat(
    goat_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """Soft-delete a goat (mark as removed)."""
    result = await db.execute(select(Goat).where(Goat.id == goat_id))
    goat = result.scalar_one_or_none()
    if not goat:
        raise NotFoundException("Goat", str(goat_id))

    goat.status = GoatStatus.REMOVED

    db.add(AuditLog(
        user_id=current_user["id"], action="DELETE",
        entity_type="goat", entity_id=goat.id,
        description=f"Removed goat: {goat.id}"
    ))

    return ResponseModel(message="Goat removed successfully")
