package com.satya.bhoomisetu.presentation.documentwriter

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.data.remote.SessionManager
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.DocumentWriterRequest
import com.satya.bhoomisetu.domain.repository.DocumentWriterRepository
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

data class WriterDetailUiState(
    val request: DocumentWriterRequest? = null,
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isSendingMessage: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class DocumentWriterRequestDetailViewModel @Inject constructor(
    private val repository: DocumentWriterRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val requestId: String = savedStateHandle.get<String>("requestId") ?: ""

    private val _uiState = MutableStateFlow(WriterDetailUiState())
    val uiState: StateFlow<WriterDetailUiState> = _uiState.asStateFlow()

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
            when (val result = repository.getRequestDetail(requestId)) {
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
            when (val result = repository.acceptRequest(requestId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            request = result.data,
                            successMessage = "Order accepted! Contact details & drafting workspace unlocked."
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

    fun uploadClientDocument(fileUri: Uri, documentType: String, title: String, context: Context) {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            val result = repository.uploadClientDocument(requestId, fileUri, documentType, title, context)
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            request = result.data,
                            successMessage = "Document uploaded successfully for writer review!"
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

    fun uploadDeedDraft(fileUri: Uri, title: String, notes: String, isFinal: Boolean, context: Context) {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            val result = repository.uploadDeedDraft(requestId, fileUri, title, notes, isFinal, context)
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            request = result.data,
                            successMessage = "Draft deed successfully uploaded and delivered to client!"
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

    fun completeRequest() {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.completeRequest(requestId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            request = result.data,
                            successMessage = "Deed drafting marked as COMPLETED!"
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
                else -> {}
            }
        }
    }

    fun sendMessage(text: String, senderName: String) {
        if (requestId.isBlank() || text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingMessage = true) }
            when (val result = repository.sendMessage(requestId, text, senderName)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isSendingMessage = false,
                            messages = state.messages + result.data
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
                if (requestId.isNotBlank() && _uiState.value.request?.status != "PENDING") {
                    when (val result = repository.getMessages(requestId)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(messages = result.data) }
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
