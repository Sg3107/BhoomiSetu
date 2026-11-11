package com.satya.bhoomisetu.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.SurveyorProfile
import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.domain.usecase.GetCurrentUserUseCase
import com.satya.bhoomisetu.domain.usecase.LogoutUseCase
import com.satya.bhoomisetu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val surveyorProfile: SurveyorProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val currentUser: StateFlow<User?> = getCurrentUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _surveyorProfile = MutableStateFlow<SurveyorProfile?>(null)
    val surveyorProfile: StateFlow<SurveyorProfile?> = _surveyorProfile.asStateFlow()

    fun fetchSurveyorProfile(userId: String) {
        viewModelScope.launch {
            when (val result = authRepository.getSurveyorProfile(userId)) {
                is Result.Success -> _surveyorProfile.value = result.data
                else -> _surveyorProfile.value = null
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLoggedOut()
        }
    }
}
