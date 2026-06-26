"""Vaccination model - Vaccination records and scheduling."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Float, DateTime, Enum, Text, ForeignKey, Date, Boolean
)
from app.database import Base


class VaccineType(str, enum.Enum):
    PPR = "PPR"
    FMD = "FMD"
    ET = "ET"
    GOAT_POX = "Goat Pox"
    ANTHRAX = "Anthrax"
    HS = "HS"
    BQ = "BQ"
    OTHER = "Other"


class VaccinationStatus(str, enum.Enum):
    SCHEDULED = "scheduled"
    COMPLETED = "completed"
    OVERDUE = "overdue"
    MISSED = "missed"
    CANCELLED = "cancelled"


class Vaccination(Base):
    __tablename__ = "vaccinations"

    id = Column(Integer, primary_key=True, autoincrement=True)
    goat_id = Column(Integer, ForeignKey("goats.id"), nullable=False, index=True)
    farmer_id = Column(Integer, ForeignKey("farmers.id"), nullable=False, index=True)

    # Vaccination Details
    vaccine_type = Column(Enum(VaccineType), nullable=False)
    vaccine_name = Column(String(100), nullable=True)
    batch_number = Column(String(50), nullable=True)
    manufacturer = Column(String(100), nullable=True)
    dose_number = Column(Integer, default=1)

    # Dates
    vaccination_date = Column(Date, nullable=True)
    next_due_date = Column(Date, nullable=True)
    status = Column(Enum(VaccinationStatus), default=VaccinationStatus.SCHEDULED)

    # Certificate
    certificate_url = Column(String(500), nullable=True)

    # Location
    vaccination_center = Column(String(200), nullable=True)
    latitude = Column(Float, nullable=True)
    longitude = Column(Float, nullable=True)

    # Metadata
    administered_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    remarks = Column(Text, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Vaccination(id={self.id}, type={self.vaccine_type}, status={self.status})>"
