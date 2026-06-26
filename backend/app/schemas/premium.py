"""Premium schemas - Premium payment management."""
from typing import Optional
from pydantic import BaseModel, Field
from datetime import datetime


class PremiumCreate(BaseModel):
    policy_id: int
    farmer_id: int
    enrollment_id: Optional[int] = None
    amount: float = Field(..., gt=0)
    payment_mode: str = Field(..., pattern=r"^(cash|upi|online|bank_transfer)$")
    transaction_id: Optional[str] = None
    upi_id: Optional[str] = None
    upi_reference: Optional[str] = None
    remarks: Optional[str] = None


class PremiumUpdate(BaseModel):
    status: Optional[str] = None
    transaction_id: Optional[str] = None
    receipt_url: Optional[str] = None
    remarks: Optional[str] = None


class PremiumResponse(BaseModel):
    id: int
    policy_id: int
    farmer_id: int
    amount: float
    payment_mode: str
    transaction_id: Optional[str] = None
    payment_date: datetime
    status: str
    receipt_number: Optional[str] = None
    receipt_url: Optional[str] = None
    upi_id: Optional[str] = None
    collected_by: Optional[int] = None
    remarks: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True
