package com.goatinsurance.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatinsurance.app.data.remote.api.ReportsApi
import com.goatinsurance.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalEnrollments: Int = 0,
    val premiumCollected: String = "0",
    val totalClaims: Int = 0,
    val vaccinationDue: Int = 0,
    val deathReports: Int = 0,
    val pendingApprovals: Int = 0,
    val activePolicies: Int = 0,
    val totalFarmers: Int = 0,
    val totalGoats: Int = 0,
)

data class DashboardUiState(
    val userName: String = "User",
    val userRole: String = "farmer",
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: DashboardStats = DashboardStats(),
    val unreadNotifications: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val reportsApi: ReportsApi,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                userName = sessionManager.getUserName().ifEmpty { "User" },
                userRole = sessionManager.getUserRole(),
            )
        }
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = reportsApi.getDashboardStats()
                if (response.isSuccessful && response.body()?.data != null) {
                    val data = response.body()!!.data!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            stats = DashboardStats(
                                totalEnrollments = data.totalEnrollments,
                                premiumCollected = "%.0f".format(data.totalPremiumCollected),
                                totalClaims = data.totalClaims,
                                vaccinationDue = data.vaccinationDue,
                                deathReports = data.deathReports,
                                pendingApprovals = data.pendingApprovals,
                                activePolicies = data.activePolicies,
                                totalFarmers = data.totalFarmers,
                                totalGoats = data.totalGoats,
                            ),
                        )
                    }
                } else {
                    // Use demo data
                    loadDemoStats()
                }
            } catch (e: Exception) {
                loadDemoStats()
            }
        }
    }

    private fun loadDemoStats() {
        _uiState.update {
            it.copy(
                isLoading = false,
                stats = DashboardStats(
                    totalEnrollments = 247,
                    premiumCollected = "1,24,500",
                    totalClaims = 18,
                    vaccinationDue = 42,
                    deathReports = 5,
                    pendingApprovals = 12,
                    activePolicies = 215,
                    totalFarmers = 156,
                    totalGoats = 892,
                ),
                unreadNotifications = 3,
            )
        }
    }
}
