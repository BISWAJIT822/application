"""Vaccinations API - Vaccination records and scheduling."""
import math
from typing import Optional
from datetime import date
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, or_

from app.database import get_db
from app.models.vaccination import Vaccination, VaccinationStatus
from app.models.goat import Goat
from app.models.audit_log import AuditLog
from app.schemas.vaccination import (
    VaccinationCreate, VaccinationUpdate, VaccinationResponse, VaccinationBulkCreate
)
from app.schemas.common import ResponseModel, PaginatedResponse, PaginationParams
from app.core.security import get_current_user, require_roles
from app.core.exceptions import NotFoundException
from app.api.deps import get_pagination

router = APIRouter(prefix="/vaccinations", tags=["Vaccinations"])


@router.post("/", response_model=ResponseModel)
async def record_vaccination(
    data: VaccinationCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Record a vaccination."""
    vaccination = Vaccination(
        **data.model_dump(exclude_unset=True),
        administered_by=current_user["id"],
    )
    db.add(vaccination)
    await db.flush()

    # Update goat vaccination dates
    goat_result = await db.execute(select(Goat).where(Goat.id == data.goat_id))
    goat = goat_result.scalar_one_or_none()
    if goat and data.vaccination_date:
        from datetime import datetime
        goat.last_vaccination_date = datetime.combine(data.vaccination_date, datetime.min.time())
        if data.next_due_date:
            goat.next_vaccination_date = datetime.combine(data.next_due_date, datetime.min.time())

    db.add(AuditLog(
        user_id=current_user["id"], action="CREATE",
        entity_type="vaccination", entity_id=vaccination.id,
        description=f"Recorded {data.vaccine_type} vaccination for goat {data.goat_id}"
    ))

    return ResponseModel(
        message="Vaccination recorded",
        data=VaccinationResponse.model_validate(vaccination).model_dump()
    )


@router.post("/bulk", response_model=ResponseModel)
async def record_bulk_vaccination(
    data: VaccinationBulkCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Record multiple vaccinations at once (enrollment Step 5)."""
    results = []
    for vac_data in data.vaccinations:
        vaccination = Vaccination(
            goat_id=data.goat_id,
            farmer_id=data.farmer_id,
            vaccine_type=vac_data.vaccine_type,
            vaccine_name=vac_data.vaccine_name,
            vaccination_date=vac_data.vaccination_date,
            next_due_date=vac_data.next_due_date,
            status=vac_data.status or "completed",
            administered_by=current_user["id"],
        )
        db.add(vaccination)
        await db.flush()
        results.append(VaccinationResponse.model_validate(vaccination).model_dump())

    return ResponseModel(
        message=f"{len(results)} vaccinations recorded",
        data=results
    )


@router.get("/", response_model=ResponseModel)
async def list_vaccinations(
    pagination: PaginationParams = Depends(get_pagination),
    goat_id: Optional[int] = Query(None),
    farmer_id: Optional[int] = Query(None),
    status: Optional[str] = Query(None),
    vaccine_type: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """List vaccinations with filtering."""
    query = select(Vaccination)

    if goat_id:
        query = query.where(Vaccination.goat_id == goat_id)
    if farmer_id:
        query = query.where(Vaccination.farmer_id == farmer_id)
    if status:
        query = query.where(Vaccination.status == status)
    if vaccine_type:
        query = query.where(Vaccination.vaccine_type == vaccine_type)

    count_query = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_query)).scalar() or 0

    sort_column = getattr(Vaccination, pagination.sort_by, Vaccination.created_at)
    if pagination.sort_order == "desc":
        query = query.order_by(sort_column.desc())
    else:
        query = query.order_by(sort_column.asc())

    offset = (pagination.page - 1) * pagination.page_size
    query = query.offset(offset).limit(pagination.page_size)

    result = await db.execute(query)
    vaccinations = result.scalars().all()
    total_pages = math.ceil(total / pagination.page_size) if total > 0 else 1

    return ResponseModel(
        data=PaginatedResponse(
            items=[VaccinationResponse.model_validate(v).model_dump() for v in vaccinations],
            total=total, page=pagination.page, page_size=pagination.page_size,
            total_pages=total_pages,
            has_next=pagination.page < total_pages,
            has_previous=pagination.page > 1,
        ).model_dump()
    )


@router.get("/upcoming", response_model=ResponseModel)
async def get_upcoming_vaccinations(
    days: int = Query(30, ge=1, le=365),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get upcoming vaccinations due within specified days."""
    from datetime import timedelta
    today = date.today()
    end_date = today + timedelta(days=days)

    query = select(Vaccination).where(
        Vaccination.next_due_date >= today,
        Vaccination.next_due_date <= end_date,
        Vaccination.status.in_(["scheduled", "overdue"])
    ).order_by(Vaccination.next_due_date.asc())

    result = await db.execute(query)
    vaccinations = result.scalars().all()

    return ResponseModel(
        data=[VaccinationResponse.model_validate(v).model_dump() for v in vaccinations]
    )


@router.put("/{vaccination_id}", response_model=ResponseModel)
async def update_vaccination(
    vaccination_id: int,
    data: VaccinationUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator", "suraksha_didi"]))
):
    """Update a vaccination record."""
    result = await db.execute(select(Vaccination).where(Vaccination.id == vaccination_id))
    vaccination = result.scalar_one_or_none()
    if not vaccination:
        raise NotFoundException("Vaccination", str(vaccination_id))

    update_data = data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(vaccination, field, value)

    return ResponseModel(
        message="Vaccination updated",
        data=VaccinationResponse.model_validate(vaccination).model_dump()
    )


@router.get("/history/{goat_id}", response_model=ResponseModel)
async def get_vaccination_history(
    goat_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get complete vaccination history for a goat."""
    query = select(Vaccination).where(
        Vaccination.goat_id == goat_id
    ).order_by(Vaccination.vaccination_date.desc())

    result = await db.execute(query)
    vaccinations = result.scalars().all()

    return ResponseModel(
        data=[VaccinationResponse.model_validate(v).model_dump() for v in vaccinations]
    )
