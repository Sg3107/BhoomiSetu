package com.satya.bhoomisetu.domain.repository

import com.satya.bhoomisetu.domain.model.PropertyNote
import com.satya.bhoomisetu.domain.model.PropertyNoteType
import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.model.RecordType
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {
    suspend fun createProperty(
        nickname: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        recordType: RecordType
    ): PropertyProfile

    fun getProperties(): Flow<List<PropertyProfile>>

    suspend fun getProperty(id: String): PropertyProfile?

    suspend fun deleteProperty(id: String)

    suspend fun addNote(propertyId: String, type: PropertyNoteType, text: String): PropertyNote

    fun getNotes(propertyId: String): Flow<List<PropertyNote>>

    suspend fun deleteNote(note: PropertyNote)
}
