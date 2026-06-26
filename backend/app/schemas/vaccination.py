"""Vaccination schemas - Vaccination records and scheduling."""
from typing import Optional, List
from pydantic import BaseModel, Field
from datetime import date, datetime


class VaccinationCreate(BaseModel):
    goat_id: int
    farmer_id: int
    vaccine_type: str
    vaccine_name: Optional[str] = None
    batch_number: Optional[str] = None
    manufacturer: Optional[str] = None
    dose_number: int = 1
    vaccination_date: Optional[date] = None
    next_due_date: Optional[date] = None
    status: str = "scheduled"
    vaccination_center: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    remarks: Optional[str] = None


class VaccinationUpdate(BaseModel):
    vaccination_date: Optional[date] = None
    next_due_date: Optional[date] = None
    status: Optional[str] = None
    certificate_url: Optional[str] = None
    batch_number: Optional[str] = None
    remarks: Optional[str] = None


class VaccinationResponse(BaseModel):
    id: int
    goat_id: int
    farmer_id: int
    vaccine_type: str
    vaccine_name: Optional[str] = None
    batch_number: Optional[str] = None
    manufacturer: Optional[str] = None
    dose_number: int
    vaccination_date: Optional[date] = None
    next_due_date: Optional[date] = None
    status: str
    certificate_url: Optional[str] = None
    vaccination_center: Optional[str] = None
    administered_by: Optional[int] = None
    remarks: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True


class VaccinationBulkCreate(BaseModel):
    """Create multiple vaccination records at once (for enrollment Step 5)."""
    goat_id: int
    farmer_id: int
    vaccinations: List[VaccinationCreate]


class VaccinationSchedule(BaseModel):
    """Upcoming vaccination schedule."""
    goat_id: int
    goat_tag: Optional[str] = None
    farmer_name: str
    vaccine_type: str
    due_date: date
    status: str
