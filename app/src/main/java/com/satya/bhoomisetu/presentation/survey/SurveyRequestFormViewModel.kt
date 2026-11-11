package com.satya.bhoomisetu.presentation.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.repository.SurveyRequestRepository
import com.satya.bhoomisetu.domain.usecase.GetCurrentUserUseCase
import com.satya.bhoomisetu.domain.usecase.GetPropertiesUseCase
import com.satya.bhoomisetu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyRequestFormViewModel @Inject constructor(
    private val repository: SurveyRequestRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getPropertiesUseCase: GetPropertiesUseCase
) : ViewModel() {

    val savedProperties: StateFlow<List<PropertyProfile>> = getPropertiesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<Result<Unit>?>(null)
    val uiState: StateFlow<Result<Unit>?> = _uiState.asStateFlow()

    fun submitRequest(
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        description: String
    ) {
        viewModelScope.launch {
            _uiState.value = Result.Loading
            
            val user = getCurrentUserUseCase().firstOrNull()
            if (user == null) {
                _uiState.value = Result.Error(Exception("User not found. Please login again."))
                return@launch
            }

            if (district.isBlank() || mandal.isBlank() || description.isBlank()) {
                _uiState.value = Result.Error(Exception("District, Mandal, and Description are required."))
                return@launch
            }

            val result = repository.createRequest(
                requesterName = user.name,
                requesterPhone = user.phone,
                district = district,
                mandal = mandal,
                village = village,
                surveyNumber = surveyNumber,
                description = description
            )

            if (result is Result.Success) {
                _uiState.value = Result.Success(Unit)
            } else if (result is Result.Error) {
                _uiState.value = result
            }
        }
    }
    
    fun resetState() {
        _uiState.value = null
    }
}
