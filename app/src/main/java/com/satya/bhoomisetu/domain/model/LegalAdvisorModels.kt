package com.satya.bhoomisetu.domain.model

data class LegalAdvisorProfile(
    val enrollmentNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0,
    val specialization: String = "Land & Property Title Verification",
    val isAvailable: Boolean = true
)

data class DocumentWriterProfile(
    val licenseNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0,
    val isAvailable: Boolean = true
)

data class LegalAdvisorSummary(
    val id: String,
    val name: String,
    val enrollmentNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0,
    val specialization: String = "Land & Property Verification"
)
