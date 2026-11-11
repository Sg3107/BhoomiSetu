package com.satya.bhoomisetu.data.remote.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
    val surveyorProfile: SurveyorProfileDto? = null,
    val legalAdvisorProfile: LegalAdvisorProfileDto? = null,
    val documentWriterProfile: DocumentWriterProfileDto? = null
)

data class RegisterResponseDto(
    val message: String,
    val email: String,
    val userId: String? = null,
    val requiresEmailVerification: Boolean = true
)

data class SimpleMessageResponseDto(
    val message: String
)

data class UserDto(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String,
    val createdAt: Long,
    val surveyorProfile: SurveyorProfileDto? = null,
    val legalAdvisorProfile: LegalAdvisorProfileDto? = null,
    val documentWriterProfile: DocumentWriterProfileDto? = null
)

data class SurveyorProfileDto(
    val userId: String,
    val licenseNumber: String,
    val serviceAreas: List<String>,
    val yearsOfExperience: Int,
    val idProofUrl: String? = null,
    val isAvailable: Boolean = false,
    val verificationStatus: String = "PENDING"
)

data class LegalAdvisorProfileDto(
    val enrollmentNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0,
    val specialization: String = "Land & Property Verification",
    val isAvailable: Boolean = true
)

data class DocumentWriterProfileDto(
    val licenseNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0,
    val isAvailable: Boolean = true
)


data class ErrorResponseDto(
    val error: ErrorDetailDto,
    val email: String? = null
)

data class ErrorDetailDto(
    val code: String,
    val message: String
)
