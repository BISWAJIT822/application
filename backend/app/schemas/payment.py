"""Payment schemas - Transaction management."""
from typing import Optional
from pydantic import BaseModel, Field
from datetime import datetime


class PaymentCreate(BaseModel):
    farmer_id: int
    payment_type: str
    amount: float = Field(..., gt=0)
    payment_mode: str
    reference_id: Optional[int] = None
    reference_type: Optional[str] = None
    gateway_name: Optional[str] = None
    remarks: Optional[str] = None


class PaymentResponse(BaseModel):
    id: int
    transaction_id: str
    farmer_id: int
    payment_type: str
    amount: float
    payment_mode: str
    status: str
    reference_id: Optional[int] = None
    reference_type: Optional[str] = None
    gateway_transaction_id: Optional[str] = None
    processed_by: Optional[int] = None
    remarks: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True
