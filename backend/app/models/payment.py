"""Payment model - General payment transactions."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Float, DateTime, Enum, Text, ForeignKey
)
from app.database import Base


class PaymentType(str, enum.Enum):
    PREMIUM = "premium"
    CLAIM_SETTLEMENT = "claim_settlement"
    REFUND = "refund"


class TransactionStatus(str, enum.Enum):
    INITIATED = "initiated"
    PROCESSING = "processing"
    SUCCESS = "success"
    FAILED = "failed"
    REVERSED = "reversed"


class Payment(Base):
    __tablename__ = "payments"

    id = Column(Integer, primary_key=True, autoincrement=True)
    transaction_id = Column(String(100), unique=True, nullable=False, index=True)
    farmer_id = Column(Integer, ForeignKey("farmers.id"), nullable=False, index=True)

    # Payment Details
    payment_type = Column(Enum(PaymentType), nullable=False)
    amount = Column(Float, nullable=False)
    payment_mode = Column(String(50), nullable=False)
    status = Column(Enum(TransactionStatus), default=TransactionStatus.INITIATED)

    # Reference
    reference_id = Column(Integer, nullable=True)  # policy_id or claim_id
    reference_type = Column(String(50), nullable=True)  # "policy" or "claim"

    # Gateway Details
    gateway_name = Column(String(50), nullable=True)
    gateway_transaction_id = Column(String(100), nullable=True)
    gateway_response = Column(Text, nullable=True)

    # Metadata
    processed_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    remarks = Column(Text, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Payment(id={self.id}, txn={self.transaction_id}, status={self.status})>"
