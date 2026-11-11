package com.satya.bhoomisetu.data.repository

import com.satya.bhoomisetu.data.local.dao.PropertyDao
import com.satya.bhoomisetu.data.local.entity.PropertyNoteEntity
import com.satya.bhoomisetu.data.local.entity.PropertyProfileEntity
import com.satya.bhoomisetu.data.remote.SessionManager
import com.satya.bhoomisetu.domain.model.PropertyNote
import com.satya.bhoomisetu.domain.model.PropertyNoteType
import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.model.RecordType
import com.satya.bhoomisetu.domain.repository.PropertyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class PropertyRepositoryImpl @Inject constructor(
    private val dao: PropertyDao,
    private val sessionManager: SessionManager
) : PropertyRepository {

    override suspend fun createProperty(
        nickname: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        recordType: RecordType
    ): PropertyProfile {
        val ownerId = sessionManager.userIdFlow.first() ?: ""
        val profile = PropertyProfile(
            id = "prop_${UUID.randomUUID().toString().substring(0, 8)}",
            ownerId = ownerId,
            nickname = nickname,
            district = district,
            mandal = mandal,
            village = village,
            surveyNumber = surveyNumber,
            recordType = recordType,
            createdAt = System.currentTimeMillis()
        )
        dao.insertProperty(PropertyProfileEntity.fromDomainModel(profile))
        return profile
    }

    override fun getProperties(): Flow<List<PropertyProfile>> =
        sessionManager.userIdFlow.flatMapLatest { ownerId ->
            if (ownerId.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                dao.getAllProperties(ownerId).map { list -> list.map { it.toDomainModel() } }
            }
        }

    override suspend fun getProperty(id: String): PropertyProfile? =
        dao.getPropertyById(id)?.toDomainModel()

    override suspend fun deleteProperty(id: String) {
        dao.deleteNotesForProperty(id)
        dao.deleteProperty(id)
    }

    override suspend fun addNote(propertyId: String, type: PropertyNoteType, text: String): PropertyNote {
        val note = PropertyNote(
            id = "note_${UUID.randomUUID().toString().substring(0, 8)}",
            propertyId = propertyId,
            type = type,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        dao.insertNote(PropertyNoteEntity.fromDomainModel(note))
        return note
    }

    override fun getNotes(propertyId: String): Flow<List<PropertyNote>> =
        dao.getNotesForProperty(propertyId).map { list -> list.map { it.toDomainModel() } }

    override suspend fun deleteNote(note: PropertyNote) {
        dao.deleteNote(PropertyNoteEntity.fromDomainModel(note))
    }
}
