package com.goatinsurance.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.goatinsurance.app.data.local.dao.*
import com.goatinsurance.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        FarmerEntity::class,
        GoatEntity::class,
        EnrollmentEntity::class,
        VaccinationEntity::class,
        ClaimEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun farmerDao(): FarmerDao
    abstract fun goatDao(): GoatDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun claimDao(): ClaimDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goat_insurance_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
