"""Claim schemas - Insurance claim management."""
from typing import Optional, List
from pydantic import BaseModel, Field
from datetime import date, datetime


class ClaimCreate(BaseModel):
    policy_id: int
    goat_id: int
    farmer_id: int
    death_date: date
    death_cause: Optional[str] = None
    death_description: Optional[str] = None
    death_location: Optional[str] = None
    death_photos: Optional[List[str]] = None
    carcass_photos: Optional[List[str]] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    claim_amount: Optional[float] = None


class ClaimUpdate(BaseModel):
    death_cause: Optional[str] = None
    death_description: Optional[str] = None
    death_photos: Optional[List[str]] = None
    carcass_photos: Optional[List[str]] = None
    status: Optional[str] = None
    remarks: Optional[str] = None


class ClaimReview(BaseModel):
    status: str = Field(..., pattern=r"^(approved|rejected|on_hold)$")
    review_remarks: Optional[str] = None
    approved_amount: Optional[float] = None


class ClaimVerification(BaseModel):
    carcass_verified: bool
    verification_remarks: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class ClaimSettlement(BaseModel):
    approved_amount: float = Field(..., gt=0)
    payment_mode: str
    settlement_reference: Optional[str] = None


class ClaimResponse(BaseModel):
    id: int
    claim_number: str
    policy_id: int
    goat_id: int
    farmer_id: int
    death_date: date
    death_cause: Optional[str] = None
    death_description: Optional[str] = None
    death_location: Optional[str] = None
    death_photos: Optional[List[str]] = None
    carcass_photos: Optional[List[str]] = None
    carcass_verified: bool = False
    verification_date: Optional[datetime] = None
    verified_by: Optional[int] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    ai_assessment_score: Optional[float] = None
    ai_assessment_remarks: Optional[str] = None
    ai_fraud_flag: bool = False
    status: str
    reviewed_by: Optional[int] = None
    review_date: Optional[datetime] = None
    review_remarks: Optional[str] = None
    claim_amount: Optional[float] = None
    approved_amount: Optional[float] = None
    settlement_date: Optional[datetime] = None
    settlement_reference: Optional[str] = None
    filed_by: Optional[int] = None
    created_at: datetime

    class Config:
        from_attributes = True


class ClaimListResponse(BaseModel):
    id: int
    claim_number: str
    farmer_id: int
    goat_id: int
    death_date: date
    status: str
    claim_amount: Optional[float] = None
    approved_amount: Optional[float] = None
    created_at: datetime

    class Config:
        from_attributes = True
