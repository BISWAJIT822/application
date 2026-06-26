package com.goatinsurance.app.data.local.dao

import androidx.room.*
import com.goatinsurance.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers ORDER BY createdAt DESC")
    fun getAllFarmers(): Flow<List<FarmerEntity>>

    @Query("SELECT * FROM farmers WHERE id = :id")
    suspend fun getFarmerById(id: Int): FarmerEntity?

    @Query("SELECT * FROM farmers WHERE remoteId = :remoteId")
    suspend fun getFarmerByRemoteId(remoteId: Int): FarmerEntity?

    @Query("SELECT * FROM farmers WHERE isSynced = 0")
    suspend fun getUnsyncedFarmers(): List<FarmerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmers(farmers: List<FarmerEntity>)

    @Update
    suspend fun updateFarmer(farmer: FarmerEntity)

    @Query("DELETE FROM farmers WHERE id = :id")
    suspend fun deleteFarmer(id: Int)

    @Query("DELETE FROM farmers")
    suspend fun clearAll()
}

@Dao
interface GoatDao {
    @Query("SELECT * FROM goats ORDER BY createdAt DESC")
    fun getAllGoats(): Flow<List<GoatEntity>>

    @Query("SELECT * FROM goats WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getGoatsByFarmer(farmerId: Int): Flow<List<GoatEntity>>

    @Query("SELECT * FROM goats WHERE id = :id")
    suspend fun getGoatById(id: Int): GoatEntity?

    @Query("SELECT * FROM goats WHERE remoteId = :remoteId")
    suspend fun getGoatByRemoteId(remoteId: Int): GoatEntity?

    @Query("SELECT * FROM goats WHERE isSynced = 0")
    suspend fun getUnsyncedGoats(): List<GoatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoat(goat: GoatEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoats(goats: List<GoatEntity>)

    @Update
    suspend fun updateGoat(goat: GoatEntity)

    @Query("DELETE FROM goats WHERE id = :id")
    suspend fun deleteGoat(id: Int)

    @Query("DELETE FROM goats")
    suspend fun clearAll()
}

@Dao
interface EnrollmentDao {
    @Query("SELECT * FROM enrollments ORDER BY updatedAt DESC")
    fun getAllEnrollments(): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE id = :id")
    suspend fun getEnrollmentById(id: String): EnrollmentEntity?

    @Query("SELECT * FROM enrollments WHERE isSynced = 0")
    suspend fun getUnsyncedEnrollments(): List<EnrollmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollments(enrollments: List<EnrollmentEntity>)

    @Query("DELETE FROM enrollments WHERE id = :id")
    suspend fun deleteEnrollment(id: String)

    @Query("DELETE FROM enrollments")
    suspend fun clearAll()
}

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations ORDER BY vaccinationDate DESC")
    fun getAllVaccinations(): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE goatId = :goatId ORDER BY vaccinationDate DESC")
    fun getVaccinationsForGoat(goatId: Int): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE isSynced = 0")
    suspend fun getUnsyncedVaccinations(): List<VaccinationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccinations(vaccinations: List<VaccinationEntity>)

    @Update
    suspend fun updateVaccination(vaccination: VaccinationEntity)

    @Query("DELETE FROM vaccinations")
    suspend fun clearAll()
}

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims ORDER BY createdAt DESC")
    fun getAllClaims(): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims WHERE isSynced = 0")
    suspend fun getUnsyncedClaims(): List<ClaimEntity>

    @Query("SELECT * FROM claims WHERE id = :id")
    suspend fun getClaimById(id: Int): ClaimEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaims(claims: List<ClaimEntity>)

    @Update
    suspend fun updateClaim(claim: ClaimEntity)

    @Query("DELETE FROM claims")
    suspend fun clearAll()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
