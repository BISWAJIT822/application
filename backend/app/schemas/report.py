"""Report schemas."""
from typing import Optional, Any
from pydantic import BaseModel
from datetime import date, datetime


class ReportCreate(BaseModel):
    title: str
    report_type: str
    category: str
    format: str = "pdf"
    start_date: date
    end_date: date
    filters: Optional[dict] = None


class ReportResponse(BaseModel):
    id: int
    title: str
    report_type: str
    category: str
    format: str
    start_date: date
    end_date: date
    summary: Optional[str] = None
    data: Optional[Any] = None
    file_url: Optional[str] = None
    filters: Optional[dict] = None
    generated_by: Optional[int] = None
    generated_at: datetime

    class Config:
        from_attributes = True
