package com.satya.bhoomisetu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.satya.bhoomisetu.domain.model.SurveyorProfile
import com.satya.bhoomisetu.domain.model.VerificationStatus

@Entity(tableName = "surveyor_profiles")
data class SurveyorProfileEntity(
    @PrimaryKey val userId: String,
    val licenseNumber: String,
    val serviceAreasCsv: String, // Stored as comma-separated values
    val yearsOfExperience: Int,
    val idProofUrl: String?,
    val isAvailable: Boolean,
    val verificationStatus: String
) {
    fun toDomainModel(): SurveyorProfile {
        return SurveyorProfile(
            userId = userId,
            licenseNumber = licenseNumber,
            serviceAreas = serviceAreasCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            yearsOfExperience = yearsOfExperience,
            idProofUrl = idProofUrl,
            isAvailable = isAvailable,
            verificationStatus = try { VerificationStatus.valueOf(verificationStatus) } catch (e: Exception) { VerificationStatus.PENDING }
        )
    }

    companion object {
        fun fromDomainModel(profile: SurveyorProfile): SurveyorProfileEntity {
            return SurveyorProfileEntity(
                userId = profile.userId,
                licenseNumber = profile.licenseNumber,
                serviceAreasCsv = profile.serviceAreas.joinToString(","),
                yearsOfExperience = profile.yearsOfExperience,
                idProofUrl = profile.idProofUrl,
                isAvailable = profile.isAvailable,
                verificationStatus = profile.verificationStatus.name
            )
        }
    }
}
