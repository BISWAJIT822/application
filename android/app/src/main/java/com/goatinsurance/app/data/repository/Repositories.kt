package com.goatinsurance.app.data.repository

import com.goatinsurance.app.data.local.dao.*
import com.goatinsurance.app.data.local.entity.*
import com.goatinsurance.app.data.remote.api.*
import com.goatinsurance.app.data.remote.dto.*
import com.goatinsurance.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Helper response handler
private inline fun <T> handleResponse(call: () -> Response<ApiResponse<T>>): Result<T> {
    return try {
        val response = call()
        val body = response.body()
        if (response.isSuccessful && body != null && body.success) {
            Result.success(body.data ?: throw Exception("Null response data"))
        } else {
            Result.failure(Exception(body?.message ?: response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ──────────────────────────────────────────────
// Auth Repository
// ──────────────────────────────────────────────

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    suspend fun sendOtp(phone: String): Result<OtpResponseData> {
        return handleResponse { authApi.sendOtp(SendOtpRequest(phone)) }
    }

    suspend fun verifyOtp(phone: String, otp: String, role: String?, fcmToken: String?): Result<TokenResponseData> {
        val result = handleResponse { authApi.verifyOtp(VerifyOtpRequest(phone, otp, role, fcmToken)) }
        result.onSuccess { data ->
            sessionManager.saveTokens(data.accessToken, data.refreshToken)
            val user = data.user
            sessionManager.saveUserInfo(user.id, user.name, user.phone, user.role)
            userDao.insertUser(
                UserEntity(
                    id = user.id,
                    phone = user.phone,
                    name = user.name,
                    role = user.role,
                    email = user.email,
                    avatarUrl = user.avatarUrl,
                    isActive = user.isActive,
                    isVerified = user.isVerified,
                    district = user.district,
                    block = user.block,
                    village = user.village,
                    language = user.language,
                    lastLogin = user.lastLogin,
                    createdAt = user.createdAt
                )
            )
        }
        return result
    }

    suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Exception) {
            // Best effort
        }
        sessionManager.clearSession()
        userDao.clearUser()
    }

    val currentUser: Flow<UserEntity?> = userDao.getUser()
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    fun getUserRole(): String = sessionManager.getUserRole()
}

// ──────────────────────────────────────────────
// Farmer Repository
// ──────────────────────────────────────────────

@Singleton
class FarmerRepository @Inject constructor(
    private val farmersApi: FarmersApi,
    private val farmerDao: FarmerDao
) {
    val allFarmers: Flow<List<FarmerEntity>> = farmerDao.getAllFarmers()

    suspend fun fetchFarmers(search: String? = null): Result<List<FarmerDto>> {
        val result = handleResponse { farmersApi.listFarmers(search = search) }
        result.onSuccess { paginated ->
            val entities = paginated.items.map { dto ->
                FarmerEntity(
                    remoteId = dto.id,
                    name = dto.name,
                    phone = dto.phone,
                    alternatePhone = dto.alternatePhone,
                    aadhaarNumber = dto.aadhaarNumber,
                    photoUrl = dto.photoUrl,
                    village = dto.village,
                    block = dto.block,
                    district = dto.district,
                    state = dto.state,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    totalGoats = dto.totalGoats,
                    isActive = dto.isActive,
                    createdAt = dto.createdAt,
                    isSynced = true
                )
            }
            farmerDao.insertFarmers(entities)
        }
        return result.map { it.items }
    }

    suspend fun createFarmer(dto: FarmerCreateDto): Result<FarmerEntity> {
        // Offline-first: save locally first
        val localEntity = FarmerEntity(
            name = dto.name,
            phone = dto.phone,
            alternatePhone = dto.alternatePhone,
            aadhaarNumber = dto.aadhaarNumber,
            photoUrl = dto.photoUrl,
            village = dto.village,
            block = dto.block,
            district = dto.district,
            state = "Bihar",
            latitude = dto.latitude,
            longitude = dto.longitude,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            isSynced = false
        )
        val localId = farmerDao.insertFarmer(localEntity).toInt()
        val insertedLocal = localEntity.copy(id = localId)

        // Try syncing online
        val result = handleResponse { farmersApi.createFarmer(dto) }
        if (result.isSuccess) {
            val remote = result.getOrThrow()
            val updated = insertedLocal.copy(remoteId = remote.id, isSynced = true)
            farmerDao.updateFarmer(updated)
            return Result.success(updated)
        }
        return Result.success(insertedLocal)
    }

    suspend fun syncUnsynced() {
        val unsynced = farmerDao.getUnsyncedFarmers()
        for (farmer in unsynced) {
            val dto = FarmerCreateDto(
                name = farmer.name,
                phone = farmer.phone,
                alternatePhone = farmer.alternatePhone,
                aadhaarNumber = farmer.aadhaarNumber,
                village = farmer.village,
                block = farmer.block,
                district = farmer.district,
                photoUrl = farmer.photoUrl,
                latitude = farmer.latitude,
                longitude = farmer.longitude
            )
            val result = handleResponse { farmersApi.createFarmer(dto) }
            if (result.isSuccess) {
                val remote = result.getOrThrow()
                farmerDao.updateFarmer(farmer.copy(remoteId = remote.id, isSynced = true))
            }
        }
    }
}

// ──────────────────────────────────────────────
// Goat Repository
// ──────────────────────────────────────────────

@Singleton
class GoatRepository @Inject constructor(
    private val goatsApi: GoatsApi,
    private val goatDao: GoatDao
) {
    val allGoats: Flow<List<GoatEntity>> = goatDao.getAllGoats()

    fun getGoatsByFarmer(farmerId: Int): Flow<List<GoatEntity>> = goatDao.getGoatsByFarmer(farmerId)

    suspend fun fetchGoats(farmerId: Int? = null): Result<List<GoatDto>> {
        val result = handleResponse { goatsApi.listGoats(farmerId = farmerId) }
        result.onSuccess { paginated ->
            val entities = paginated.items.map { dto ->
                GoatEntity(
                    remoteId = dto.id,
                    farmerId = dto.farmerId,
                    tagNumber = dto.tagNumber,
                    qrCode = dto.qrCode,
                    earTagNumber = dto.earTagNumber,
                    breed = dto.breed,
                    ageMonths = dto.ageMonths,
                    gender = dto.gender,
                    weightKg = dto.weightKg,
                    color = dto.color,
                    identificationMarks = dto.identificationMarks,
                    photoLeft = dto.photoLeft,
                    photoRight = dto.photoRight,
                    photoFront = dto.photoFront,
                    photoBack = dto.photoBack,
                    photoTop = dto.photoTop,
                    photoFace = dto.photoFace,
                    healthStatus = dto.healthStatus,
                    status = dto.status,
                    marketValue = dto.marketValue,
                    insuredValue = dto.insuredValue,
                    createdAt = dto.createdAt,
                    isSynced = true
                )
            }
            goatDao.insertGoats(entities)
        }
        return result.map { it.items }
    }

    suspend fun createGoat(dto: GoatCreateDto): Result<GoatEntity> {
        val localEntity = GoatEntity(
            farmerId = dto.farmerId,
            breed = dto.breed,
            ageMonths = dto.ageMonths,
            gender = dto.gender,
            weightKg = dto.weightKg,
            color = dto.color,
            identificationMarks = dto.identificationMarks,
            status = "active",
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            isSynced = false
        )
        val localId = goatDao.insertGoat(localEntity).toInt()
        val insertedLocal = localEntity.copy(id = localId)

        val result = handleResponse { goatsApi.createGoat(dto) }
        if (result.isSuccess) {
            val remote = result.getOrThrow()
            val updated = insertedLocal.copy(remoteId = remote.id, isSynced = true)
            goatDao.updateGoat(updated)
            return Result.success(updated)
        }
        return Result.success(insertedLocal)
    }

    suspend fun updateGoatPhotos(id: Int, photos: GoatPhotosDto): Result<Unit> {
        val local = goatDao.getGoatById(id)
        if (local != null) {
            val updatedLocal = local.copy(
                photoLeft = photos.photoLeft ?: local.photoLeft,
                photoRight = photos.photoRight ?: local.photoRight,
                photoFront = photos.photoFront ?: local.photoFront,
                photoBack = photos.photoBack ?: local.photoBack,
                photoTop = photos.photoTop ?: local.photoTop,
                photoFace = photos.photoFace ?: local.photoFace,
                isSynced = false
            )
            goatDao.updateGoat(updatedLocal)

            if (local.remoteId != null) {
                val result = handleResponse { goatsApi.updateGoatPhotos(local.remoteId, photos) }
                if (result.isSuccess) {
                    goatDao.updateGoat(updatedLocal.copy(isSynced = true))
                    return Result.success(Unit)
                }
            }
        }
        return Result.success(Unit)
    }

    suspend fun assignQrTag(goatId: Int, qrCode: String, earTagNumber: String?): Result<Unit> {
        val local = goatDao.getGoatById(goatId)
        if (local != null) {
            val updatedLocal = local.copy(
                qrCode = qrCode,
                earTagNumber = earTagNumber,
                isSynced = false
            )
            goatDao.updateGoat(updatedLocal)

            if (local.remoteId != null) {
                val result = handleResponse { goatsApi.assignQrTag(QrTagDto(local.remoteId, qrCode, earTagNumber)) }
                if (result.isSuccess) {
                    goatDao.updateGoat(updatedLocal.copy(isSynced = true))
                    return Result.success(Unit)
                }
            }
        }
        return Result.success(Unit)
    }

    suspend fun syncUnsynced() {
        val unsynced = goatDao.getUnsyncedGoats()
        for (goat in unsynced) {
            if (goat.remoteId == null) {
                val dto = GoatCreateDto(
                    farmerId = goat.farmerId,
                    breed = goat.breed,
                    ageMonths = goat.ageMonths,
                    gender = goat.gender,
                    weightKg = goat.weightKg,
                    color = goat.color,
                    identificationMarks = goat.identificationMarks
                )
                val result = handleResponse { goatsApi.createGoat(dto) }
                if (result.isSuccess) {
                    val remote = result.getOrThrow()
                    val syncedGoat = goat.copy(remoteId = remote.id, isSynced = true)
                    goatDao.updateGoat(syncedGoat)

                    // Also sync photos if present
                    if (goat.photoLeft != null || goat.photoRight != null || goat.photoFront != null) {
                        goatsApi.updateGoatPhotos(
                            remote.id, GoatPhotosDto(
                                photoLeft = goat.photoLeft,
                                photoRight = goat.photoRight,
                                photoFront = goat.photoFront,
                                photoBack = goat.photoBack,
                                photoTop = goat.photoTop,
                                photoFace = goat.photoFace
                            )
                        )
                    }
                }
            } else {
                // If it has remote ID but unsynced photos or tag
                goatsApi.updateGoatPhotos(
                    goat.remoteId, GoatPhotosDto(
                        photoLeft = goat.photoLeft,
                        photoRight = goat.photoRight,
                        photoFront = goat.photoFront,
                        photoBack = goat.photoBack,
                        photoTop = goat.photoTop,
                        photoFace = goat.photoFace
                    )
                )
                if (goat.qrCode != null) {
                    goatsApi.assignQrTag(QrTagDto(goat.remoteId, goat.qrCode, goat.earTagNumber))
                }
                goatDao.updateGoat(goat.copy(isSynced = true))
            }
        }
    }
}

// ──────────────────────────────────────────────
// Enrollment Repository
// ──────────────────────────────────────────────

@Singleton
class EnrollmentRepository @Inject constructor(
    private val enrollmentsApi: EnrollmentsApi,
    private val enrollmentDao: EnrollmentDao
) {
    val allEnrollments: Flow<List<EnrollmentEntity>> = enrollmentDao.getAllEnrollments()

    suspend fun fetchEnrollments(): Result<List<EnrollmentDto>> {
        val result = handleResponse { enrollmentsApi.listEnrollments() }
        result.onSuccess { paginated ->
            val entities = paginated.items.map { dto ->
                EnrollmentEntity(
                    id = dto.id.toString(),
                    remoteId = dto.id,
                    enrollmentNumber = dto.enrollmentNumber,
                    farmerId = dto.farmerId,
                    goatId = dto.goatId,
                    policyId = dto.policyId,
                    currentStep = dto.currentStep,
                    status = dto.status,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt,
                    isSynced = true
                )
            }
            enrollmentDao.insertEnrollments(entities)
        }
        return result.map { it.items }
    }

    suspend fun createEnrollmentDraft(): Result<EnrollmentEntity> {
        val offlineId = UUID.randomUUID().toString()
        val local = EnrollmentEntity(
            id = offlineId,
            currentStep = 1,
            status = "draft",
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            isSynced = false
        )
        enrollmentDao.insertEnrollment(local)

        val result = handleResponse { enrollmentsApi.createEnrollment(EnrollmentCreateDto(offlineId = offlineId)) }
        if (result.isSuccess) {
            val remote = result.getOrThrow()
            val updated = local.copy(remoteId = remote.id, enrollmentNumber = remote.enrollmentNumber, isSynced = true)
            enrollmentDao.insertEnrollment(updated)
            return Result.success(updated)
        }
        return Result.success(local)
    }

    suspend fun saveStepData(id: String, step: Int, stepData: Map<String, Any?>): Result<Unit> {
        val local = enrollmentDao.getEnrollmentById(id)
        if (local != null) {
            // Save JSON representation locally
            val gson = com.google.gson.Gson()
            val updatedLocal = local.copy(
                currentStep = step,
                stepDataJson = gson.toJson(stepData),
                updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                isSynced = false
            )
            enrollmentDao.insertEnrollment(updatedLocal)

            if (local.remoteId != null) {
                val result = handleResponse { enrollmentsApi.updateStep(local.remoteId, EnrollmentStepDto(step, stepData)) }
                if (result.isSuccess) {
                    enrollmentDao.insertEnrollment(updatedLocal.copy(isSynced = true))
                }
            }
        }
        return Result.success(Unit)
    }

    suspend fun finalizeEnrollment(id: String): Result<EnrollmentFinalizeData> {
        val local = enrollmentDao.getEnrollmentById(id) ?: return Result.failure(Exception("Local draft not found"))
        if (local.remoteId != null) {
            val result = handleResponse { enrollmentsApi.finalizeEnrollment(local.remoteId) }
            result.onSuccess { data ->
                enrollmentDao.insertEnrollment(
                    local.copy(
                        status = "completed",
                        policyId = data.policyId,
                        isSynced = true
                    )
                )
            }
            return result
        }
        return Result.failure(Exception("Cannot finalize offline enrollment. Please connect to internet."))
    }

    suspend fun syncUnsynced() {
        val unsynced = enrollmentDao.getUnsyncedEnrollments()
        for (enrollment in unsynced) {
            if (enrollment.remoteId == null) {
                val result = handleResponse { enrollmentsApi.createEnrollment(EnrollmentCreateDto(offlineId = enrollment.id)) }
                if (result.isSuccess) {
                    val remote = result.getOrThrow()
                    enrollmentDao.insertEnrollment(
                        enrollment.copy(
                            remoteId = remote.id,
                            enrollmentNumber = remote.enrollmentNumber,
                            isSynced = true
                        )
                    )
                }
            } else {
                // If it has remote ID but has un-synced stepData
                val stepDataJson = enrollment.stepDataJson
                if (stepDataJson != null) {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<Map<String, Any?>>() {}.type
                    val map: Map<String, Any?> = gson.fromJson(stepDataJson, type)

                    val result = handleResponse { enrollmentsApi.updateStep(enrollment.remoteId, EnrollmentStepDto(enrollment.currentStep, map)) }
                    if (result.isSuccess) {
                        enrollmentDao.insertEnrollment(enrollment.copy(isSynced = true))
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// Vaccination Repository
// ──────────────────────────────────────────────

@Singleton
class VaccinationRepository @Inject constructor(
    private val api: VaccinationsApi,
    private val dao: VaccinationDao
) {
    val allVaccinations: Flow<List<VaccinationEntity>> = dao.getAllVaccinations()

    fun getVaccinationsForGoat(goatId: Int): Flow<List<VaccinationEntity>> = dao.getVaccinationsForGoat(goatId)

    suspend fun fetchUpcomingVaccinations(days: Int = 30): Result<List<VaccinationDto>> {
        return handleResponse { api.getUpcoming(days) }
    }

    suspend fun fetchVaccinations(): Result<List<VaccinationDto>> {
        val result = handleResponse { api.listVaccinations() }
        result.onSuccess { paginated ->
            val entities = paginated.items.map { dto ->
                VaccinationEntity(
                    remoteId = dto.id,
                    goatId = dto.goatId,
                    farmerId = dto.farmerId,
                    vaccineType = dto.vaccineType,
                    vaccineName = dto.vaccineName,
                    doseNumber = dto.doseNumber,
                    vaccinationDate = dto.vaccinationDate,
                    nextDueDate = dto.nextDueDate,
                    status = dto.status,
                    certificateUrl = dto.certificateUrl,
                    createdAt = dto.createdAt,
                    isSynced = true
                )
            }
            dao.insertVaccinations(entities)
        }
        return result.map { it.items }
    }

    suspend fun recordVaccination(dto: VaccinationCreateDto): Result<VaccinationEntity> {
        val local = VaccinationEntity(
            goatId = dto.goatId,
            farmerId = dto.farmerId,
            vaccineType = dto.vaccineType,
            vaccineName = dto.vaccineType,
            vaccinationDate = dto.vaccinationDate,
            nextDueDate = dto.nextDueDate,
            status = dto.status,
            certificateUrl = null,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            isSynced = false
        )
        val id = dao.insertVaccination(local).toInt()
        val insertedLocal = local.copy(id = id)

        val result = handleResponse { api.recordVaccination(dto) }
        if (result.isSuccess) {
            val remote = result.getOrThrow()
            val updated = insertedLocal.copy(remoteId = remote.id, isSynced = true)
            dao.insertVaccination(updated)
            return Result.success(updated)
        }
        return Result.success(insertedLocal)
    }

    suspend fun syncUnsynced() {
        val unsynced = dao.getUnsyncedVaccinations()
        for (v in unsynced) {
            val dto = VaccinationCreateDto(
                goatId = v.goatId,
                farmerId = v.farmerId,
                vaccineType = v.vaccineType,
                vaccinationDate = v.vaccinationDate,
                nextDueDate = v.nextDueDate,
                status = v.status
            )
            val result = handleResponse { api.recordVaccination(dto) }
            if (result.isSuccess) {
                val remote = result.getOrThrow()
                dao.insertVaccination(v.copy(remoteId = remote.id, isSynced = true))
            }
        }
    }
}

// ──────────────────────────────────────────────
// Claim Repository
// ──────────────────────────────────────────────

@Singleton
class ClaimRepository @Inject constructor(
    private val api: ClaimsApi,
    private val dao: ClaimDao
) {
    val allClaims: Flow<List<ClaimEntity>> = dao.getAllClaims()

    suspend fun fetchClaims(): Result<List<ClaimDto>> {
        val result = handleResponse { api.listClaims() }
        result.onSuccess { paginated ->
            val entities = paginated.items.map { dto ->
                ClaimEntity(
                    remoteId = dto.id,
                    claimNumber = dto.claimNumber,
                    policyId = dto.policyId,
                    goatId = dto.goatId,
                    farmerId = dto.farmerId,
                    deathDate = dto.deathDate,
                    deathCause = dto.deathCause,
                    deathDescription = dto.deathDescription,
                    carcassVerified = dto.carcassVerified,
                    aiAssessmentScore = dto.aiAssessmentScore,
                    status = dto.status,
                    claimAmount = dto.claimAmount,
                    approvedAmount = dto.approvedAmount,
                    reviewRemarks = dto.reviewRemarks,
                    createdAt = dto.createdAt,
                    isSynced = true
                )
            }
            dao.insertClaims(entities)
        }
        return result.map { it.items }
    }

    suspend fun fileClaim(dto: ClaimCreateDto): Result<ClaimEntity> {
        val local = ClaimEntity(
            policyId = dto.policyId,
            goatId = dto.goatId,
            farmerId = dto.farmerId,
            deathDate = dto.deathDate,
            deathCause = dto.deathCause,
            deathDescription = dto.deathDescription,
            carcassVerified = false,
            aiAssessmentScore = null,
            status = "submitted",
            claimAmount = dto.claimAmount,
            approvedAmount = null,
            reviewRemarks = null,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            isSynced = false
        )
        val id = dao.insertClaim(local).toInt()
        val insertedLocal = local.copy(id = id)

        val result = handleResponse { api.fileClaim(dto) }
        if (result.isSuccess) {
            val remote = result.getOrThrow()
            val updated = insertedLocal.copy(remoteId = remote.id, claimNumber = remote.claimNumber, isSynced = true)
            dao.updateClaim(updated)
            return Result.success(updated)
        }
        return Result.success(insertedLocal)
    }

    suspend fun reviewClaim(id: Int, status: String, remarks: String?, approvedAmount: Double?): Result<ClaimEntity> {
        val local = dao.getClaimById(id) ?: return Result.failure(Exception("Claim not found locally"))
        if (local.remoteId != null) {
            val result = handleResponse { api.reviewClaim(local.remoteId, ClaimReviewDto(status, remarks, approvedAmount)) }
            if (result.isSuccess) {
                val remote = result.getOrThrow()
                val updated = local.copy(
                    status = remote.status,
                    reviewRemarks = remote.reviewRemarks,
                    approvedAmount = remote.approvedAmount,
                    isSynced = true
                )
                dao.updateClaim(updated)
                return Result.success(updated)
            }
        }
        return Result.failure(Exception("Cannot review claim offline."))
    }

    suspend fun syncUnsynced() {
        val unsynced = dao.getUnsyncedClaims()
        for (c in unsynced) {
            val dto = ClaimCreateDto(
                policyId = c.policyId,
                goatId = c.goatId,
                farmerId = c.farmerId,
                deathDate = c.deathDate,
                deathCause = c.deathCause,
                deathDescription = c.deathDescription,
                claimAmount = c.claimAmount
            )
            val result = handleResponse { api.fileClaim(dto) }
            if (result.isSuccess) {
                val remote = result.getOrThrow()
                dao.updateClaim(c.copy(remoteId = remote.id, claimNumber = remote.claimNumber, isSynced = true))
            }
        }
    }
}

// ──────────────────────────────────────────────
// Report Repository
// ──────────────────────────────────────────────

@Singleton
class ReportRepository @Inject constructor(
    private val reportsApi: ReportsApi
) {
    suspend fun getDashboardStats(): Result<DashboardStatsDto> {
        return handleResponse { reportsApi.getDashboardStats() }
    }

    suspend fun generateReport(title: String, type: String, category: String, start: String, end: String): Result<ReportDto> {
        return handleResponse { reportsApi.generateReport(ReportCreateDto(title, type, category, "pdf", start, end)) }
    }

    suspend fun fetchReports(category: String? = null): Result<List<ReportDto>> {
        return handleResponse { reportsApi.listReports(category) }
    }
}

// ──────────────────────────────────────────────
// Notification Repository
// ──────────────────────────────────────────────

@Singleton
class NotificationRepository @Inject constructor(
    private val api: NotificationsApi,
    private val dao: NotificationDao
) {
    val notifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val unreadCount: Flow<Int> = dao.getUnreadCount()

    suspend fun fetchNotifications(): Result<NotificationsData> {
        val result = handleResponse { api.getNotifications() }
        result.onSuccess { data ->
            val entities = data.notifications.map { dto ->
                NotificationEntity(
                    id = dto.id,
                    title = dto.title,
                    body = dto.body,
                    notificationType = dto.notificationType,
                    isRead = dto.isRead,
                    createdAt = dto.createdAt
                )
            }
            dao.clearAll()
            dao.insertNotifications(entities)
        }
        return result
    }

    suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
        handleResponse { api.markRead(MarkReadDto(listOf(id))) }
    }

    suspend fun markAllAsRead() {
        dao.markAllAsRead()
        handleResponse { api.markAllRead() }
    }
}

// ──────────────────────────────────────────────
// Upload Repository
// ──────────────────────────────────────────────

@Singleton
class UploadRepository @Inject constructor(
    private val uploadApi: UploadApi
) {
    suspend fun uploadPhoto(filePart: MultipartBody.Part, category: String = "photos"): Result<FileUploadData> {
        return handleResponse { uploadApi.uploadFile(filePart, category) }
    }
}
