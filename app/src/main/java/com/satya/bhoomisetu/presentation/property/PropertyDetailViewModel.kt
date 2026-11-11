package com.satya.bhoomisetu.presentation.property

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.PropertyNote
import com.satya.bhoomisetu.domain.model.PropertyNoteType
import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.usecase.AddPropertyNoteUseCase
import com.satya.bhoomisetu.domain.usecase.DeletePropertyUseCase
import com.satya.bhoomisetu.domain.usecase.GetPropertiesUseCase
import com.satya.bhoomisetu.domain.usecase.GetPropertyNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPropertiesUseCase: GetPropertiesUseCase,
    private val getPropertyNotesUseCase: GetPropertyNotesUseCase,
    private val addPropertyNoteUseCase: AddPropertyNoteUseCase,
    private val deletePropertyUseCase: DeletePropertyUseCase
) : ViewModel() {

    private val propertyId: String = checkNotNull(savedStateHandle["propertyId"])

    private val _property = MutableStateFlow<PropertyProfile?>(null)
    val property: StateFlow<PropertyProfile?> = _property.asStateFlow()

    val notes: StateFlow<List<PropertyNote>> = getPropertyNotesUseCase(propertyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    init {
        viewModelScope.launch {
            _property.value = getPropertiesUseCase.getById(propertyId)
        }
    }

    fun addNote(type: PropertyNoteType, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            addPropertyNoteUseCase(propertyId, type, text.trim())
        }
    }

    fun deleteProperty() {
        viewModelScope.launch {
            deletePropertyUseCase(propertyId)
            _isDeleted.value = true
        }
    }
}
