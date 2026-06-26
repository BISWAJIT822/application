"""Claim model - Insurance claim management."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Float, DateTime, Enum, Text, ForeignKey, Date, Boolean, JSON
)
from app.database import Base


class ClaimStatus(str, enum.Enum):
    DRAFT = "draft"
    SUBMITTED = "submitted"
    UNDER_REVIEW = "under_review"
    VERIFICATION = "verification"
    APPROVED = "approved"
    REJECTED = "rejected"
    ON_HOLD = "on_hold"
    SETTLED = "settled"
    CLOSED = "closed"


class DeathCause(str, enum.Enum):
    DISEASE = "disease"
    ACCIDENT = "accident"
    NATURAL = "natural"
    PREDATOR = "predator"
    POISONING = "poisoning"
    UNKNOWN = "unknown"
    OTHER = "other"


class Claim(Base):
    __tablename__ = "claims"

    id = Column(Integer, primary_key=True, autoincrement=True)
    claim_number = Column(String(50), unique=True, nullable=False, index=True)
    policy_id = Column(Integer, ForeignKey("policies.id"), nullable=False, index=True)
    goat_id = Column(Integer, ForeignKey("goats.id"), nullable=False, index=True)
    farmer_id = Column(Integer, ForeignKey("farmers.id"), nullable=False, index=True)

    # Death Details
    death_date = Column(Date, nullable=False)
    death_cause = Column(Enum(DeathCause), nullable=True)
    death_description = Column(Text, nullable=True)
    death_location = Column(String(200), nullable=True)

    # Evidence Photos (JSON array of URLs)
    death_photos = Column(JSON, nullable=True)
    carcass_photos = Column(JSON, nullable=True)

    # Verification
    carcass_verified = Column(Boolean, default=False)
    verification_date = Column(DateTime, nullable=True)
    verified_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    verification_remarks = Column(Text, nullable=True)

    # GPS Location
    latitude = Column(Float, nullable=True)
    longitude = Column(Float, nullable=True)

    # AI Assessment
    ai_assessment_score = Column(Float, nullable=True)
    ai_assessment_remarks = Column(Text, nullable=True)
    ai_fraud_flag = Column(Boolean, default=False)

    # Review
    status = Column(Enum(ClaimStatus), default=ClaimStatus.DRAFT)
    reviewed_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    review_date = Column(DateTime, nullable=True)
    review_remarks = Column(Text, nullable=True)

    # Settlement
    claim_amount = Column(Float, nullable=True)
    approved_amount = Column(Float, nullable=True)
    settlement_date = Column(DateTime, nullable=True)
    settlement_reference = Column(String(100), nullable=True)
    payment_mode = Column(String(50), nullable=True)

    # Metadata
    filed_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Claim(id={self.id}, number={self.claim_number}, status={self.status})>"
