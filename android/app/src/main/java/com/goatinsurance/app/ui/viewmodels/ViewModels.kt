package com.goatinsurance.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatinsurance.app.data.local.entity.*
import com.goatinsurance.app.data.remote.dto.*
import com.goatinsurance.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ──────────────────────────────────────────────
// Goat List / Detail ViewModel
// ──────────────────────────────────────────────

@HiltViewModel
class GoatListViewModel @Inject constructor(
    private val goatRepository: GoatRepository,
    private val farmerRepository: FarmerRepository
) : ViewModel() {

    private val _goats = MutableStateFlow<List<GoatEntity>>(emptyList())
    val goats: StateFlow<List<GoatEntity>> = _goats.asStateFlow()

    private val _currentGoat = MutableStateFlow<GoatEntity?>(null)
    val currentGoat: StateFlow<GoatEntity?> = _currentGoat.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadGoats()
    }

    fun loadGoats() {
        viewModelScope.launch {
            _isLoading.value = true
            goatRepository.fetchGoats()
            goatRepository.allGoats.collect { list ->
                _goats.value = list
                _isLoading.value = false
            }
        }
    }

    fun getGoatById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val goat = goatRepository.fetchGoats().getOrNull()?.find { it.id == id }
            if (goat != null) {
                // mock update local UI
                goatRepository.fetchGoats()
            }
            _isLoading.value = false
        }
    }
}

// ──────────────────────────────────────────────
// Vaccination ViewModel
// ──────────────────────────────────────────────

@HiltViewModel
class VaccinationViewModel @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val goatRepository: GoatRepository
) : ViewModel() {

    private val _vaccinations = MutableStateFlow<List<VaccinationEntity>>(emptyList())
    val vaccinations: StateFlow<List<VaccinationEntity>> = _vaccinations.asStateFlow()

    private val _upcomingVaccinations = MutableStateFlow<List<VaccinationDto>>(emptyList())
    val upcomingVaccinations: StateFlow<List<VaccinationDto>> = _upcomingVaccinations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadVaccinations()
        loadUpcoming()
    }

    fun loadVaccinations() {
        viewModelScope.launch {
            _isLoading.value = true
            vaccinationRepository.fetchVaccinations()
            vaccinationRepository.allVaccinations.collect { list ->
                _vaccinations.value = list
                _isLoading.value = false
            }
        }
    }

    fun loadUpcoming() {
        viewModelScope.launch {
            val result = vaccinationRepository.fetchUpcomingVaccinations()
            result.onSuccess { list ->
                _upcomingVaccinations.value = list
            }
        }
    }

    fun recordVaccination(goatId: Int, farmerId: Int, vaccineType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            vaccinationRepository.recordVaccination(
                VaccinationCreateDto(
                    goatId = goatId,
                    farmerId = farmerId,
                    vaccineType = vaccineType,
                    vaccinationDate = dateStr,
                    nextDueDate = dateStr, // placeholder due date
                    status = "completed"
                )
            )
            _isLoading.value = false
        }
    }
}

// ──────────────────────────────────────────────
// Claims ViewModel
// ──────────────────────────────────────────────

@HiltViewModel
class ClaimsViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
    private val goatRepository: GoatRepository
) : ViewModel() {

    private val _claims = MutableStateFlow<List<ClaimEntity>>(emptyList())
    val claims: StateFlow<List<ClaimEntity>> = _claims.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadClaims()
    }

    fun loadClaims() {
        viewModelScope.launch {
            _isLoading.value = true
            claimRepository.fetchClaims()
            claimRepository.allClaims.collect { list ->
                _claims.value = list
                _isLoading.value = false
            }
        }
    }

    fun fileClaim(policyId: Int, goatId: Int, farmerId: Int, cause: String, desc: String, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            claimRepository.fileClaim(
                ClaimCreateDto(
                    policyId = policyId,
                    goatId = goatId,
                    farmerId = farmerId,
                    deathDate = dateStr,
                    deathCause = cause,
                    deathDescription = desc,
                    claimAmount = amount
                )
            )
            _isLoading.value = false
        }
    }

    fun reviewClaim(claimId: Int, status: String, remarks: String, approvedAmount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            claimRepository.reviewClaim(claimId, status, remarks, approvedAmount)
            _isLoading.value = false
        }
    }
}

// ──────────────────────────────────────────────
// Reports ViewModel
// ──────────────────────────────────────────────

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<DashboardStatsDto?>(null)
    val stats: StateFlow<DashboardStatsDto?> = _stats.asStateFlow()

    private val _reports = MutableStateFlow<List<ReportDto>>(emptyList())
    val reports: StateFlow<List<ReportDto>> = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = reportRepository.getDashboardStats()
            result.onSuccess { data ->
                _stats.value = data
            }
            _isLoading.value = false
        }
    }

    fun generateReport(title: String, type: String, category: String, start: String, end: String) {
        viewModelScope.launch {
            _isLoading.value = true
            reportRepository.generateReport(title, type, category, start, end)
            loadDashboardStats()
            _isLoading.value = false
        }
    }
}

// ──────────────────────────────────────────────
// Profile ViewModel
// ──────────────────────────────────────────────

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val currentUser: Flow<UserEntity?> = authRepository.currentUser
    val unreadNotificationsCount: Flow<Int> = notificationRepository.unreadCount

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}

// ──────────────────────────────────────────────
// Admin ViewModel
// ──────────────────────────────────────────────

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    // Audit logs & management parameters
}

// ──────────────────────────────────────────────
// AI Assistant ViewModel
// ──────────────────────────────────────────────

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AssistantViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! I am your Goat Insurance AI Assistant. How can I help you today?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val faqs = mapOf(
        "claim" to "To file a claim, select the deceased goat from your Goats tab, capture carcass photos, enter the cause of death and submit. The coordinator will review it shortly.",
        "vaccination" to "Goat vaccinations like PPR and FMD are scheduled periodically. Check the Vaccinations tab on the home screen to view upcoming and history.",
        "premium" to "Premium can be collected via Cash, UPI, or Net Banking during enrollment. It is typically a small percentage of the goat's market value."
    )

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text, true)
        _messages.value = _messages.value + userMsg

        // AI automated reply after a delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val lower = text.lowercase()
            val replyText = when {
                lower.contains("claim") || lower.contains("death") || lower.contains("die") -> faqs["claim"]
                lower.contains("vaccin") || lower.contains("ppr") || lower.contains("fmd") -> faqs["vaccination"]
                lower.contains("premium") || lower.contains("pay") || lower.contains("fee") -> faqs["premium"]
                else -> "I can assist you with enrollments, vaccinations, claims and premium status. Please try typing keywords like 'claim', 'vaccination' or 'premium'."
            } ?: "I'm not sure about that. Please contact our support team."
            _messages.value = _messages.value + ChatMessage(replyText, false)
        }
    }
}
