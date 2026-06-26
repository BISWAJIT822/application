"""Enrollment schemas - Multi-step enrollment process."""
from typing import Optional, Any
from pydantic import BaseModel
from datetime import datetime


class EnrollmentCreate(BaseModel):
    """Start a new enrollment."""
    offline_id: Optional[str] = None


class EnrollmentStepUpdate(BaseModel):
    """Update a specific enrollment step."""
    step: int
    data: dict


class EnrollmentResponse(BaseModel):
    id: int
    enrollment_number: str
    farmer_id: Optional[int] = None
    goat_id: Optional[int] = None
    policy_id: Optional[int] = None
    premium_id: Optional[int] = None
    current_step: int
    status: str
    step_1_data: Optional[Any] = None
    step_2_data: Optional[Any] = None
    step_3_data: Optional[Any] = None
    step_4_data: Optional[Any] = None
    step_5_data: Optional[Any] = None
    step_6_data: Optional[Any] = None
    step_7_data: Optional[Any] = None
    is_synced: bool = True
    offline_id: Optional[str] = None
    created_by: Optional[int] = None
    completed_at: Optional[datetime] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class EnrollmentListResponse(BaseModel):
    id: int
    enrollment_number: str
    farmer_id: Optional[int] = None
    current_step: int
    status: str
    is_synced: bool = True
    created_at: datetime

    class Config:
        from_attributes = True


class EnrollmentFinalizeRequest(BaseModel):
    """Finalize enrollment and generate policy."""
    enrollment_id: int
    generate_certificate: bool = True
