package com.satya.bhoomisetu.util

/**
 * Standard UI state container for ViewModel StateFlows.
 */
data class UiState<out T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val errorMessage: String? = null
)
