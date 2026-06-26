"""Policy schemas - Insurance policy management."""
from typing import Optional
from pydantic import BaseModel, Field
from datetime import date, datetime


class PolicyCreate(BaseModel):
    farmer_id: int
    goat_id: int
    enrollment_id: Optional[int] = None
    start_date: date
    end_date: date
    sum_insured: float = Field(..., gt=0)
    premium_amount: float = Field(..., gt=0)
    scheme_name: Optional[str] = None
    remarks: Optional[str] = None


class PolicyUpdate(BaseModel):
    status: Optional[str] = None
    certificate_url: Optional[str] = None
    certificate_number: Optional[str] = None
    remarks: Optional[str] = None


class PolicyResponse(BaseModel):
    id: int
    policy_number: str
    enrollment_id: Optional[int] = None
    farmer_id: int
    goat_id: int
    start_date: date
    end_date: date
    sum_insured: float
    premium_amount: float
    status: str
    certificate_url: Optional[str] = None
    certificate_number: Optional[str] = None
    insurer_name: Optional[str] = None
    scheme_name: Optional[str] = None
    issued_by: Optional[int] = None
    remarks: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True


class PolicyListResponse(BaseModel):
    id: int
    policy_number: str
    farmer_id: int
    goat_id: int
    start_date: date
    end_date: date
    sum_insured: float
    status: str
    created_at: datetime

    class Config:
        from_attributes = True
