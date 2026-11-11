package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.repository.PropertyRepository
import javax.inject.Inject

class DeletePropertyUseCase @Inject constructor(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteProperty(id)
}
