"""Farmers API - CRUD operations for farmer management."""
import math
from typing import Optional
from fastapi import APIRouter, Depends, Query, UploadFile, File
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, or_

from app.database import get_db
from app.models.farmer import Farmer
from app.models.audit_log import AuditLog
from app.schemas.farmer import FarmerCreate, FarmerUpdate, FarmerResponse, FarmerListResponse
from app.schemas.common import ResponseModel, PaginatedResponse
from app.core.security import get_current_user, require_roles
from app.core.exceptions import NotFoundException, ForbiddenException
from app.api.deps import get_pagination
from app.schemas.common import PaginationParams

router = APIRouter(prefix="/farmers", tags=["Farmers"])


@router.post("/", response_model=ResponseModel)
async def create_farmer(
    farmer_data: FarmerCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Create a new farmer record."""
    farmer = Farmer(
        **farmer_data.model_dump(exclude_unset=True),
        registered_by=current_user["id"]
    )
    db.add(farmer)
    await db.flush()

    # Audit
    db.add(AuditLog(
        user_id=current_user["id"], action="CREATE",
        entity_type="farmer", entity_id=farmer.id,
        description=f"Created farmer: {farmer.name}"
    ))

    return ResponseModel(
        message="Farmer created successfully",
        data=FarmerResponse.model_validate(farmer).model_dump()
    )


@router.get("/", response_model=ResponseModel)
async def list_farmers(
    pagination: PaginationParams = Depends(get_pagination),
    status: Optional[str] = Query(None),
    village: Optional[str] = Query(None),
    district: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """List farmers with pagination, search, and filtering."""
    query = select(Farmer)

    # Filters
    if status == "active":
        query = query.where(Farmer.is_active == True)
    elif status == "inactive":
        query = query.where(Farmer.is_active == False)
    if village:
        query = query.where(Farmer.village.ilike(f"%{village}%"))
    if district:
        query = query.where(Farmer.district.ilike(f"%{district}%"))

    # Search
    if pagination.search:
        search_term = f"%{pagination.search}%"
        query = query.where(
            or_(
                Farmer.name.ilike(search_term),
                Farmer.phone.ilike(search_term),
                Farmer.village.ilike(search_term),
                Farmer.aadhaar_number.ilike(search_term),
            )
        )

    # Count
    count_query = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_query)).scalar() or 0

    # Sort
    sort_column = getattr(Farmer, pagination.sort_by, Farmer.created_at)
    if pagination.sort_order == "desc":
        query = query.order_by(sort_column.desc())
    else:
        query = query.order_by(sort_column.asc())

    # Paginate
    offset = (pagination.page - 1) * pagination.page_size
    query = query.offset(offset).limit(pagination.page_size)

    result = await db.execute(query)
    farmers = result.scalars().all()

    total_pages = math.ceil(total / pagination.page_size) if total > 0 else 1

    return ResponseModel(
        data=PaginatedResponse(
            items=[FarmerListResponse.model_validate(f).model_dump() for f in farmers],
            total=total,
            page=pagination.page,
            page_size=pagination.page_size,
            total_pages=total_pages,
            has_next=pagination.page < total_pages,
            has_previous=pagination.page > 1,
        ).model_dump()
    )


@router.get("/{farmer_id}", response_model=ResponseModel)
async def get_farmer(
    farmer_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get farmer details by ID."""
    result = await db.execute(select(Farmer).where(Farmer.id == farmer_id))
    farmer = result.scalar_one_or_none()
    if not farmer:
        raise NotFoundException("Farmer", str(farmer_id))

    return ResponseModel(
        data=FarmerResponse.model_validate(farmer).model_dump()
    )


@router.put("/{farmer_id}", response_model=ResponseModel)
async def update_farmer(
    farmer_id: int,
    farmer_data: FarmerUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Update farmer details."""
    result = await db.execute(select(Farmer).where(Farmer.id == farmer_id))
    farmer = result.scalar_one_or_none()
    if not farmer:
        raise NotFoundException("Farmer", str(farmer_id))

    update_data = farmer_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(farmer, field, value)

    db.add(AuditLog(
        user_id=current_user["id"], action="UPDATE",
        entity_type="farmer", entity_id=farmer.id,
        new_values=update_data,
        description=f"Updated farmer: {farmer.name}"
    ))

    return ResponseModel(
        message="Farmer updated successfully",
        data=FarmerResponse.model_validate(farmer).model_dump()
    )


@router.delete("/{farmer_id}", response_model=ResponseModel)
async def delete_farmer(
    farmer_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """Soft-delete a farmer (admin only)."""
    result = await db.execute(select(Farmer).where(Farmer.id == farmer_id))
    farmer = result.scalar_one_or_none()
    if not farmer:
        raise NotFoundException("Farmer", str(farmer_id))

    farmer.is_active = False

    db.add(AuditLog(
        user_id=current_user["id"], action="DELETE",
        entity_type="farmer", entity_id=farmer.id,
        description=f"Soft-deleted farmer: {farmer.name}"
    ))

    return ResponseModel(message="Farmer deleted successfully")
