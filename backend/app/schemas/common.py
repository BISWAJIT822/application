"""Common schemas - Shared response models and pagination."""
from typing import Generic, TypeVar, Optional, List, Any
from pydantic import BaseModel
from datetime import datetime

T = TypeVar("T")


class ResponseModel(BaseModel):
    """Standard API response wrapper."""
    success: bool = True
    message: str = "Success"
    data: Optional[Any] = None


class ErrorResponse(BaseModel):
    """Standard error response."""
    success: bool = False
    message: str
    error_code: Optional[str] = None
    details: Optional[Any] = None


class PaginatedResponse(BaseModel):
    """Paginated list response."""
    items: List[Any]
    total: int
    page: int
    page_size: int
    total_pages: int
    has_next: bool
    has_previous: bool


class PaginationParams(BaseModel):
    """Pagination query parameters."""
    page: int = 1
    page_size: int = 20
    sort_by: Optional[str] = "created_at"
    sort_order: Optional[str] = "desc"
    search: Optional[str] = None


class StatsResponse(BaseModel):
    """Dashboard statistics response."""
    total_enrollments: int = 0
    total_premium_collected: float = 0.0
    total_claims: int = 0
    pending_claims: int = 0
    vaccination_due: int = 0
    death_reports: int = 0
    pending_approvals: int = 0
    active_policies: int = 0
    total_farmers: int = 0
    total_goats: int = 0


class FileUploadResponse(BaseModel):
    """File upload response."""
    file_url: str
    file_name: str
    file_size: int
    content_type: str
