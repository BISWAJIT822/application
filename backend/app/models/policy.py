"""Policy model - Insurance policy management."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Float, DateTime, Enum, Text, ForeignKey, Date
)
from app.database import Base


class PolicyStatus(str, enum.Enum):
    DRAFT = "draft"
    ACTIVE = "active"
    EXPIRED = "expired"
    CLAIMED = "claimed"
    CANCELLED = "cancelled"
    LAPSED = "lapsed"


class Policy(Base):
    __tablename__ = "policies"

    id = Column(Integer, primary_key=True, autoincrement=True)
    policy_number = Column(String(50), unique=True, nullable=False, index=True)
    enrollment_id = Column(Integer, ForeignKey("enrollments.id"), nullable=True)
    farmer_id = Column(Integer, ForeignKey("farmers.id"), nullable=False, index=True)
    goat_id = Column(Integer, ForeignKey("goats.id"), nullable=False, index=True)

    # Policy Details
    start_date = Column(Date, nullable=False)
    end_date = Column(Date, nullable=False)
    sum_insured = Column(Float, nullable=False)
    premium_amount = Column(Float, nullable=False)
    status = Column(Enum(PolicyStatus), default=PolicyStatus.DRAFT)

    # Certificate
    certificate_url = Column(String(500), nullable=True)
    certificate_number = Column(String(50), nullable=True)

    # Insurance Company
    insurer_name = Column(String(100), default="Government Goat Insurance Scheme")
    scheme_name = Column(String(100), nullable=True)

    # Metadata
    issued_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    approved_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    remarks = Column(Text, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Policy(id={self.id}, number={self.policy_number}, status={self.status})>"
