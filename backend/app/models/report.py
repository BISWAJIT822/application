"""Report model - Generated report tracking."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, DateTime, Enum, Text, ForeignKey, Date, JSON
)
from app.database import Base


class ReportType(str, enum.Enum):
    DAILY = "daily"
    WEEKLY = "weekly"
    MONTHLY = "monthly"
    CUSTOM = "custom"


class ReportCategory(str, enum.Enum):
    ENROLLMENT = "enrollment"
    PREMIUM = "premium"
    CLAIMS = "claims"
    VACCINATION = "vaccination"
    ANALYTICS = "analytics"
    AUDIT = "audit"


class ReportFormat(str, enum.Enum):
    PDF = "pdf"
    EXCEL = "excel"
    CSV = "csv"


class Report(Base):
    __tablename__ = "reports"

    id = Column(Integer, primary_key=True, autoincrement=True)

    # Report Details
    title = Column(String(200), nullable=False)
    report_type = Column(Enum(ReportType), nullable=False)
    category = Column(Enum(ReportCategory), nullable=False)
    format = Column(Enum(ReportFormat), default=ReportFormat.PDF)

    # Date Range
    start_date = Column(Date, nullable=False)
    end_date = Column(Date, nullable=False)

    # Content
    summary = Column(Text, nullable=True)
    data = Column(JSON, nullable=True)
    file_url = Column(String(500), nullable=True)

    # Filters Applied
    filters = Column(JSON, nullable=True)

    # Metadata
    generated_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    generated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Report(id={self.id}, type={self.report_type}, category={self.category})>"
