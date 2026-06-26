"""Notification model - Push notification management."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, DateTime, Enum, Text, ForeignKey, Boolean
)
from app.database import Base


class NotificationType(str, enum.Enum):
    VACCINATION_DUE = "vaccination_due"
    CLAIM_UPDATE = "claim_update"
    POLICY_EXPIRY = "policy_expiry"
    PAYMENT_RECEIVED = "payment_received"
    ENROLLMENT_COMPLETE = "enrollment_complete"
    GENERAL = "general"
    ALERT = "alert"


class Notification(Base):
    __tablename__ = "notifications"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)

    # Notification Details
    title = Column(String(200), nullable=False)
    body = Column(Text, nullable=False)
    notification_type = Column(Enum(NotificationType), default=NotificationType.GENERAL)
    image_url = Column(String(500), nullable=True)

    # Reference
    reference_id = Column(Integer, nullable=True)
    reference_type = Column(String(50), nullable=True)

    # Status
    is_read = Column(Boolean, default=False)
    is_sent = Column(Boolean, default=False)
    sent_at = Column(DateTime, nullable=True)
    read_at = Column(DateTime, nullable=True)

    # FCM
    fcm_message_id = Column(String(200), nullable=True)

    # Metadata
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Notification(id={self.id}, type={self.notification_type}, read={self.is_read})>"
