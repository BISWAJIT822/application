"""Admin API - Administrative management endpoints."""
import math
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, update

from app.database import get_db
from app.models.user import User, UserRole
from app.models.audit_log import AuditLog
from app.models.farmer import Farmer
from app.models.goat import Goat
from app.models.policy import Policy
from app.models.claim import Claim
from app.schemas.user import UserResponse, UserCreate
from app.schemas.common import ResponseModel, PaginatedResponse, PaginationParams
from app.core.security import require_roles
from app.core.exceptions import NotFoundException
from app.api.deps import get_pagination

router = APIRouter(prefix="/admin", tags=["Admin"])


@router.get("/users", response_model=ResponseModel)
async def list_users(
    pagination: PaginationParams = Depends(get_pagination),
    role: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """List all users (admin only)."""
    query = select(User)
    if role:
        query = query.where(User.role == role)

    if pagination.search:
        from sqlalchemy import or_
        search_term = f"%{pagination.search}%"
        query = query.where(or_(
            User.name.ilike(search_term),
            User.phone.ilike(search_term),
        ))

    count_query = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_query)).scalar() or 0

    sort_column = getattr(User, pagination.sort_by, User.created_at)
    if pagination.sort_order == "desc":
        query = query.order_by(sort_column.desc())
    else:
        query = query.order_by(sort_column.asc())

    offset = (pagination.page - 1) * pagination.page_size
    query = query.offset(offset).limit(pagination.page_size)

    result = await db.execute(query)
    users = result.scalars().all()
    total_pages = math.ceil(total / pagination.page_size) if total > 0 else 1

    return ResponseModel(
        data=PaginatedResponse(
            items=[UserResponse.model_validate(u).model_dump() for u in users],
            total=total, page=pagination.page, page_size=pagination.page_size,
            total_pages=total_pages,
            has_next=pagination.page < total_pages,
            has_previous=pagination.page > 1,
        ).model_dump()
    )


@router.put("/users/{user_id}/role", response_model=ResponseModel)
async def change_user_role(
    user_id: int,
    role: str,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """Change a user's role."""
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user:
        raise NotFoundException("User", str(user_id))

    old_role = user.role
    user.role = UserRole(role)

    db.add(AuditLog(
        user_id=current_user["id"], action="CHANGE_ROLE",
        entity_type="user", entity_id=user.id,
        old_values={"role": old_role.value if isinstance(old_role, UserRole) else old_role},
        new_values={"role": role},
        description=f"Changed role for {user.name}: {old_role} → {role}"
    ))

    return ResponseModel(message=f"Role changed to {role}")


@router.put("/users/{user_id}/toggle-active", response_model=ResponseModel)
async def toggle_user_active(
    user_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """Activate or deactivate a user."""
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user:
        raise NotFoundException("User", str(user_id))

    user.is_active = not user.is_active

    db.add(AuditLog(
        user_id=current_user["id"], action="TOGGLE_ACTIVE",
        entity_type="user", entity_id=user.id,
        description=f"{'Activated' if user.is_active else 'Deactivated'} user: {user.name}"
    ))

    return ResponseModel(
        message=f"User {'activated' if user.is_active else 'deactivated'}"
    )


@router.get("/analytics", response_model=ResponseModel)
async def get_analytics(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """Get comprehensive analytics for admin dashboard."""
    # Users by role
    role_counts = {}
    for role in UserRole:
        count = (await db.execute(
            select(func.count()).select_from(User).where(User.role == role)
        )).scalar() or 0
        role_counts[role.value] = count

    # Monthly enrollment trend (last 6 months)
    from datetime import datetime, timedelta
    monthly_enrollments = []
    from app.models.enrollment import Enrollment
    for i in range(5, -1, -1):
        month_start = (datetime.now().replace(day=1) - timedelta(days=30 * i)).replace(day=1)
        if i > 0:
            month_end = (datetime.now().replace(day=1) - timedelta(days=30 * (i - 1))).replace(day=1)
        else:
            month_end = datetime.now()
        count = (await db.execute(
            select(func.count()).select_from(Enrollment).where(
                Enrollment.created_at >= month_start,
                Enrollment.created_at < month_end,
            )
        )).scalar() or 0
        monthly_enrollments.append({
            "month": month_start.strftime("%b %Y"),
            "count": count,
        })

    return ResponseModel(
        data={
            "users_by_role": role_counts,
            "monthly_enrollments": monthly_enrollments,
        }
    )


@router.get("/audit-logs", response_model=ResponseModel)
async def list_audit_logs(
    pagination: PaginationParams = Depends(get_pagination),
    action: Optional[str] = Query(None),
    entity_type: Optional[str] = Query(None),
    user_id: Optional[int] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin"]))
):
    """List audit logs with filtering."""
    query = select(AuditLog)

    if action:
        query = query.where(AuditLog.action == action)
    if entity_type:
        query = query.where(AuditLog.entity_type == entity_type)
    if user_id:
        query = query.where(AuditLog.user_id == user_id)

    count_query = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_query)).scalar() or 0

    query = query.order_by(AuditLog.created_at.desc())
    offset = (pagination.page - 1) * pagination.page_size
    query = query.offset(offset).limit(pagination.page_size)

    result = await db.execute(query)
    logs = result.scalars().all()
    total_pages = math.ceil(total / pagination.page_size) if total > 0 else 1

    return ResponseModel(
        data=PaginatedResponse(
            items=[{
                "id": log.id,
                "user_id": log.user_id,
                "action": log.action,
                "entity_type": log.entity_type,
                "entity_id": log.entity_id,
                "description": log.description,
                "created_at": log.created_at.isoformat() if log.created_at else None,
            } for log in logs],
            total=total, page=pagination.page, page_size=pagination.page_size,
            total_pages=total_pages,
            has_next=pagination.page < total_pages,
            has_previous=pagination.page > 1,
        ).model_dump()
    )
