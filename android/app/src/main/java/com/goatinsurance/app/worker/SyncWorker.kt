package com.goatinsurance.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.goatinsurance.app.data.repository.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val farmerRepository: FarmerRepository,
    private val goatRepository: GoatRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val vaccinationRepository: VaccinationRepository,
    private val claimRepository: ClaimRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Sync in logical dependency order
            farmerRepository.syncUnsynced()
            goatRepository.syncUnsynced()
            enrollmentRepository.syncUnsynced()
            vaccinationRepository.syncUnsynced()
            claimRepository.syncUnsynced()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
