package com.goatinsurance.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ──────────────────────────────────────────────
// Common DTOs
// ──────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    @SerializedName("error_code") val errorCode: String? = null,
)

data class PaginatedData<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_previous") val hasPrevious: Boolean,
)

data class FileUploadData(
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_size") val fileSize: Int,
    @SerializedName("content_type") val contentType: String,
)

// ──────────────────────────────────────────────
// Auth DTOs
// ──────────────────────────────────────────────

data class SendOtpRequest(val phone: String)
data class OtpResponseData(val otp: String? = null)

data class VerifyOtpRequest(
    val phone: String,
    val otp: String,
    val role: String? = null,
    @SerializedName("fcm_token") val fcmToken: String? = null,
)

data class TokenResponseData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val user: UserDto,
)

data class RefreshTokenRequest(@SerializedName("refresh_token") val refreshToken: String)
data class TokenRefreshData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
)

// ──────────────────────────────────────────────
// User DTOs
// ──────────────────────────────────────────────

data class UserDto(
    val id: Int,
    val phone: String,
    val name: String,
    val role: String,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("is_verified") val isVerified: Boolean = false,
    val district: String? = null,
    val block: String? = null,
    val village: String? = null,
    val language: String = "en",
    @SerializedName("last_login") val lastLogin: String? = null,
    @SerializedName("created_at") val createdAt: String,
)

data class UserUpdateDto(
    val name: String? = null,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val district: String? = null,
    val block: String? = null,
    val village: String? = null,
    val language: String? = null,
)

data class LanguageDto(val language: String)

// ──────────────────────────────────────────────
// Farmer DTOs
// ──────────────────────────────────────────────

data class FarmerDto(
    val id: Int,
    val name: String,
    val phone: String,
    @SerializedName("alternate_phone") val alternatePhone: String? = null,
    @SerializedName("aadhaar_number") val aadhaarNumber: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    val village: String,
    val block: String? = null,
    val district: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("total_goats") val totalGoats: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String,
)

data class FarmerCreateDto(
    val name: String,
    val phone: String,
    @SerializedName("alternate_phone") val alternatePhone: String? = null,
    @SerializedName("aadhaar_number") val aadhaarNumber: String? = null,
    val village: String,
    val block: String? = null,
    val district: String? = null,
    @SerializedName("full_address") val fullAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
)

// ──────────────────────────────────────────────
// Goat DTOs
// ──────────────────────────────────────────────

data class GoatDto(
    val id: Int,
    @SerializedName("farmer_id") val farmerId: Int,
    @SerializedName("tag_number") val tagNumber: String? = null,
    @SerializedName("qr_code") val qrCode: String? = null,
    @SerializedName("ear_tag_number") val earTagNumber: String? = null,
    val breed: String,
    @SerializedName("age_months") val ageMonths: Int? = null,
    val gender: String,
    @SerializedName("weight_kg") val weightKg: Double? = null,
    val color: String? = null,
    @SerializedName("identification_marks") val identificationMarks: String? = null,
    @SerializedName("photo_left") val photoLeft: String? = null,
    @SerializedName("photo_right") val photoRight: String? = null,
    @SerializedName("photo_front") val photoFront: String? = null,
    @SerializedName("photo_back") val photoBack: String? = null,
    @SerializedName("photo_top") val photoTop: String? = null,
    @SerializedName("photo_face") val photoFace: String? = null,
    @SerializedName("health_status") val healthStatus: String = "healthy",
    val status: String,
    @SerializedName("market_value") val marketValue: Double? = null,
    @SerializedName("insured_value") val insuredValue: Double? = null,
    @SerializedName("created_at") val createdAt: String,
)

data class GoatCreateDto(
    @SerializedName("farmer_id") val farmerId: Int,
    val breed: String,
    @SerializedName("age_months") val ageMonths: Int? = null,
    val gender: String,
    @SerializedName("weight_kg") val weightKg: Double? = null,
    val color: String? = null,
    @SerializedName("identification_marks") val identificationMarks: String? = null,
)

data class GoatUpdateDto(
    val breed: String? = null,
    @SerializedName("weight_kg") val weightKg: Double? = null,
    val status: String? = null,
)

data class GoatPhotosDto(
    @SerializedName("photo_left") val photoLeft: String? = null,
    @SerializedName("photo_right") val photoRight: String? = null,
    @SerializedName("photo_front") val photoFront: String? = null,
    @SerializedName("photo_back") val photoBack: String? = null,
    @SerializedName("photo_top") val photoTop: String? = null,
    @SerializedName("photo_face") val photoFace: String? = null,
)

data class QrTagDto(
    @SerializedName("goat_id") val goatId: Int,
    @SerializedName("qr_code") val qrCode: String,
    @SerializedName("ear_tag_number") val earTagNumber: String? = null,
)

// ──────────────────────────────────────────────
// Enrollment DTOs
// ──────────────────────────────────────────────

data class EnrollmentDto(
    val id: Int,
    @SerializedName("enrollment_number") val enrollmentNumber: String,
    @SerializedName("farmer_id") val farmerId: Int? = null,
    @SerializedName("goat_id") val goatId: Int? = null,
    @SerializedName("policy_id") val policyId: Int? = null,
    @SerializedName("current_step") val currentStep: Int,
    val status: String,
    @SerializedName("is_synced") val isSynced: Boolean = true,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class EnrollmentCreateDto(
    @SerializedName("offline_id") val offlineId: String? = null,
)

data class EnrollmentStepDto(
    val step: Int,
    val data: Map<String, Any?>,
)

data class EnrollmentFinalizeData(
    val enrollment: EnrollmentDto,
    @SerializedName("policy_number") val policyNumber: String,
    @SerializedName("policy_id") val policyId: Int,
)

// ──────────────────────────────────────────────
// Vaccination DTOs
// ──────────────────────────────────────────────

data class VaccinationDto(
    val id: Int,
    @SerializedName("goat_id") val goatId: Int,
    @SerializedName("farmer_id") val farmerId: Int,
    @SerializedName("vaccine_type") val vaccineType: String,
    @SerializedName("vaccine_name") val vaccineName: String? = null,
    @SerializedName("dose_number") val doseNumber: Int = 1,
    @SerializedName("vaccination_date") val vaccinationDate: String? = null,
    @SerializedName("next_due_date") val nextDueDate: String? = null,
    val status: String,
    @SerializedName("certificate_url") val certificateUrl: String? = null,
    @SerializedName("created_at") val createdAt: String,
)

data class VaccinationCreateDto(
    @SerializedName("goat_id") val goatId: Int,
    @SerializedName("farmer_id") val farmerId: Int,
    @SerializedName("vaccine_type") val vaccineType: String,
    @SerializedName("vaccination_date") val vaccinationDate: String? = null,
    @SerializedName("next_due_date") val nextDueDate: String? = null,
    val status: String = "completed",
)

data class VaccinationUpdateDto(
    val status: String? = null,
    @SerializedName("vaccination_date") val vaccinationDate: String? = null,
    @SerializedName("next_due_date") val nextDueDate: String? = null,
)

data class BulkVaccinationDto(
    @SerializedName("goat_id") val goatId: Int,
    @SerializedName("farmer_id") val farmerId: Int,
    val vaccinations: List<VaccinationCreateDto>,
)

// ──────────────────────────────────────────────
// Claims DTOs
// ──────────────────────────────────────────────

data class ClaimDto(
    val id: Int,
    @SerializedName("claim_number") val claimNumber: String,
    @SerializedName("policy_id") val policyId: Int,
    @SerializedName("goat_id") val goatId: Int,
    @SerializedName("farmer_id") val farmerId: Int,
    @SerializedName("death_date") val deathDate: String,
    @SerializedName("death_cause") val deathCause: String? = null,
    @SerializedName("death_description") val deathDescription: String? = null,
    @SerializedName("carcass_verified") val carcassVerified: Boolean = false,
    @SerializedName("ai_assessment_score") val aiAssessmentScore: Double? = null,
    val status: String,
    @SerializedName("claim_amount") val claimAmount: Double? = null,
    @SerializedName("approved_amount") val approvedAmount: Double? = null,
    @SerializedName("review_remarks") val reviewRemarks: String? = null,
    @SerializedName("created_at") val createdAt: String,
)

data class ClaimCreateDto(
    @SerializedName("policy_id") val policyId: Int,
    @SerializedName("goat_id") val goatId: Int,
    @SerializedName("farmer_id") val farmerId: Int,
    @SerializedName("death_date") val deathDate: String,
    @SerializedName("death_cause") val deathCause: String? = null,
    @SerializedName("death_description") val deathDescription: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("claim_amount") val claimAmount: Double? = null,
)

data class ClaimVerifyDto(
    @SerializedName("carcass_verified") val carcassVerified: Boolean,
    @SerializedName("verification_remarks") val verificationRemarks: String? = null,
)

data class ClaimReviewDto(
    val status: String,
    @SerializedName("review_remarks") val reviewRemarks: String? = null,
    @SerializedName("approved_amount") val approvedAmount: Double? = null,
)

data class ClaimSettleDto(
    @SerializedName("approved_amount") val approvedAmount: Double,
    @SerializedName("payment_mode") val paymentMode: String,
    @SerializedName("settlement_reference") val settlementReference: String? = null,
)

// ──────────────────────────────────────────────
// Dashboard & Reports DTOs
// ──────────────────────────────────────────────

data class DashboardStatsDto(
    @SerializedName("total_enrollments") val totalEnrollments: Int = 0,
    @SerializedName("total_premium_collected") val totalPremiumCollected: Double = 0.0,
    @SerializedName("total_claims") val totalClaims: Int = 0,
    @SerializedName("pending_claims") val pendingClaims: Int = 0,
    @SerializedName("vaccination_due") val vaccinationDue: Int = 0,
    @SerializedName("death_reports") val deathReports: Int = 0,
    @SerializedName("pending_approvals") val pendingApprovals: Int = 0,
    @SerializedName("active_policies") val activePolicies: Int = 0,
    @SerializedName("total_farmers") val totalFarmers: Int = 0,
    @SerializedName("total_goats") val totalGoats: Int = 0,
)

data class ReportDto(
    val id: Int,
    val title: String,
    @SerializedName("report_type") val reportType: String,
    val category: String,
    val format: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val summary: String? = null,
    val data: Map<String, Any?>? = null,
    @SerializedName("file_url") val fileUrl: String? = null,
    @SerializedName("generated_at") val generatedAt: String,
)

data class ReportCreateDto(
    val title: String,
    @SerializedName("report_type") val reportType: String,
    val category: String,
    val format: String = "pdf",
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
)

// ──────────────────────────────────────────────
// Notification DTOs
// ──────────────────────────────────────────────

data class NotificationsData(
    val notifications: List<NotificationDto>,
    @SerializedName("unread_count") val unreadCount: Int,
)

data class NotificationDto(
    val id: Int,
    val title: String,
    val body: String,
    @SerializedName("notification_type") val notificationType: String,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
)

data class MarkReadDto(
    @SerializedName("notification_ids") val notificationIds: List<Int>,
)
