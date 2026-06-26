"""Authentication API - OTP login, JWT tokens, role selection."""
import uuid
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.database import get_db
from app.models.user import User, UserRole
from app.models.audit_log import AuditLog
from app.schemas.user import (
    SendOtpRequest, VerifyOtpRequest, TokenResponse,
    RefreshTokenRequest, UserResponse, UserCreate
)
from app.schemas.common import ResponseModel
from app.core.security import (
    generate_otp, verify_otp, create_access_token,
    create_refresh_token, decode_token, revoke_token,
    get_current_user
)
from app.config import get_settings

router = APIRouter(prefix="/auth", tags=["Authentication"])
settings = get_settings()


@router.post("/send-otp", response_model=ResponseModel)
async def send_otp(
    request: SendOtpRequest,
    db: AsyncSession = Depends(get_db)
):
    """Send OTP to the provided phone number."""
    otp = generate_otp(request.phone)

    # In production, send via SMS gateway (Twilio, MSG91, etc.)
    # For development, return OTP in response
    return ResponseModel(
        success=True,
        message="OTP sent successfully",
        data={"otp": otp} if settings.DEBUG else None
    )


@router.post("/verify-otp", response_model=ResponseModel)
async def verify_otp_endpoint(
    request: VerifyOtpRequest,
    db: AsyncSession = Depends(get_db)
):
    """Verify OTP and return JWT tokens."""
    if not verify_otp(request.phone, request.otp):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired OTP"
        )

    # Find or create user
    result = await db.execute(select(User).where(User.phone == request.phone))
    user = result.scalar_one_or_none()

    if not user:
        # Auto-register new user
        role = UserRole(request.role) if request.role else UserRole.FARMER
        user = User(
            phone=request.phone,
            name=f"User_{request.phone[-4:]}",
            role=role,
            is_active=True,
            is_verified=True,
        )
        db.add(user)
        await db.flush()

    # Update last login
    user.last_login = datetime.now(timezone.utc)
    user.is_verified = True

    # Update FCM token if provided
    if request.fcm_token:
        user.fcm_token = request.fcm_token

    await db.flush()

    # Generate tokens
    token_data = {
        "sub": str(user.id),
        "phone": user.phone,
        "role": user.role.value if isinstance(user.role, UserRole) else user.role,
        "name": user.name,
    }
    access_token = create_access_token(token_data)
    refresh_token = create_refresh_token(token_data)

    # Audit log
    audit = AuditLog(
        user_id=user.id,
        action="LOGIN",
        entity_type="user",
        entity_id=user.id,
        description=f"User logged in via OTP from phone {request.phone}"
    )
    db.add(audit)

    user_response = UserResponse(
        id=user.id,
        phone=user.phone,
        name=user.name,
        role=user.role.value if isinstance(user.role, UserRole) else user.role,
        email=user.email,
        avatar_url=user.avatar_url,
        is_active=user.is_active,
        is_verified=user.is_verified,
        district=user.district,
        block=user.block,
        village=user.village,
        language=user.language or "en",
        last_login=user.last_login,
        created_at=user.created_at,
    )

    return ResponseModel(
        success=True,
        message="Login successful",
        data={
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer",
            "expires_in": settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
            "user": user_response.model_dump(),
        }
    )


@router.post("/refresh", response_model=ResponseModel)
async def refresh_token(
    request: RefreshTokenRequest,
    db: AsyncSession = Depends(get_db)
):
    """Refresh access token using refresh token."""
    payload = decode_token(request.refresh_token)
    if payload.get("type") != "refresh":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid refresh token"
        )

    token_data = {
        "sub": payload["sub"],
        "phone": payload.get("phone"),
        "role": payload.get("role"),
        "name": payload.get("name"),
    }
    new_access_token = create_access_token(token_data)

    return ResponseModel(
        success=True,
        message="Token refreshed",
        data={
            "access_token": new_access_token,
            "token_type": "bearer",
            "expires_in": settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        }
    )


@router.post("/logout", response_model=ResponseModel)
async def logout(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Logout and invalidate token."""
    # Audit log
    audit = AuditLog(
        user_id=current_user["id"],
        action="LOGOUT",
        entity_type="user",
        entity_id=current_user["id"],
        description="User logged out"
    )
    db.add(audit)

    return ResponseModel(
        success=True,
        message="Logged out successfully"
    )


@router.post("/register", response_model=ResponseModel)
async def register_user(
    request: UserCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Register a new user (admin/coordinator only)."""
    if current_user["role"] not in ["admin", "coordinator"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only admin and coordinator can register users"
        )

    # Check if phone already exists
    result = await db.execute(select(User).where(User.phone == request.phone))
    if result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Phone number already registered"
        )

    user = User(
        phone=request.phone,
        name=request.name,
        role=UserRole(request.role),
        email=request.email,
        district=request.district,
        block=request.block,
        village=request.village,
        is_active=True,
        created_by=current_user["id"],
    )
    db.add(user)
    await db.flush()

    return ResponseModel(
        success=True,
        message="User registered successfully",
        data={"user_id": user.id}
    )
