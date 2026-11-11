package com.satya.bhoomisetu.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.domain.usecase.RegisterUseCase
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

data class RegisterFieldsState(
    val name: String = "",
    val nameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val selectedRole: UserRole = UserRole.USER,
    // Professional specific fields
    val licenseNumber: String = "",
    val licenseNumberError: String? = null,
    val experienceYears: String = "",
    val experienceYearsError: String? = null,
    val selectedServiceAreas: List<String> = emptyList(),
    val serviceAreaError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _fieldsState = MutableStateFlow(RegisterFieldsState())
    val fieldsState: StateFlow<RegisterFieldsState> = _fieldsState.asStateFlow()

    private val _uiState = MutableStateFlow(UiState<String>())
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _fieldsState.update { it.copy(name = name, nameError = null) }
    }

    fun onPhoneChanged(phone: String) {
        // Allow digits only up to 10
        val filtered = phone.filter { it.isDigit() }.take(10)
        _fieldsState.update { it.copy(phone = filtered, phoneError = null) }
    }

    fun onEmailChanged(email: String) {
        _fieldsState.update { it.copy(email = email.trim(), emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _fieldsState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _fieldsState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onRoleChanged(role: UserRole) {
        _fieldsState.update { it.copy(selectedRole = role) }
    }

    fun onLicenseNumberChanged(license: String) {
        _fieldsState.update { it.copy(licenseNumber = license, licenseNumberError = null) }
    }

    fun onExperienceYearsChanged(years: String) {
        val filtered = years.filter { it.isDigit() }.take(2)
        _fieldsState.update { it.copy(experienceYears = filtered, experienceYearsError = null) }
    }

    fun toggleServiceArea(district: String) {
        _fieldsState.update { current ->
            val updatedAreas = if (current.selectedServiceAreas.contains(district)) {
                current.selectedServiceAreas - district
            } else {
                current.selectedServiceAreas + district
            }
            current.copy(selectedServiceAreas = updatedAreas, serviceAreaError = null)
        }
    }

    fun register(onNavigateToVerify: (String) -> Unit) {
        val fields = _fieldsState.value
        var hasError = false

        if (!Validators.isValidName(fields.name)) {
            _fieldsState.update { it.copy(nameError = "Full name must be at least 2 characters") }
            hasError = true
        }

        if (!Validators.isValidPhone(fields.phone)) {
            _fieldsState.update { it.copy(phoneError = "Enter a valid 10-digit Indian mobile number (e.g., 9876543210)") }
            hasError = true
        }

        if (!Validators.isValidEmail(fields.email)) {
            _fieldsState.update { it.copy(emailError = "Enter a valid email address (e.g. name@gmail.com)") }
            hasError = true
        }

        if (!Validators.isValidPassword(fields.password)) {
            _fieldsState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            hasError = true
        }

        if (!Validators.doPasswordsMatch(fields.password, fields.confirmPassword)) {
            _fieldsState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            hasError = true
        }

        if (fields.selectedRole == UserRole.SURVEYOR) {
            if (fields.licenseNumber.trim().isBlank()) {
                _fieldsState.update { it.copy(licenseNumberError = "License number is required for surveyors") }
                hasError = true
            }
            if (!Validators.isValidExperienceYears(fields.experienceYears)) {
                _fieldsState.update { it.copy(experienceYearsError = "Enter valid years of experience (0-50)") }
                hasError = true
            }
            if (fields.selectedServiceAreas.isEmpty()) {
                _fieldsState.update { it.copy(serviceAreaError = "Select at least 1 service district in Andhra Pradesh") }
                hasError = true
            }
        } else if (fields.selectedRole == UserRole.LEGAL_ADVISOR) {
            if (fields.licenseNumber.trim().isBlank()) {
                _fieldsState.update { it.copy(licenseNumberError = "Bar Council / Enrollment number is required (e.g. AP/1234/2020)") }
                hasError = true
            }
            if (fields.selectedServiceAreas.isEmpty()) {
                _fieldsState.update { it.copy(serviceAreaError = "Select at least 1 practice district in Andhra Pradesh") }
                hasError = true
            }
        } else if (fields.selectedRole == UserRole.DOCUMENT_WRITER) {
            if (fields.selectedServiceAreas.isEmpty()) {
                _fieldsState.update { it.copy(serviceAreaError = "Select at least 1 service district in Andhra Pradesh") }
                hasError = true
            }
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = registerUseCase(
                name = fields.name,
                phone = fields.phone,
                email = fields.email,
                password = fields.password,
                confirmPassword = fields.confirmPassword,
                role = fields.selectedRole,
                licenseNumber = fields.licenseNumber,
                experienceYears = fields.experienceYears,
                serviceAreas = fields.selectedServiceAreas
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, data = result.data) }
                    onNavigateToVerify(result.data)
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
