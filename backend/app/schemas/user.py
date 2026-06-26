"""User schemas - Authentication and profile."""
from typing import Optional
from pydantic import BaseModel, Field
from datetime import datetime


class SendOtpRequest(BaseModel):
    phone: str = Field(..., min_length=10, max_length=15, pattern=r"^\+?[0-9]{10,15}$")


class VerifyOtpRequest(BaseModel):
    phone: str = Field(..., min_length=10, max_length=15)
    otp: str = Field(..., min_length=4, max_length=6)
    role: Optional[str] = None
    fcm_token: Optional[str] = None


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int
    user: "UserResponse"


class RefreshTokenRequest(BaseModel):
    refresh_token: str


class UserCreate(BaseModel):
    phone: str = Field(..., min_length=10, max_length=15)
    name: str = Field(..., min_length=2, max_length=100)
    role: str = Field(default="farmer")
    email: Optional[str] = None
    district: Optional[str] = None
    block: Optional[str] = None
    village: Optional[str] = None


class UserUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=2, max_length=100)
    email: Optional[str] = None
    avatar_url: Optional[str] = None
    district: Optional[str] = None
    block: Optional[str] = None
    village: Optional[str] = None
    language: Optional[str] = None
    fcm_token: Optional[str] = None


class UserResponse(BaseModel):
    id: int
    phone: str
    name: str
    role: str
    email: Optional[str] = None
    avatar_url: Optional[str] = None
    is_active: bool
    is_verified: bool
    district: Optional[str] = None
    block: Optional[str] = None
    village: Optional[str] = None
    language: str = "en"
    last_login: Optional[datetime] = None
    created_at: datetime

    class Config:
        from_attributes = True


class ProfileResponse(BaseModel):
    user: UserResponse
    stats: Optional[dict] = None


class ChangeLanguageRequest(BaseModel):
    language: str = Field(..., min_length=2, max_length=10)
