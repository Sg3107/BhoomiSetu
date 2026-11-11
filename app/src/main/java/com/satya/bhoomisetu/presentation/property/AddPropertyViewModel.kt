package com.satya.bhoomisetu.presentation.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satya.bhoomisetu.domain.model.RecordType
import com.satya.bhoomisetu.domain.usecase.CreatePropertyUseCase
import com.satya.bhoomisetu.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPropertyState(
    val nickname: String = "",
    val district: String = "",
    val mandal: String = "",
    val village: String = "",
    val surveyNumber: String = "",
    val recordType: RecordType = RecordType.ADANGAL,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val savedPropertyId: String? = null
)

@HiltViewModel
class AddPropertyViewModel @Inject constructor(
    private val createPropertyUseCase: CreatePropertyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddPropertyState())
    val state: StateFlow<AddPropertyState> = _state.asStateFlow()

    val districts: List<String> = Constants.AP_DISTRICTS

    fun onNicknameChange(v: String) = _state.update { it.copy(nickname = v, fieldErrors = it.fieldErrors - "nickname") }
    fun onDistrictChange(v: String) = _state.update { it.copy(district = v, fieldErrors = it.fieldErrors - "district") }
    fun onMandalChange(v: String) = _state.update { it.copy(mandal = v, fieldErrors = it.fieldErrors - "mandal") }
    fun onVillageChange(v: String) = _state.update { it.copy(village = v, fieldErrors = it.fieldErrors - "village") }
    fun onSurveyNumberChange(v: String) = _state.update { it.copy(surveyNumber = v, fieldErrors = it.fieldErrors - "surveyNumber") }
    fun onRecordTypeChange(v: RecordType) = _state.update { it.copy(recordType = v) }

    fun save() {
        val s = _state.value
        val errors = mutableMapOf<String, String>()
        if (s.nickname.isBlank()) errors["nickname"] = "Give this property a name, e.g. \"Grandpa's farm\""
        if (s.district.isBlank()) errors["district"] = "Select a district"
        if (s.mandal.isBlank()) errors["mandal"] = "Enter the mandal name"
        if (s.village.isBlank()) errors["village"] = "Enter the village name"
        if (s.surveyNumber.isBlank()) errors["surveyNumber"] = "Enter the survey number"

        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val property = createPropertyUseCase(
                nickname = s.nickname.trim(),
                district = s.district.trim(),
                mandal = s.mandal.trim(),
                village = s.village.trim(),
                surveyNumber = s.surveyNumber.trim(),
                recordType = s.recordType
            )
            _state.update { it.copy(isSaving = false, savedPropertyId = property.id) }
        }
    }
}
