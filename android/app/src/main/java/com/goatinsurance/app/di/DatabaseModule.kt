package com.goatinsurance.app.di

import android.content.Context
import com.goatinsurance.app.data.local.AppDatabase
import com.goatinsurance.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideFarmerDao(db: AppDatabase): FarmerDao = db.farmerDao()

    @Provides
    fun provideGoatDao(db: AppDatabase): GoatDao = db.goatDao()

    @Provides
    fun provideEnrollmentDao(db: AppDatabase): EnrollmentDao = db.enrollmentDao()

    @Provides
    fun provideVaccinationDao(db: AppDatabase): VaccinationDao = db.vaccinationDao()

    @Provides
    fun provideClaimDao(db: AppDatabase): ClaimDao = db.claimDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
}
