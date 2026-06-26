"""
Goat Insurance Management System - Security Module
JWT, OTP, Password Hashing, Role-based Access Control
"""
import random
import string
import time
from datetime import datetime, timedelta, timezone
from typing import Optional, Dict, Any, List

from fastapi import Depends, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError, jwt
from passlib.context import CryptContext

from app.config import get_settings
from app.core.exceptions import UnauthorizedException, ForbiddenException

settings = get_settings()

# Password hashing
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Bearer token
security = HTTPBearer()

# In-memory OTP store (use Redis in production)
_otp_store: Dict[str, Dict[str, Any]] = {}

# Revoked tokens set (use Redis in production)
_revoked_tokens: set = set()


# ──────────────────────────────────────────────
# Password Hashing
# ──────────────────────────────────────────────

def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)


# ──────────────────────────────────────────────
# JWT Tokens
# ──────────────────────────────────────────────

def create_access_token(
    data: dict,
    expires_delta: Optional[timedelta] = None
) -> str:
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + (
        expires_delta or timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    )
    to_encode.update({
        "exp": expire,
        "iat": datetime.now(timezone.utc),
        "type": "access"
    })
    return jwt.encode(to_encode, settings.JWT_SECRET_KEY, algorithm=settings.JWT_ALGORITHM)


def create_refresh_token(data: dict) -> str:
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode.update({
        "exp": expire,
        "iat": datetime.now(timezone.utc),
        "type": "refresh"
    })
    return jwt.encode(to_encode, settings.JWT_SECRET_KEY, algorithm=settings.JWT_ALGORITHM)


def decode_token(token: str) -> dict:
    try:
        payload = jwt.decode(
            token,
            settings.JWT_SECRET_KEY,
            algorithms=[settings.JWT_ALGORITHM]
        )
        if token in _revoked_tokens:
            raise UnauthorizedException("Token has been revoked")
        return payload
    except JWTError:
        raise UnauthorizedException("Invalid or expired token")


def revoke_token(token: str):
    _revoked_tokens.add(token)


# ──────────────────────────────────────────────
# OTP Management
# ──────────────────────────────────────────────

def generate_otp(phone: str) -> str:
    otp = ''.join(random.choices(string.digits, k=settings.OTP_LENGTH))
    _otp_store[phone] = {
        "otp": otp,
        "created_at": time.time(),
        "attempts": 0
    }
    return otp


def verify_otp(phone: str, otp: str) -> bool:
    if phone not in _otp_store:
        return False

    stored = _otp_store[phone]

    # Check expiry
    if time.time() - stored["created_at"] > settings.OTP_EXPIRY_SECONDS:
        del _otp_store[phone]
        return False

    # Check max attempts
    if stored["attempts"] >= 5:
        del _otp_store[phone]
        return False

    stored["attempts"] += 1

    if stored["otp"] == otp:
        del _otp_store[phone]
        return True

    return False


# ──────────────────────────────────────────────
# Authentication Dependencies
# ──────────────────────────────────────────────

async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> dict:
    """Extract and validate current user from JWT token."""
    payload = decode_token(credentials.credentials)
    if payload.get("type") != "access":
        raise UnauthorizedException("Invalid token type")

    user_id = payload.get("sub")
    if not user_id:
        raise UnauthorizedException("Invalid token payload")

    return {
        "id": int(user_id),
        "phone": payload.get("phone"),
        "role": payload.get("role"),
        "name": payload.get("name"),
    }


def require_roles(allowed_roles: List[str]):
    """Dependency factory for role-based access control."""
    async def role_checker(current_user: dict = Depends(get_current_user)):
        if current_user["role"] not in allowed_roles:
            raise ForbiddenException(
                f"Role '{current_user['role']}' is not authorized. "
                f"Required: {', '.join(allowed_roles)}"
            )
        return current_user
    return role_checker


# Convenience role dependencies
require_admin = require_roles(["admin"])
require_coordinator = require_roles(["admin", "coordinator"])
require_suraksha_didi = require_roles(["admin", "coordinator", "suraksha_didi"])
require_any_authenticated = require_roles(["admin", "coordinator", "suraksha_didi", "farmer"])
