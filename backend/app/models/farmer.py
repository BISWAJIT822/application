"""Farmer model - Farmer registration and details."""
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Float, DateTime, Text, ForeignKey, Boolean
)
from sqlalchemy.orm import relationship
from app.database import Base


class Farmer(Base):
    __tablename__ = "farmers"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True, index=True)

    # Personal Details
    name = Column(String(100), nullable=False)
    phone = Column(String(15), nullable=False, index=True)
    alternate_phone = Column(String(15), nullable=True)
    aadhaar_number = Column(String(12), nullable=True, unique=True)
    aadhaar_photo_url = Column(String(500), nullable=True)
    photo_url = Column(String(500), nullable=True)

    # Address
    village = Column(String(100), nullable=False)
    block = Column(String(100), nullable=True)
    district = Column(String(100), nullable=True)
    state = Column(String(100), default="Jharkhand")
    pin_code = Column(String(6), nullable=True)
    full_address = Column(Text, nullable=True)

    # GPS Location
    latitude = Column(Float, nullable=True)
    longitude = Column(Float, nullable=True)

    # Bank Details
    bank_name = Column(String(100), nullable=True)
    account_number = Column(String(20), nullable=True)
    ifsc_code = Column(String(11), nullable=True)
    account_holder_name = Column(String(100), nullable=True)

    # Metadata
    total_goats = Column(Integer, default=0)
    is_active = Column(Boolean, default=True)
    registered_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    def __repr__(self):
        return f"<Farmer(id={self.id}, name={self.name}, village={self.village})>"
