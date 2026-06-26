"""Goat schemas - Goat registration and management."""
from typing import Optional, List
from pydantic import BaseModel, Field
from datetime import datetime


class GoatCreate(BaseModel):
    farmer_id: int
    breed: str = Field(..., min_length=1, max_length=50)
    age_months: Optional[int] = Field(None, ge=0, le=240)
    gender: str = Field(..., pattern=r"^(male|female)$")
    weight_kg: Optional[float] = Field(None, ge=0, le=200)
    color: Optional[str] = None
    identification_marks: Optional[str] = None
    tag_number: Optional[str] = None
    ear_tag_number: Optional[str] = None
    market_value: Optional[float] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class GoatUpdate(BaseModel):
    breed: Optional[str] = None
    age_months: Optional[int] = None
    weight_kg: Optional[float] = None
    color: Optional[str] = None
    identification_marks: Optional[str] = None
    tag_number: Optional[str] = None
    ear_tag_number: Optional[str] = None
    health_status: Optional[str] = None
    market_value: Optional[float] = None
    status: Optional[str] = None


class GoatPhotoUpload(BaseModel):
    photo_left: Optional[str] = None
    photo_right: Optional[str] = None
    photo_front: Optional[str] = None
    photo_back: Optional[str] = None
    photo_top: Optional[str] = None
    photo_face: Optional[str] = None


class GoatResponse(BaseModel):
    id: int
    farmer_id: int
    tag_number: Optional[str] = None
    qr_code: Optional[str] = None
    ear_tag_number: Optional[str] = None
    breed: str
    age_months: Optional[int] = None
    gender: str
    weight_kg: Optional[float] = None
    color: Optional[str] = None
    identification_marks: Optional[str] = None
    photo_left: Optional[str] = None
    photo_right: Optional[str] = None
    photo_front: Optional[str] = None
    photo_back: Optional[str] = None
    photo_top: Optional[str] = None
    photo_face: Optional[str] = None
    health_status: str = "healthy"
    status: str
    market_value: Optional[float] = None
    insured_value: Optional[float] = None
    last_vaccination_date: Optional[datetime] = None
    next_vaccination_date: Optional[datetime] = None
    created_at: datetime

    class Config:
        from_attributes = True


class GoatListResponse(BaseModel):
    id: int
    farmer_id: int
    tag_number: Optional[str] = None
    breed: str
    gender: str
    status: str
    photo_face: Optional[str] = None
    age_months: Optional[int] = None
    weight_kg: Optional[float] = None
    created_at: datetime

    class Config:
        from_attributes = True


class QRTagAssignment(BaseModel):
    goat_id: int
    qr_code: str
    ear_tag_number: Optional[str] = None
