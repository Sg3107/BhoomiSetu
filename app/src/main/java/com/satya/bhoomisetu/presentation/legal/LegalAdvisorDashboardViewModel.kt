package com.satya.bhoomisetu.presentation.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.LegalAdvisorRequest
import com.satya.bhoomisetu.domain.repository.LegalAdvisorRepository
import com.satya.bhoomisetu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdvisorDashboardUiState(
    val requests: List<LegalAdvisorRequest> = emptyList(),
    val filterTab: Int = 0, // 0 = Pending, 1 = Active/Accepted, 2 = Completed
    val isLoading: Boolean = false,
    val actionLoadingId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LegalAdvisorDashboardViewModel @Inject constructor(
    private val repository: LegalAdvisorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorDashboardUiState())
    val uiState: StateFlow<AdvisorDashboardUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    fun setFilterTab(tab: Int) {
        _uiState.update { it.copy(filterTab = tab) }
    }

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getAdvisorRequests()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, requests = result.data) }
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

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoadingId = requestId, errorMessage = null) }
            when (val result = repository.acceptLegalRequest(requestId)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            actionLoadingId = null,
                            successMessage = "Request accepted. Client phone number and in-app chat are now unlocked!",
                            requests = state.requests.map { if (it.id == requestId) result.data else it }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(actionLoadingId = null, errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoadingId = requestId, errorMessage = null) }
            when (val result = repository.rejectLegalRequest(requestId)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            actionLoadingId = null,
                            successMessage = "Request declined.",
                            requests = state.requests.map { if (it.id == requestId) result.data else it }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(actionLoadingId = null, errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
