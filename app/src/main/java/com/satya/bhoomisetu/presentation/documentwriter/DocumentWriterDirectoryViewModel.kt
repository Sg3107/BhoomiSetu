package com.satya.bhoomisetu.presentation.documentwriter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.DocumentWriterRequest
import com.satya.bhoomisetu.domain.model.DocumentWriterSummary
import com.satya.bhoomisetu.domain.repository.DocumentWriterRepository
import com.satya.bhoomisetu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriterDirectoryUiState(
    val selectedDistrict: String = "All Districts",
    val writers: List<DocumentWriterSummary> = emptyList(),
    val myRequests: List<DocumentWriterRequest> = emptyList(),
    val selectedTab: Int = 0, // 0 = Find Writers, 1 = My Deed Requests
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class DocumentWriterDirectoryViewModel @Inject constructor(
    private val repository: DocumentWriterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriterDirectoryUiState())
    val uiState: StateFlow<WriterDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadWriters()
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
        loadWriters()
    }

    fun loadWriters() {
        val district = if (_uiState.value.selectedDistrict == "All Districts") null else _uiState.value.selectedDistrict
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getDocumentWriters(district)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, writers = result.data) }
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getMyRequests()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, myRequests = result.data) }
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

    fun submitWritingRequest(
        writerId: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        deedType: String,
        propertyTitle: String,
        description: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = repository.createWriterRequest(
                writerId = writerId,
                district = district,
                mandal = mandal,
                village = village,
                surveyNumber = surveyNumber,
                deedType = deedType,
                propertyTitle = propertyTitle,
                description = description
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Deed drafting request submitted successfully!",
                            selectedTab = 1
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
