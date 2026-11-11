package com.satya.bhoomisetu.presentation.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.SurveyRequest
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.domain.repository.SurveyRequestRepository
import com.satya.bhoomisetu.domain.usecase.GetCurrentUserUseCase
import com.satya.bhoomisetu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyMarketplaceViewModel @Inject constructor(
    private val repository: SurveyRequestRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _requests = MutableStateFlow<Result<List<SurveyRequest>>>(Result.Loading)
    val requests: StateFlow<Result<List<SurveyRequest>>> = _requests.asStateFlow()

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _selectedDistrict = MutableStateFlow<String?>(null)
    val selectedDistrict: StateFlow<String?> = _selectedDistrict.asStateFlow()

    private val _selectedMandal = MutableStateFlow<String?>(null)
    val selectedMandal: StateFlow<String?> = _selectedMandal.asStateFlow()

    init {
        viewModelScope.launch {
            _userRole.value = getCurrentUserUseCase().firstOrNull()?.role
        }
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            _requests.value = Result.Loading
            _requests.value = repository.getMyRequests()
        }
    }

    fun filterOpenRequests(district: String?, mandal: String?) {
        _selectedDistrict.value = district
        _selectedMandal.value = mandal
        viewModelScope.launch {
            _requests.value = Result.Loading
            _requests.value = repository.getOpenRequests(district = district, mandal = mandal)
        }
    }

    fun loadOpenRequests() {
        filterOpenRequests(_selectedDistrict.value, _selectedMandal.value)
    }

    fun loadAssignedRequests() {
        viewModelScope.launch {
            _requests.value = Result.Loading
            _requests.value = repository.getAssignedRequests()
        }
    }
}
