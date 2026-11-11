package com.satya.bhoomisetu.data.remote.dto

data class LoginRequest(
    val emailOrPhone: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val password: String
)

data class VerifyEmailOtpRequest(
    val email: String,
    val otp: String
)

data class ResendEmailOtpRequest(
    val email: String
)

data class GoogleAuthRequest(
    val idToken: String,
    val role: String? = null
)

data class RegisterRequest(
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val role: String,
    val surveyorProfile: SurveyorProfileRequestDto? = null,
    val legalAdvisorProfile: LegalAdvisorProfileRequestDto? = null,
    val documentWriterProfile: DocumentWriterProfileRequestDto? = null
)

data class SurveyorProfileRequestDto(
    val licenseNumber: String,
    val serviceAreas: List<String>,
    val yearsOfExperience: Int
)

data class LegalAdvisorProfileRequestDto(
    val enrollmentNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0,
    val specialization: String = "Land & Property Verification"
)

data class DocumentWriterProfileRequestDto(
    val licenseNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0
)

data class RefreshTokenRequest(
    val refreshToken: String
)

