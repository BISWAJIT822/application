"""Enrollment model - Multi-step enrollment process tracking."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, DateTime, Enum, Text, ForeignKey, JSON, Boolean
)
from app.database import Base


class EnrollmentStatus(str, enum.Enum):
    DRAFT = "draft"
    STEP_1_FARMER = "step_1_farmer"
    STEP_2_GOAT = "step_2_goat"
    STEP_3_PHOTOS = "step_3_photos"
    STEP_4_QR_TAG = "step_4_qr_tag"
    STEP_5_VACCINATION = "step_5_vaccination"
    STEP_6_PREMIUM = "step_6_premium"
    STEP_7_POLICY = "step_7_policy"
    COMPLETED = "completed"
    CANCELLED = "cancelled"


class Enrollment(Base):
    __tablename__ = "enrollments"

    id = Column(Integer, primary_key=True, autoincrement=True)
    enrollment_number = Column(String(50), unique=True, nullable=False, index=True)

    # References
    farmer_id = Column(Integer, ForeignKey("farmers.id"), nullable=True)
    goat_id = Column(Integer, ForeignKey("goats.id"), nullable=True)
    policy_id = Column(Integer, ForeignKey("policies.id"), nullable=True)
    premium_id = Column(Integer, ForeignKey("premiums.id"), nullable=True)

    # Status
    current_step = Column(Integer, default=1)
    status = Column(Enum(EnrollmentStatus), default=EnrollmentStatus.DRAFT)

    # Step Data (JSON for flexible storage during draft)
    step_1_data = Column(JSON, nullable=True)  # Farmer details
    step_2_data = Column(JSON, nullable=True)  # Goat details
    step_3_data = Column(JSON, nullable=True)  # Photo URLs
    step_4_data = Column(JSON, nullable=True)  # QR/Tag data
    step_5_data = Column(JSON, nullable=True)  # Vaccination data
    step_6_data = Column(JSON, nullable=True)  # Premium data
    step_7_data = Column(JSON, nullable=True)  # Policy data

    # Sync
    is_synced = Column(Boolean, default=True)
    offline_id = Column(String(100), nullable=True)  # Local UUID for offline creation

    # Metadata
    created_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    completed_at = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Enrollment(id={self.id}, step={self.current_step}, status={self.status})>"
