package com.satya.bhoomisetu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.satya.bhoomisetu.domain.model.PropertyNote
import com.satya.bhoomisetu.domain.model.PropertyNoteType

@Entity(tableName = "property_notes")
data class PropertyNoteEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val type: String,
    val text: String,
    val createdAt: Long
) {
    fun toDomainModel() = PropertyNote(
        id = id,
        propertyId = propertyId,
        type = PropertyNoteType.valueOf(type),
        text = text,
        createdAt = createdAt
    )

    companion object {
        fun fromDomainModel(model: PropertyNote) = PropertyNoteEntity(
            id = model.id,
            propertyId = model.propertyId,
            type = model.type.name,
            text = model.text,
            createdAt = model.createdAt
        )
    }
}
