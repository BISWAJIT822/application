package com.goatinsurance.app.ui.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatinsurance.app.data.local.entity.EnrollmentEntity
import com.goatinsurance.app.data.repository.EnrollmentRepository
import com.goatinsurance.app.data.repository.FarmerRepository
import com.goatinsurance.app.data.repository.GoatRepository
import com.goatinsurance.app.data.remote.dto.FarmerCreateDto
import com.goatinsurance.app.data.remote.dto.GoatCreateDto
import com.goatinsurance.app.data.remote.dto.GoatPhotosDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val farmerRepository: FarmerRepository,
    private val goatRepository: GoatRepository
) : ViewModel() {

    private val _enrollments = MutableStateFlow<List<EnrollmentEntity>>(emptyList())
    val enrollments: StateFlow<List<EnrollmentEntity>> = _enrollments.asStateFlow()

    private val _currentEnrollment = MutableStateFlow<EnrollmentEntity?>(null)
    val currentEnrollment: StateFlow<EnrollmentEntity?> = _currentEnrollment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEnrollments()
    }

    fun loadEnrollments() {
        viewModelScope.launch {
            _isLoading.value = true
            enrollmentRepository.allEnrollments.collect { list ->
                _enrollments.value = list
                _isLoading.value = false
            }
        }
    }

    fun startNewEnrollment() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = enrollmentRepository.createEnrollmentDraft()
            result.onSuccess { draft ->
                _currentEnrollment.value = draft
            }.onFailure {
                // handle error
            }
            _isLoading.value = false
        }
    }

    fun saveStep(step: Int, stepData: Map<String, Any?>) {
        val current = _currentEnrollment.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            enrollmentRepository.saveStepData(current.id, step, stepData)
            _currentEnrollment.value = current.copy(currentStep = step)
            _isLoading.value = false
        }
    }

    fun finalizeCurrentEnrollment(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val current = _currentEnrollment.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = enrollmentRepository.finalizeEnrollment(current.id)
            result.onSuccess { data ->
                _currentEnrollment.value = null
                onSuccess(data.policyNumber)
            }.onFailure { error ->
                onError(error.message ?: "Failed to finalize enrollment")
            }
            _isLoading.value = false
        }
    }
}
