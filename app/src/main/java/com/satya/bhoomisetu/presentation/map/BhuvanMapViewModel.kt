package com.satya.bhoomisetu.presentation.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BhuvanMapViewModel @Inject constructor() : ViewModel() {

    companion object {
        const val DEFAULT_BHUVAN_URL = "https://bhuvan.nrsc.gov.in/ngmaps#18/16.304169/80.377363"
    }

    private val _currentUrl = MutableStateFlow(DEFAULT_BHUVAN_URL)
    val currentUrl = _currentUrl.asStateFlow()

    data class LocationInfo(
        val lat: Double,
        val lon: Double,
        val name: String
    )

    val quickLocations = listOf(
        LocationInfo(16.304169, 80.377363, "Guntur / Amaravati"),
        LocationInfo(16.506174, 80.648015, "Vijayawada (NTR)"),
        LocationInfo(17.686816, 83.218482, "Visakhapatnam"),
        LocationInfo(16.989065, 82.247465, "Kakinada"),
        LocationInfo(16.945000, 81.790000, "Rajahmundry"),
        LocationInfo(13.628800, 79.419200, "Tirupati"),
        LocationInfo(15.828100, 78.037300, "Kurnool"),
        LocationInfo(14.681900, 77.600600, "Anantapur")
    )

    fun jumpToCoordinates(lat: Double, lon: Double) {
        _currentUrl.value = "https://bhuvan.nrsc.gov.in/ngmaps#18/$lat/$lon"
    }

    fun resetToDefault() {
        _currentUrl.value = DEFAULT_BHUVAN_URL
    }
}
