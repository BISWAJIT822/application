"""Goat model - Goat registration and tracking."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Float, DateTime, Enum, Text, ForeignKey, Boolean, JSON
)
from app.database import Base


class GoatGender(str, enum.Enum):
    MALE = "male"
    FEMALE = "female"


class GoatStatus(str, enum.Enum):
    ACTIVE = "active"
    INSURED = "insured"
    CLAIMED = "claimed"
    DECEASED = "deceased"
    SOLD = "sold"
    REMOVED = "removed"


class Goat(Base):
    __tablename__ = "goats"

    id = Column(Integer, primary_key=True, autoincrement=True)
    farmer_id = Column(Integer, ForeignKey("farmers.id"), nullable=False, index=True)

    # Identification
    tag_number = Column(String(50), unique=True, nullable=True, index=True)
    qr_code = Column(String(100), unique=True, nullable=True)
    ear_tag_number = Column(String(50), nullable=True)

    # Details
    breed = Column(String(50), nullable=False)
    age_months = Column(Integer, nullable=True)
    gender = Column(Enum(GoatGender), nullable=False)
    weight_kg = Column(Float, nullable=True)
    color = Column(String(50), nullable=True)
    identification_marks = Column(Text, nullable=True)

    # Photos (JSON array of URLs)
    photo_left = Column(String(500), nullable=True)
    photo_right = Column(String(500), nullable=True)
    photo_front = Column(String(500), nullable=True)
    photo_back = Column(String(500), nullable=True)
    photo_top = Column(String(500), nullable=True)
    photo_face = Column(String(500), nullable=True)

    # Health
    health_status = Column(String(50), default="healthy")
    last_vaccination_date = Column(DateTime, nullable=True)
    next_vaccination_date = Column(DateTime, nullable=True)

    # Location
    latitude = Column(Float, nullable=True)
    longitude = Column(Float, nullable=True)

    # Status
    status = Column(Enum(GoatStatus), default=GoatStatus.ACTIVE)
    market_value = Column(Float, nullable=True)
    insured_value = Column(Float, nullable=True)

    # Metadata
    registered_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Goat(id={self.id}, tag={self.tag_number}, breed={self.breed})>"
