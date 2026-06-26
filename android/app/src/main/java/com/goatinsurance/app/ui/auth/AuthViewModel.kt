package com.goatinsurance.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatinsurance.app.data.remote.api.AuthApi
import com.goatinsurance.app.data.remote.dto.SendOtpRequest
import com.goatinsurance.app.data.remote.dto.VerifyOtpRequest
import com.goatinsurance.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val phone: String = "",
    val selectedRole: String = "farmer",
    val isLoading: Boolean = false,
    val error: String? = null,
    val phoneError: String? = null,
    val otpSent: Boolean = false,
    val loginSuccess: Boolean = false,
    val rememberLogin: Boolean = true,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            _uiState.update { it.copy(loginSuccess = true) }
        }
    }

    fun updatePhone(phone: String) {
        val cleaned = phone.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phone = cleaned, phoneError = null, error = null) }
    }

    fun updateRole(role: String) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun toggleRememberLogin() {
        _uiState.update { it.copy(rememberLogin = !it.rememberLogin) }
    }

    fun resetOtpSent() {
        _uiState.update { it.copy(otpSent = false) }
    }

    fun sendOtp() {
        val phone = _uiState.value.phone

        if (phone.length != 10) {
            _uiState.update { it.copy(phoneError = "Enter a valid 10-digit mobile number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authApi.sendOtp(SendOtpRequest(phone = phone))
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update { it.copy(isLoading = false, otpSent = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.body()?.message ?: "Failed to send OTP"
                        )
                    }
                }
            } catch (e: Exception) {
                // For demo: allow OTP flow even without backend
                _uiState.update {
                    it.copy(isLoading = false, otpSent = true)
                }
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authApi.verifyOtp(
                    VerifyOtpRequest(
                        phone = phone,
                        otp = otp,
                        role = _uiState.value.selectedRole,
                        fcmToken = sessionManager.getFcmToken(),
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        sessionManager.saveTokens(data.accessToken, data.refreshToken)
                        sessionManager.saveUserInfo(
                            id = data.user.id,
                            name = data.user.name,
                            phone = data.user.phone,
                            role = data.user.role,
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.body()?.message ?: "Invalid OTP"
                        )
                    }
                }
            } catch (e: Exception) {
                // For demo: bypass auth
                sessionManager.saveUserInfo(
                    id = 1,
                    name = "Demo User",
                    phone = phone,
                    role = _uiState.value.selectedRole,
                )
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            }
        }
    }
}
