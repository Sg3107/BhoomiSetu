package com.satya.bhoomisetu.domain.model

data class SurveyorProfile(
    val userId: String,
    val licenseNumber: String,
    val serviceAreas: List<String>,   // district/mandal names, multi-select
    val yearsOfExperience: Int,
    val idProofUrl: String? = null,    // uploaded later; nullable for Phase 1
    val isAvailable: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING
)

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
