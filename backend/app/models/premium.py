"""Premium model - Premium payment tracking."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Float, DateTime, Enum, Text, ForeignKey
)
from app.database import Base


class PaymentMode(str, enum.Enum):
    CASH = "cash"
    UPI = "upi"
    ONLINE = "online"
    BANK_TRANSFER = "bank_transfer"


class PremiumStatus(str, enum.Enum):
    PENDING = "pending"
    PAID = "paid"
    FAILED = "failed"
    REFUNDED = "refunded"


class Premium(Base):
    __tablename__ = "premiums"

    id = Column(Integer, primary_key=True, autoincrement=True)
    policy_id = Column(Integer, ForeignKey("policies.id"), nullable=False, index=True)
    farmer_id = Column(Integer, ForeignKey("farmers.id"), nullable=False, index=True)
    enrollment_id = Column(Integer, ForeignKey("enrollments.id"), nullable=True)

    # Payment Details
    amount = Column(Float, nullable=False)
    payment_mode = Column(Enum(PaymentMode), nullable=False)
    transaction_id = Column(String(100), nullable=True, unique=True)
    payment_date = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    status = Column(Enum(PremiumStatus), default=PremiumStatus.PENDING)

    # Receipt
    receipt_number = Column(String(50), unique=True, nullable=True)
    receipt_url = Column(String(500), nullable=True)

    # UPI Details
    upi_id = Column(String(100), nullable=True)
    upi_reference = Column(String(100), nullable=True)

    # Metadata
    collected_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    remarks = Column(Text, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Premium(id={self.id}, amount={self.amount}, status={self.status})>"
