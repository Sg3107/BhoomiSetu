package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.PropertyNote
import com.satya.bhoomisetu.domain.model.PropertyNoteType
import com.satya.bhoomisetu.domain.repository.PropertyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddPropertyNoteUseCase @Inject constructor(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(propertyId: String, type: PropertyNoteType, text: String): PropertyNote =
        repository.addNote(propertyId, type, text)
}

class GetPropertyNotesUseCase @Inject constructor(
    private val repository: PropertyRepository
) {
    operator fun invoke(propertyId: String): Flow<List<PropertyNote>> = repository.getNotes(propertyId)
}

class DeletePropertyNoteUseCase @Inject constructor(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(note: PropertyNote) = repository.deleteNote(note)
}
