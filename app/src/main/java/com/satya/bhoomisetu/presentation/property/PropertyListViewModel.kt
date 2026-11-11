package com.satya.bhoomisetu.presentation.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.usecase.GetPropertiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    getPropertiesUseCase: GetPropertiesUseCase
) : ViewModel() {

    val properties: StateFlow<List<PropertyProfile>> = getPropertiesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
