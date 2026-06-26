package com.goatinsurance.app.navigation

/**
 * All navigation routes in the application.
 */
sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")
    data object OtpVerification : Screen("otp_verification/{phone}") {
        fun createRoute(phone: String) = "otp_verification/$phone"
    }

    // Main
    data object Dashboard : Screen("dashboard")

    // Enrollment
    data object EnrollmentList : Screen("enrollment_list")
    data object NewEnrollment : Screen("new_enrollment")
    data object EnrollmentDetail : Screen("enrollment_detail/{enrollmentId}") {
        fun createRoute(id: Int) = "enrollment_detail/$id"
    }

    // Goats
    data object GoatList : Screen("goat_list")
    data object GoatDetail : Screen("goat_detail/{goatId}") {
        fun createRoute(id: Int) = "goat_detail/$id"
    }

    // Vaccination
    data object VaccinationList : Screen("vaccination_list")
    data object RecordVaccination : Screen("record_vaccination/{goatId}") {
        fun createRoute(id: Int) = "record_vaccination/$id"
    }

    // Claims
    data object ClaimsList : Screen("claims_list")
    data object FileClaim : Screen("file_claim/{policyId}") {
        fun createRoute(id: Int) = "file_claim/$id"
    }
    data object ClaimDetail : Screen("claim_detail/{claimId}") {
        fun createRoute(id: Int) = "claim_detail/$id"
    }
    data object ClaimReview : Screen("claim_review/{claimId}") {
        fun createRoute(id: Int) = "claim_review/$id"
    }

    // Reports
    data object Reports : Screen("reports")
    data object ReportDetail : Screen("report_detail/{reportId}") {
        fun createRoute(id: Int) = "report_detail/$id"
    }

    // Profile
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")

    // Admin
    data object AdminDashboard : Screen("admin_dashboard")
    data object ManageUsers : Screen("manage_users")
    data object AuditLogs : Screen("audit_logs")

    // AI Assistant
    data object Assistant : Screen("assistant")

    // Notifications
    data object Notifications : Screen("notifications")
}

/**
 * Bottom navigation items.
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val iconName: String
) {
    HOME("dashboard", "Home", "home"),
    ENROLLMENT("enrollment_list", "Enroll", "add_circle"),
    GOATS("goat_list", "Goats", "pets"),
    CLAIMS("claims_list", "Claims", "description"),
    PROFILE("profile", "Profile", "person"),
}
