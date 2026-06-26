"""Notification schemas."""
from typing import Optional
from pydantic import BaseModel
from datetime import datetime


class NotificationCreate(BaseModel):
    user_id: int
    title: str
    body: str
    notification_type: str = "general"
    image_url: Optional[str] = None
    reference_id: Optional[int] = None
    reference_type: Optional[str] = None


class NotificationResponse(BaseModel):
    id: int
    user_id: int
    title: str
    body: str
    notification_type: str
    image_url: Optional[str] = None
    reference_id: Optional[int] = None
    reference_type: Optional[str] = None
    is_read: bool = False
    is_sent: bool = False
    sent_at: Optional[datetime] = None
    read_at: Optional[datetime] = None
    created_at: datetime

    class Config:
        from_attributes = True


class MarkReadRequest(BaseModel):
    notification_ids: list[int]


class SendPushRequest(BaseModel):
    user_ids: list[int]
    title: str
    body: str
    notification_type: str = "general"
    data: Optional[dict] = None
