package com.satya.bhoomisetu.presentation.legal

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.data.remote.SessionManager
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.LegalAdvisorRequest
import com.satya.bhoomisetu.domain.repository.LegalAdvisorRepository
import com.satya.bhoomisetu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LegalDetailUiState(
    val request: LegalAdvisorRequest? = null,
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isSendingMessage: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LegalRequestDetailViewModel @Inject constructor(
    private val repository: LegalAdvisorRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val requestId: String = savedStateHandle.get<String>("requestId") ?: ""

    private val _uiState = MutableStateFlow(LegalDetailUiState())
    val uiState: StateFlow<LegalDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = sessionManager.userIdFlow.first() ?: ""
            _uiState.update { it.copy(currentUserId = userId) }
            loadRequestDetail()
            loadMessages()
            startMessagePolling()
        }
    }

    fun loadRequestDetail() {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getLegalRequestDetail(requestId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, request = result.data) }
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

    fun acceptRequest() {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.acceptLegalRequest(requestId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            request = result.data,
                            successMessage = "Request accepted. Client phone number & chat unlocked!"
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun uploadDocument(fileUri: Uri, documentType: String, title: String, context: Context) {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            val result = repository.uploadDocument(requestId, fileUri, documentType, title, context)
            when (result) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isUploading = false,
                            request = result.data,
                            successMessage = "Document uploaded successfully for verification!"
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isUploading = false, errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun reviewDocument(docId: String, status: String, remarks: String) {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            when (val result = repository.reviewDocument(requestId, docId, status, remarks)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            request = result.data,
                            successMessage = "Document verification status updated."
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun submitOverallReview(status: String, summary: String) {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.submitReview(requestId, status, summary)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            request = result.data,
                            successMessage = "Verification review & legal opinion submitted!"
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun loadMessages() {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            when (val result = repository.getMessages(requestId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(messages = result.data) }
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun sendMessage(text: String, senderName: String) {
        if (requestId.isBlank() || text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingMessage = true) }
            when (val result = repository.sendMessage(requestId, text, senderName)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isSendingMessage = false,
                            messages = it.messages + result.data
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSendingMessage = false, errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    private fun startMessagePolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(4000)
                if (_uiState.value.request?.status in listOf("ACCEPTED", "VERIFIED", "NEEDS_CHANGES")) {
                    when (val result = repository.getMessages(requestId)) {
                        is Result.Success -> {
                            if (result.data.size != _uiState.value.messages.size) {
                                _uiState.update { it.copy(messages = result.data) }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
