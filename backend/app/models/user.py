"""User model - Authentication and role management."""
import enum
from datetime import datetime, timezone
from sqlalchemy import (
    Column, Integer, String, Boolean, DateTime, Enum, Text
)
from app.database import Base


class UserRole(str, enum.Enum):
    ADMIN = "admin"
    COORDINATOR = "coordinator"
    SURAKSHA_DIDI = "suraksha_didi"
    FARMER = "farmer"


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    phone = Column(String(15), unique=True, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    email = Column(String(100), nullable=True)
    role = Column(Enum(UserRole), nullable=False, default=UserRole.FARMER)
    avatar_url = Column(String(500), nullable=True)

    # Authentication
    password_hash = Column(String(255), nullable=True)
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)
    last_login = Column(DateTime, nullable=True)
    fcm_token = Column(String(500), nullable=True)

    # Profile
    district = Column(String(100), nullable=True)
    block = Column(String(100), nullable=True)
    village = Column(String(100), nullable=True)
    language = Column(String(10), default="en")

    # Metadata
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))
    created_by = Column(Integer, nullable=True)

    def __repr__(self):
        return f"<User(id={self.id}, phone={self.phone}, role={self.role})>"
