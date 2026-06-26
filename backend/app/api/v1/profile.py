"""Profile API - User profile and settings management."""
from fastapi import APIRouter, Depends, UploadFile, File
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.database import get_db
from app.models.user import User
from app.schemas.user import UserUpdate, UserResponse, ProfileResponse, ChangeLanguageRequest
from app.schemas.common import ResponseModel
from app.core.security import get_current_user
from app.core.exceptions import NotFoundException

router = APIRouter(prefix="/profile", tags=["Profile"])


@router.get("/", response_model=ResponseModel)
async def get_profile(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get current user's profile."""
    result = await db.execute(select(User).where(User.id == current_user["id"]))
    user = result.scalar_one_or_none()
    if not user:
        raise NotFoundException("User")

    user_response = UserResponse.model_validate(user)
    return ResponseModel(data=user_response.model_dump())


@router.put("/", response_model=ResponseModel)
async def update_profile(
    data: UserUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Update current user's profile."""
    result = await db.execute(select(User).where(User.id == current_user["id"]))
    user = result.scalar_one_or_none()
    if not user:
        raise NotFoundException("User")

    update_data = data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(user, field, value)

    return ResponseModel(
        message="Profile updated",
        data=UserResponse.model_validate(user).model_dump()
    )


@router.put("/language", response_model=ResponseModel)
async def change_language(
    data: ChangeLanguageRequest,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Change user's preferred language."""
    result = await db.execute(select(User).where(User.id == current_user["id"]))
    user = result.scalar_one_or_none()
    if not user:
        raise NotFoundException("User")

    user.language = data.language

    return ResponseModel(message=f"Language changed to {data.language}")


@router.put("/fcm-token", response_model=ResponseModel)
async def update_fcm_token(
    fcm_token: str,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Update FCM token for push notifications."""
    result = await db.execute(select(User).where(User.id == current_user["id"]))
    user = result.scalar_one_or_none()
    if not user:
        raise NotFoundException("User")

    user.fcm_token = fcm_token

    return ResponseModel(message="FCM token updated")
