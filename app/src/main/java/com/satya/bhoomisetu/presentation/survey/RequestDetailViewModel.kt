package com.satya.bhoomisetu.presentation.survey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.SurveyRequest
import com.satya.bhoomisetu.domain.model.User
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
class RequestDetailViewModel @Inject constructor(
    private val repository: SurveyRequestRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val requestId: String = savedStateHandle.get<String>("requestId") ?: ""

    private val _request = MutableStateFlow<Result<SurveyRequest>?>(null)
    val request: StateFlow<Result<SurveyRequest>?> = _request.asStateFlow()

    private val _messages = MutableStateFlow<Result<List<ChatMessage>>>(Result.Loading)
    val messages: StateFlow<Result<List<ChatMessage>>> = _messages.asStateFlow()
    
    private val _actionState = MutableStateFlow<Result<Unit>?>(null)
    val actionState: StateFlow<Result<Unit>?> = _actionState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUser.value = getCurrentUserUseCase().firstOrNull()
            loadRequestDetails()
            loadMessages()
        }
    }

    // Since we don't have a GET /requests/:id endpoint yet, we can filter from all open/assigned/my requests
    // A better approach would be to fetch the list and find it, but for now we'll do this hack
    fun loadRequestDetails() {
        viewModelScope.launch {
            _request.value = Result.Loading
            // Hacky workaround since there's no GET /requests/:id in the backend
            // Try open requests first, then assigned, then mine.
            val allLists = listOf(
                repository.getOpenRequests(),
                repository.getAssignedRequests(),
                repository.getMyRequests()
            )
            
            for (res in allLists) {
                if (res is Result.Success) {
                    val req = res.data.find { it.id == requestId }
                    if (req != null) {
                        _request.value = Result.Success(req)
                        return@launch
                    }
                }
            }
            
            _request.value = Result.Error(Exception("Request not found"))
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            _messages.value = Result.Loading
            _messages.value = repository.getMessages(requestId)
        }
    }

    fun acceptRequest() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            _actionState.value = Result.Loading
            val res = repository.acceptRequest(requestId, user.name)
            if (res is Result.Success) {
                _actionState.value = Result.Success(Unit)
                loadRequestDetails() // Reload to get updated status
            } else if (res is Result.Error) {
                _actionState.value = Result.Error(res.exception)
            }
        }
    }

    fun completeRequest() {
        viewModelScope.launch {
            _actionState.value = Result.Loading
            val res = repository.completeRequest(requestId)
            if (res is Result.Success) {
                _actionState.value = Result.Success(Unit)
                loadRequestDetails() // Reload to get updated status
            } else if (res is Result.Error) {
                _actionState.value = Result.Error(res.exception)
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val res = repository.sendMessage(requestId, user.name, text)
            if (res is Result.Success) {
                loadMessages() // Refresh messages
            }
        }
    }
    
    fun resetActionState() {
        _actionState.value = null
    }
}
