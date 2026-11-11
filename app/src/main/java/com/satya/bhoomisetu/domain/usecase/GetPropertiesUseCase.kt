package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.repository.PropertyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPropertiesUseCase @Inject constructor(
    private val repository: PropertyRepository
) {
    operator fun invoke(): Flow<List<PropertyProfile>> = repository.getProperties()

    suspend fun getById(id: String): PropertyProfile? = repository.getProperty(id)
}
