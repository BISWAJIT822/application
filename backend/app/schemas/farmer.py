"""Farmer schemas - Farmer registration and management."""
from typing import Optional
from pydantic import BaseModel, Field
from datetime import datetime


class FarmerCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    phone: str = Field(..., min_length=10, max_length=15)
    alternate_phone: Optional[str] = None
    aadhaar_number: Optional[str] = Field(None, min_length=12, max_length=12, pattern=r"^[0-9]{12}$")
    village: str = Field(..., min_length=1, max_length=100)
    block: Optional[str] = None
    district: Optional[str] = None
    state: Optional[str] = "Jharkhand"
    pin_code: Optional[str] = Field(None, pattern=r"^[0-9]{6}$")
    full_address: Optional[str] = None
    latitude: Optional[float] = Field(None, ge=-90, le=90)
    longitude: Optional[float] = Field(None, ge=-180, le=180)
    photo_url: Optional[str] = None
    aadhaar_photo_url: Optional[str] = None
    bank_name: Optional[str] = None
    account_number: Optional[str] = None
    ifsc_code: Optional[str] = Field(None, pattern=r"^[A-Z]{4}0[A-Z0-9]{6}$")
    account_holder_name: Optional[str] = None


class FarmerUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=2, max_length=100)
    phone: Optional[str] = None
    alternate_phone: Optional[str] = None
    aadhaar_number: Optional[str] = None
    village: Optional[str] = None
    block: Optional[str] = None
    district: Optional[str] = None
    pin_code: Optional[str] = None
    full_address: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    photo_url: Optional[str] = None
    bank_name: Optional[str] = None
    account_number: Optional[str] = None
    ifsc_code: Optional[str] = None
    account_holder_name: Optional[str] = None


class FarmerResponse(BaseModel):
    id: int
    user_id: Optional[int] = None
    name: str
    phone: str
    alternate_phone: Optional[str] = None
    aadhaar_number: Optional[str] = None
    photo_url: Optional[str] = None
    village: str
    block: Optional[str] = None
    district: Optional[str] = None
    state: Optional[str] = None
    pin_code: Optional[str] = None
    full_address: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    total_goats: int = 0
    is_active: bool = True
    created_at: datetime

    class Config:
        from_attributes = True


class FarmerListResponse(BaseModel):
    id: int
    name: str
    phone: str
    village: str
    district: Optional[str] = None
    total_goats: int = 0
    is_active: bool = True
    photo_url: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True
