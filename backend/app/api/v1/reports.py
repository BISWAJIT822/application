"""Reports API - Generate and export reports."""
from datetime import date, datetime, timezone
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func

from app.database import get_db
from app.models.report import Report, ReportType, ReportCategory
from app.models.enrollment import Enrollment, EnrollmentStatus
from app.models.claim import Claim
from app.models.premium import Premium, PremiumStatus
from app.models.vaccination import Vaccination
from app.models.farmer import Farmer
from app.models.goat import Goat
from app.schemas.report import ReportCreate, ReportResponse
from app.schemas.common import ResponseModel, StatsResponse
from app.core.security import get_current_user, require_roles

router = APIRouter(prefix="/reports", tags=["Reports"])


@router.get("/dashboard-stats", response_model=ResponseModel)
async def get_dashboard_stats(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """Get dashboard statistics."""
    total_enrollments = (await db.execute(
        select(func.count()).select_from(Enrollment)
    )).scalar() or 0

    total_premium = (await db.execute(
        select(func.coalesce(func.sum(Premium.amount), 0)).where(Premium.status == PremiumStatus.PAID)
    )).scalar() or 0.0

    total_claims = (await db.execute(
        select(func.count()).select_from(Claim)
    )).scalar() or 0

    pending_claims = (await db.execute(
        select(func.count()).select_from(Claim).where(
            Claim.status.in_(["submitted", "under_review", "verification"])
        )
    )).scalar() or 0

    vaccination_due = (await db.execute(
        select(func.count()).select_from(Vaccination).where(
            Vaccination.status.in_(["scheduled", "overdue"]),
            Vaccination.next_due_date <= date.today()
        )
    )).scalar() or 0

    total_farmers = (await db.execute(
        select(func.count()).select_from(Farmer).where(Farmer.is_active == True)
    )).scalar() or 0

    total_goats = (await db.execute(
        select(func.count()).select_from(Goat).where(Goat.status != "removed")
    )).scalar() or 0

    death_reports = (await db.execute(
        select(func.count()).select_from(Claim).where(Claim.status == "submitted")
    )).scalar() or 0

    completed_enrollments = (await db.execute(
        select(func.count()).select_from(Enrollment).where(
            Enrollment.status == EnrollmentStatus.COMPLETED
        )
    )).scalar() or 0

    stats = StatsResponse(
        total_enrollments=total_enrollments,
        total_premium_collected=float(total_premium),
        total_claims=total_claims,
        pending_claims=pending_claims,
        vaccination_due=vaccination_due,
        death_reports=death_reports,
        pending_approvals=pending_claims,
        active_policies=completed_enrollments,
        total_farmers=total_farmers,
        total_goats=total_goats,
    )

    return ResponseModel(data=stats.model_dump())


@router.post("/generate", response_model=ResponseModel)
async def generate_report(
    data: ReportCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator"]))
):
    """Generate a report."""
    report_data = {}

    if data.category == "enrollment":
        enrollments = (await db.execute(
            select(func.count()).select_from(Enrollment).where(
                Enrollment.created_at >= datetime.combine(data.start_date, datetime.min.time()),
                Enrollment.created_at <= datetime.combine(data.end_date, datetime.max.time()),
            )
        )).scalar() or 0
        completed = (await db.execute(
            select(func.count()).select_from(Enrollment).where(
                Enrollment.status == EnrollmentStatus.COMPLETED,
                Enrollment.created_at >= datetime.combine(data.start_date, datetime.min.time()),
                Enrollment.created_at <= datetime.combine(data.end_date, datetime.max.time()),
            )
        )).scalar() or 0
        report_data = {
            "total_enrollments": enrollments,
            "completed_enrollments": completed,
            "pending_enrollments": enrollments - completed,
        }

    elif data.category == "premium":
        total = (await db.execute(
            select(func.coalesce(func.sum(Premium.amount), 0)).where(
                Premium.status == PremiumStatus.PAID,
                Premium.created_at >= datetime.combine(data.start_date, datetime.min.time()),
                Premium.created_at <= datetime.combine(data.end_date, datetime.max.time()),
            )
        )).scalar() or 0
        count = (await db.execute(
            select(func.count()).select_from(Premium).where(
                Premium.status == PremiumStatus.PAID,
                Premium.created_at >= datetime.combine(data.start_date, datetime.min.time()),
                Premium.created_at <= datetime.combine(data.end_date, datetime.max.time()),
            )
        )).scalar() or 0
        report_data = {
            "total_premium_collected": float(total),
            "total_payments": count,
        }

    elif data.category == "claims":
        total = (await db.execute(
            select(func.count()).select_from(Claim).where(
                Claim.created_at >= datetime.combine(data.start_date, datetime.min.time()),
                Claim.created_at <= datetime.combine(data.end_date, datetime.max.time()),
            )
        )).scalar() or 0
        approved = (await db.execute(
            select(func.count()).select_from(Claim).where(
                Claim.status.in_(["approved", "settled"]),
                Claim.created_at >= datetime.combine(data.start_date, datetime.min.time()),
                Claim.created_at <= datetime.combine(data.end_date, datetime.max.time()),
            )
        )).scalar() or 0
        settlement_total = (await db.execute(
            select(func.coalesce(func.sum(Claim.approved_amount), 0)).where(
                Claim.status == "settled",
                Claim.created_at >= datetime.combine(data.start_date, datetime.min.time()),
                Claim.created_at <= datetime.combine(data.end_date, datetime.max.time()),
            )
        )).scalar() or 0
        report_data = {
            "total_claims": total,
            "approved_claims": approved,
            "rejected_claims": total - approved,
            "total_settlement": float(settlement_total),
        }

    report = Report(
        title=data.title,
        report_type=ReportType(data.report_type),
        category=ReportCategory(data.category),
        start_date=data.start_date,
        end_date=data.end_date,
        summary=str(report_data),
        data=report_data,
        filters=data.filters,
        generated_by=current_user["id"],
    )
    db.add(report)
    await db.flush()

    return ResponseModel(
        message="Report generated",
        data=ReportResponse.model_validate(report).model_dump()
    )


@router.get("/", response_model=ResponseModel)
async def list_reports(
    category: Optional[str] = Query(None),
    report_type: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(require_roles(["admin", "coordinator"]))
):
    """List generated reports."""
    query = select(Report).order_by(Report.generated_at.desc()).limit(50)

    if category:
        query = query.where(Report.category == category)
    if report_type:
        query = query.where(Report.report_type == report_type)

    result = await db.execute(query)
    reports = result.scalars().all()

    return ResponseModel(
        data=[ReportResponse.model_validate(r).model_dump() for r in reports]
    )
