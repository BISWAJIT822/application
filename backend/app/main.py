"""
Goat Insurance Management System - FastAPI Application
Production-ready REST API with JWT auth, RBAC, and comprehensive endpoints.
"""
import os
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request, UploadFile, File, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from loguru import logger

from app.config import get_settings
from app.database import init_db
from app.core.exceptions import AppException
from app.core.security import get_current_user
from app.schemas.common import ResponseModel, FileUploadResponse

# API Routers
from app.api.v1.auth import router as auth_router
from app.api.v1.farmers import router as farmers_router
from app.api.v1.goats import router as goats_router
from app.api.v1.enrollments import router as enrollments_router
from app.api.v1.vaccinations import router as vaccinations_router
from app.api.v1.claims import router as claims_router
from app.api.v1.reports import router as reports_router
from app.api.v1.notifications import router as notifications_router
from app.api.v1.profile import router as profile_router
from app.api.v1.admin import router as admin_router

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifecycle management."""
    logger.info("🐐 Starting Goat Insurance Management System...")
    # Create upload directories
    os.makedirs(f"{settings.UPLOAD_DIR}/photos", exist_ok=True)
    os.makedirs(f"{settings.UPLOAD_DIR}/documents", exist_ok=True)
    os.makedirs(f"{settings.UPLOAD_DIR}/certificates", exist_ok=True)

    # Initialize database
    await init_db()
    logger.info("✅ Database initialized")

    yield

    logger.info("🛑 Shutting down...")


# Create FastAPI app
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description=(
        "Enterprise-grade API for managing goat insurance policies, enrollments, "
        "vaccinations, claims, and reporting across multiple user roles."
    ),
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    lifespan=lifespan,
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Exception handlers
@app.exception_handler(AppException)
async def app_exception_handler(request: Request, exc: AppException):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "message": exc.detail,
            "error_code": getattr(exc, "error_code", None),
        },
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled exception: {exc}")
    return JSONResponse(
        status_code=500,
        content={
            "success": False,
            "message": "Internal server error",
            "error_code": "INTERNAL_ERROR",
        },
    )


# Mount static files
if os.path.exists(settings.UPLOAD_DIR):
    app.mount("/uploads", StaticFiles(directory=settings.UPLOAD_DIR), name="uploads")


# Register API routers
API_V1 = settings.API_V1_PREFIX
app.include_router(auth_router, prefix=API_V1)
app.include_router(farmers_router, prefix=API_V1)
app.include_router(goats_router, prefix=API_V1)
app.include_router(enrollments_router, prefix=API_V1)
app.include_router(vaccinations_router, prefix=API_V1)
app.include_router(claims_router, prefix=API_V1)
app.include_router(reports_router, prefix=API_V1)
app.include_router(notifications_router, prefix=API_V1)
app.include_router(profile_router, prefix=API_V1)
app.include_router(admin_router, prefix=API_V1)


# File upload endpoint
@app.post("/api/v1/upload", response_model=ResponseModel, tags=["Upload"])
async def upload_file(
    file: UploadFile = File(...),
    category: str = "photos",
    current_user: dict = Depends(get_current_user),
):
    """Upload a file (photo, document, certificate)."""
    import uuid

    allowed_types = ["image/jpeg", "image/png", "image/webp", "application/pdf"]
    if file.content_type not in allowed_types:
        return JSONResponse(
            status_code=400,
            content={"success": False, "message": f"File type {file.content_type} not allowed"}
        )

    # Check file size
    contents = await file.read()
    if len(contents) > settings.MAX_UPLOAD_SIZE_MB * 1024 * 1024:
        return JSONResponse(
            status_code=400,
            content={"success": False, "message": f"File too large. Max: {settings.MAX_UPLOAD_SIZE_MB}MB"}
        )

    # Save file
    ext = file.filename.split(".")[-1] if file.filename else "jpg"
    filename = f"{uuid.uuid4().hex}.{ext}"
    filepath = os.path.join(settings.UPLOAD_DIR, category, filename)

    with open(filepath, "wb") as f:
        f.write(contents)

    file_url = f"/uploads/{category}/{filename}"

    return ResponseModel(
        message="File uploaded successfully",
        data=FileUploadResponse(
            file_url=file_url,
            file_name=filename,
            file_size=len(contents),
            content_type=file.content_type or "application/octet-stream",
        ).model_dump()
    )


# Health check
@app.get("/health", tags=["Health"])
async def health_check():
    return {"status": "healthy", "version": settings.APP_VERSION}


# Root
@app.get("/", tags=["Root"])
async def root():
    return {
        "name": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "docs": "/docs",
    }
