package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.model.RecordType
import com.satya.bhoomisetu.domain.repository.PropertyRepository
import javax.inject.Inject

class CreatePropertyUseCase @Inject constructor(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(
        nickname: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        recordType: RecordType
    ): PropertyProfile = repository.createProperty(nickname, district, mandal, village, surveyNumber, recordType)
}
