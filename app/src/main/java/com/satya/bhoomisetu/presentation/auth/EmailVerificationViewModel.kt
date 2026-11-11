package com.satya.bhoomisetu.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.usecase.ResendEmailOtpUseCase
import com.satya.bhoomisetu.domain.usecase.VerifyEmailOtpUseCase
import com.satya.bhoomisetu.util.Result
import com.satya.bhoomisetu.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailVerificationState(
    val email: String = "",
    val otp: String = "",
    val otpError: String? = null,
    val countdownSeconds: Int = 30,
    val isResending: Boolean = false,
    val infoMessage: String? = null
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val verifyEmailOtpUseCase: VerifyEmailOtpUseCase,
    private val resendEmailOtpUseCase: ResendEmailOtpUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(UiState<User>())
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val initialEmail = savedStateHandle.get<String>("email").orEmpty()
        if (initialEmail.isNotBlank()) {
            _state.update { it.copy(email = initialEmail) }
        }
        startCountdownTimer()
    }

    fun setEmail(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onOtpChanged(otp: String) {
        val filtered = otp.filter { it.isDigit() }.take(6)
        _state.update { it.copy(otp = filtered, otpError = null, infoMessage = null) }
    }

    fun verifyOtp(onSuccess: (User) -> Unit) {
        val current = _state.value
        if (current.otp.length != 6) {
            _state.update { it.copy(otpError = "Please enter all 6 digits of the OTP") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = verifyEmailOtpUseCase(current.email, current.otp)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, data = result.data) }
                    onSuccess(result.data)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun resendOtp() {
        val current = _state.value
        if (current.countdownSeconds > 0 || current.isResending) return

        viewModelScope.launch {
            _state.update { it.copy(isResending = true, infoMessage = null) }
            val result = resendEmailOtpUseCase(current.email)
            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isResending = false,
                            infoMessage = "A fresh 6-digit OTP has been sent to your email."
                        )
                    }
                    startCountdownTimer()
                }
                is Result.Error -> {
                    _state.update { it.copy(isResending = false) }
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in 30 downTo 0) {
                _state.update { it.copy(countdownSeconds = i) }
                delay(1000)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
