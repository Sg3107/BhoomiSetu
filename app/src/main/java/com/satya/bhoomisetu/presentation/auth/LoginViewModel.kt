package com.satya.bhoomisetu.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.repository.EmailNotVerifiedException
import com.satya.bhoomisetu.domain.usecase.GoogleAuthUseCase
import com.satya.bhoomisetu.domain.usecase.LoginUseCase
import com.satya.bhoomisetu.util.Result
import com.satya.bhoomisetu.util.UiState
import com.satya.bhoomisetu.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginFieldsState(
    val phoneOrEmail: String = "",
    val phoneOrEmailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val unverifiedEmail: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleAuthUseCase: GoogleAuthUseCase
) : ViewModel() {

    private val _fieldsState = MutableStateFlow(LoginFieldsState())
    val fieldsState: StateFlow<LoginFieldsState> = _fieldsState.asStateFlow()

    private val _uiState = MutableStateFlow(UiState<User>())
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()

    fun onPhoneOrEmailChanged(value: String) {
        _fieldsState.update { it.copy(phoneOrEmail = value, phoneOrEmailError = null, unverifiedEmail = null) }
    }

    fun onPasswordChanged(password: String) {
        _fieldsState.update { it.copy(password = password, passwordError = null) }
    }

    fun login(
        onSuccess: (User) -> Unit,
        onRequiresVerification: (String) -> Unit
    ) {
        val current = _fieldsState.value
        val trimmedIdentifier = current.phoneOrEmail.trim()
        var hasError = false

        val isValid = if (trimmedIdentifier.contains("@")) {
            Validators.isValidEmail(trimmedIdentifier)
        } else {
            Validators.isValidPhone(trimmedIdentifier)
        }

        if (!isValid) {
            _fieldsState.update {
                it.copy(phoneOrEmailError = "Enter a valid email address or 10-digit mobile number")
            }
            hasError = true
        }

        if (current.password.length < 6) {
            _fieldsState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginUseCase(trimmedIdentifier, current.password)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, data = result.data) }
                    onSuccess(result.data)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    if (result.exception is EmailNotVerifiedException) {
                        val unverified = (result.exception as EmailNotVerifiedException).email
                        _fieldsState.update { it.copy(unverifiedEmail = unverified) }
                        onRequiresVerification(unverified)
                    }
                }
                Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loginWithGoogle(
        idToken: String,
        onSuccess: (User) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = googleAuthUseCase(idToken)
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
