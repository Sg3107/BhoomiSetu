package com.satya.bhoomisetu.presentation.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.LegalAdvisorRequest
import com.satya.bhoomisetu.domain.model.LegalAdvisorSummary
import com.satya.bhoomisetu.domain.repository.LegalAdvisorRepository
import com.satya.bhoomisetu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LegalDirectoryUiState(
    val selectedDistrict: String = "All Districts",
    val advisors: List<LegalAdvisorSummary> = emptyList(),
    val myRequests: List<LegalAdvisorRequest> = emptyList(),
    val selectedTab: Int = 0, // 0 = Advisors Directory, 1 = My Requests
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LegalAdvisorDirectoryViewModel @Inject constructor(
    private val repository: LegalAdvisorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LegalDirectoryUiState())
    val uiState: StateFlow<LegalDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadAdvisors()
        loadMyRequests()
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        if (tabIndex == 1) {
            loadMyRequests()
        }
    }

    fun onDistrictSelected(district: String) {
        _uiState.update { it.copy(selectedDistrict = district) }
        loadAdvisors()
    }

    fun loadAdvisors() {
        val district = if (_uiState.value.selectedDistrict == "All Districts") null else _uiState.value.selectedDistrict
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getLegalAdvisors(district)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, advisors = result.data) }
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

    fun loadMyRequests() {
        viewModelScope.launch {
            when (val result = repository.getMyLegalRequests()) {
                is Result.Success -> {
                    _uiState.update { it.copy(myRequests = result.data) }
                }
                is Result.Error -> {
                    // Fail silently or keep existing
                }
                Result.Loading -> {}
            }
        }
    }

    fun createVerificationRequest(
        advisorId: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        propertyTitle: String,
        description: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = repository.createLegalRequest(
                advisorId = advisorId,
                district = district,
                mandal = mandal,
                village = village,
                surveyNumber = surveyNumber,
                propertyTitle = propertyTitle,
                description = description
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Request submitted successfully to advisor."
                        )
                    }
                    loadMyRequests()
                    onSuccess(result.data.id)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
                Result.Loading -> {
                    _uiState.update { it.copy(isSubmitting = true) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
