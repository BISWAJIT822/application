package com.goatinsurance.app.data.remote.api

import com.goatinsurance.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Auth API endpoints.
 */
interface AuthApi {
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<ApiResponse<OtpResponseData>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<TokenResponseData>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<TokenRefreshData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Nothing>>
}

/**
 * Farmers API endpoints.
 */
interface FarmersApi {
    @POST("farmers/")
    suspend fun createFarmer(@Body farmer: FarmerCreateDto): Response<ApiResponse<FarmerDto>>

    @GET("farmers/")
    suspend fun listFarmers(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("sort_order") sortOrder: String? = "desc",
        @Query("status") status: String? = null,
        @Query("village") village: String? = null,
    ): Response<ApiResponse<PaginatedData<FarmerDto>>>

    @GET("farmers/{id}")
    suspend fun getFarmer(@Path("id") id: Int): Response<ApiResponse<FarmerDto>>

    @PUT("farmers/{id}")
    suspend fun updateFarmer(@Path("id") id: Int, @Body data: FarmerCreateDto): Response<ApiResponse<FarmerDto>>

    @DELETE("farmers/{id}")
    suspend fun deleteFarmer(@Path("id") id: Int): Response<ApiResponse<Nothing>>
}

/**
 * Goats API endpoints.
 */
interface GoatsApi {
    @POST("goats/")
    suspend fun createGoat(@Body goat: GoatCreateDto): Response<ApiResponse<GoatDto>>

    @GET("goats/")
    suspend fun listGoats(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("farmer_id") farmerId: Int? = null,
        @Query("status") status: String? = null,
        @Query("breed") breed: String? = null,
    ): Response<ApiResponse<PaginatedData<GoatDto>>>

    @GET("goats/{id}")
    suspend fun getGoat(@Path("id") id: Int): Response<ApiResponse<GoatDto>>

    @PUT("goats/{id}")
    suspend fun updateGoat(@Path("id") id: Int, @Body data: GoatUpdateDto): Response<ApiResponse<GoatDto>>

    @PUT("goats/{id}/photos")
    suspend fun updateGoatPhotos(@Path("id") id: Int, @Body photos: GoatPhotosDto): Response<ApiResponse<Nothing>>

    @POST("goats/assign-qr")
    suspend fun assignQrTag(@Body data: QrTagDto): Response<ApiResponse<Nothing>>

    @DELETE("goats/{id}")
    suspend fun deleteGoat(@Path("id") id: Int): Response<ApiResponse<Nothing>>
}

/**
 * Enrollments API endpoints.
 */
interface EnrollmentsApi {
    @POST("enrollments/")
    suspend fun createEnrollment(@Body data: EnrollmentCreateDto): Response<ApiResponse<EnrollmentDto>>

    @PUT("enrollments/{id}/step")
    suspend fun updateStep(
        @Path("id") id: Int,
        @Body data: EnrollmentStepDto
    ): Response<ApiResponse<EnrollmentDto>>

    @POST("enrollments/{id}/finalize")
    suspend fun finalizeEnrollment(@Path("id") id: Int): Response<ApiResponse<EnrollmentFinalizeData>>

    @GET("enrollments/")
    suspend fun listEnrollments(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("status") status: String? = null,
    ): Response<ApiResponse<PaginatedData<EnrollmentDto>>>

    @GET("enrollments/{id}")
    suspend fun getEnrollment(@Path("id") id: Int): Response<ApiResponse<EnrollmentDto>>
}

/**
 * Vaccinations API endpoints.
 */
interface VaccinationsApi {
    @POST("vaccinations/")
    suspend fun recordVaccination(@Body data: VaccinationCreateDto): Response<ApiResponse<VaccinationDto>>

    @POST("vaccinations/bulk")
    suspend fun recordBulkVaccination(@Body data: BulkVaccinationDto): Response<ApiResponse<List<VaccinationDto>>>

    @GET("vaccinations/")
    suspend fun listVaccinations(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("goat_id") goatId: Int? = null,
        @Query("status") status: String? = null,
    ): Response<ApiResponse<PaginatedData<VaccinationDto>>>

    @GET("vaccinations/upcoming")
    suspend fun getUpcoming(@Query("days") days: Int = 30): Response<ApiResponse<List<VaccinationDto>>>

    @GET("vaccinations/history/{goatId}")
    suspend fun getHistory(@Path("goatId") goatId: Int): Response<ApiResponse<List<VaccinationDto>>>

    @PUT("vaccinations/{id}")
    suspend fun updateVaccination(@Path("id") id: Int, @Body data: VaccinationUpdateDto): Response<ApiResponse<VaccinationDto>>
}

/**
 * Claims API endpoints.
 */
interface ClaimsApi {
    @POST("claims/")
    suspend fun fileClaim(@Body data: ClaimCreateDto): Response<ApiResponse<ClaimDto>>

    @GET("claims/")
    suspend fun listClaims(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("status") status: String? = null,
    ): Response<ApiResponse<PaginatedData<ClaimDto>>>

    @GET("claims/{id}")
    suspend fun getClaim(@Path("id") id: Int): Response<ApiResponse<ClaimDto>>

    @PUT("claims/{id}/verify")
    suspend fun verifyClaim(@Path("id") id: Int, @Body data: ClaimVerifyDto): Response<ApiResponse<ClaimDto>>

    @PUT("claims/{id}/review")
    suspend fun reviewClaim(@Path("id") id: Int, @Body data: ClaimReviewDto): Response<ApiResponse<ClaimDto>>

    @PUT("claims/{id}/settle")
    suspend fun settleClaim(@Path("id") id: Int, @Body data: ClaimSettleDto): Response<ApiResponse<ClaimDto>>
}

/**
 * Reports API endpoints.
 */
interface ReportsApi {
    @GET("reports/dashboard-stats")
    suspend fun getDashboardStats(): Response<ApiResponse<DashboardStatsDto>>

    @POST("reports/generate")
    suspend fun generateReport(@Body data: ReportCreateDto): Response<ApiResponse<ReportDto>>

    @GET("reports/")
    suspend fun listReports(
        @Query("category") category: String? = null,
    ): Response<ApiResponse<List<ReportDto>>>
}

/**
 * Profile API endpoints.
 */
interface ProfileApi {
    @GET("profile/")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    @PUT("profile/")
    suspend fun updateProfile(@Body data: UserUpdateDto): Response<ApiResponse<UserDto>>

    @PUT("profile/language")
    suspend fun changeLanguage(@Body data: LanguageDto): Response<ApiResponse<Nothing>>

    @PUT("profile/fcm-token")
    suspend fun updateFcmToken(@Query("fcm_token") token: String): Response<ApiResponse<Nothing>>
}

/**
 * Notifications API endpoints.
 */
interface NotificationsApi {
    @GET("notifications/")
    suspend fun getNotifications(
        @Query("unread_only") unreadOnly: Boolean = false,
        @Query("limit") limit: Int = 50,
    ): Response<ApiResponse<NotificationsData>>

    @PUT("notifications/mark-read")
    suspend fun markRead(@Body data: MarkReadDto): Response<ApiResponse<Nothing>>

    @PUT("notifications/mark-all-read")
    suspend fun markAllRead(): Response<ApiResponse<Nothing>>
}

/**
 * File upload API.
 */
interface UploadApi {
    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Query("category") category: String = "photos",
    ): Response<ApiResponse<FileUploadData>>
}
