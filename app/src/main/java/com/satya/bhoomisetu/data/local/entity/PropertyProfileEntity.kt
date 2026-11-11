package com.satya.bhoomisetu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.satya.bhoomisetu.domain.model.PropertyProfile
import com.satya.bhoomisetu.domain.model.RecordType

@Entity(tableName = "property_profiles")
data class PropertyProfileEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val nickname: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val recordType: String,
    val createdAt: Long
) {
    fun toDomainModel() = PropertyProfile(
        id = id,
        ownerId = ownerId,
        nickname = nickname,
        district = district,
        mandal = mandal,
        village = village,
        surveyNumber = surveyNumber,
        recordType = RecordType.valueOf(recordType),
        createdAt = createdAt
    )

    companion object {
        fun fromDomainModel(model: PropertyProfile) = PropertyProfileEntity(
            id = model.id,
            ownerId = model.ownerId,
            nickname = model.nickname,
            district = model.district,
            mandal = model.mandal,
            village = model.village,
            surveyNumber = model.surveyNumber,
            recordType = model.recordType.name,
            createdAt = model.createdAt
        )
    }
}
