package com.goatinsurance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val phone: String,
    val name: String,
    val role: String,
    val email: String?,
    val avatarUrl: String?,
    val isActive: Boolean,
    val isVerified: Boolean,
    val district: String?,
    val block: String?,
    val village: String?,
    val language: String,
    val lastLogin: String?,
    val createdAt: String
)

@Entity(tableName = "farmers")
data class FarmerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null, // null if created offline and not synced yet
    val name: String,
    val phone: String,
    val alternatePhone: String?,
    val aadhaarNumber: String?,
    val photoUrl: String?,
    val village: String,
    val block: String?,
    val district: String?,
    val state: String?,
    val latitude: Double?,
    val longitude: Double?,
    val totalGoats: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String,
    val isSynced: Boolean = true
)

@Entity(tableName = "goats")
data class GoatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null,
    val farmerId: Int,
    val tagNumber: String?,
    val qrCode: String?,
    val earTagNumber: String?,
    val breed: String,
    val ageMonths: Int?,
    val gender: String,
    val weightKg: Double?,
    val color: String?,
    val identificationMarks: String?,
    val photoLeft: String?,
    val photoRight: String?,
    val photoFront: String?,
    val photoBack: String?,
    val photoTop: String?,
    val photoFace: String?,
    val healthStatus: String = "healthy",
    val status: String,
    val marketValue: Double?,
    val insuredValue: Double?,
    val createdAt: String,
    val isSynced: Boolean = true
)

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
    @PrimaryKey val id: String, // offline UUID or remote id string
    val remoteId: Int? = null,
    val enrollmentNumber: String?,
    val farmerId: Int?,
    val goatId: Int?,
    val policyId: Int?,
    val currentStep: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = true,
    // JSON serialized step details
    val stepDataJson: String? = null
)

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null,
    val goatId: Int,
    val farmerId: Int,
    val vaccineType: String,
    val vaccineName: String?,
    val doseNumber: Int = 1,
    val vaccinationDate: String?,
    val nextDueDate: String?,
    val status: String,
    val certificateUrl: String?,
    val createdAt: String,
    val isSynced: Boolean = true
)

@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null,
    val claimNumber: String?,
    val policyId: Int,
    val goatId: Int,
    val farmerId: Int,
    val deathDate: String,
    val deathCause: String?,
    val deathDescription: String?,
    val carcassVerified: Boolean = false,
    val aiAssessmentScore: Double?,
    val status: String,
    val claimAmount: Double?,
    val approvedAmount: Double?,
    val reviewRemarks: String?,
    val createdAt: String,
    val isSynced: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val notificationType: String,
    val isRead: Boolean,
    val createdAt: String
)
