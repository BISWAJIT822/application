"""Notifications API - Push notification management."""
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, update

from app.database import get_db
from app.models.notification import Notification
from app.schemas.notification import (
    NotificationCreate, NotificationResponse, MarkReadRequest, SendPushRequest
)
from app.schemas.common import ResponseModel
from app.core.security import get_current_user, require_roles

router = APIRouter(prefix="/notifications", tags=["Notifications"])


@router.get("/", response_model=ResponseModel)
async def list_notifications(
    unread_only: bool = Query(False),
    limit: int = Query(50, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get notifications for current user."""
    query = select(Notification).where(
        Notification.user_id == current_user["id"]
    ).order_by(Notification.created_at.desc()).limit(limit)

    if unread_only:
        query = query.where(Notification.is_read == False)

    result = await db.execute(query)
    notifications = result.scalars().all()

    # Count unread
    unread_count = (await db.execute(
        select(func.count()).select_from(Notification).where(
            Notification.user_id == current_user["id"],
            Notification.is_read == False
        )
    )).scalar() or 0

    return ResponseModel(
        data={
            "notifications": [NotificationResponse.model_validate(n).model_dump() for n in notifications],
            "unread_count": unread_count,
        }
    )


@router.put("/mark-read", response_model=ResponseModel)
async def mark_notifications_read(
    data: MarkReadRequest,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Mark notifications as read."""
    await db.execute(
        update(Notification).where(
            Notification.id.in_(data.notification_ids),
            Notification.user_id == current_user["id"]
        ).values(is_read=True, read_at=datetime.now(timezone.utc))
    )

    return ResponseModel(message="Notifications marked as read")


@router.put("/mark-all-read", response_model=ResponseModel)
async def mark_all_read(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Mark all notifications as read."""
    await db.execute(
        update(Notification).where(
            Notification.user_id == current_user["id"],
            Notification.is_read == False
        ).values(is_read=True, read_at=datetime.now(timezone.utc))
    )

    return ResponseModel(message="All notifications marked as read")


@router.post("/send", response_model=ResponseModel)
async def send_push_notification(
    data: SendPushRequest,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator"]))
):
    """Send push notification to users (admin/coordinator only)."""
    notifications = []
    for user_id in data.user_ids:
        notification = Notification(
            user_id=user_id,
            title=data.title,
            body=data.body,
            notification_type=data.notification_type,
            is_sent=True,
            sent_at=datetime.now(timezone.utc),
        )
        db.add(notification)
        notifications.append(notification)

    await db.flush()

    # In production, send via Firebase Cloud Messaging
    # firebase_admin.messaging.send_multicast(...)

    return ResponseModel(
        message=f"Notification sent to {len(data.user_ids)} users",
        data={"sent_count": len(notifications)}
    )
