package com.satya.bhoomisetu.data.remote.dto

data class LegalAdvisorSummaryDto(
    val id: String,
    val name: String,
    val enrollmentNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0,
    val specialization: String = "Land & Property Verification"
)

data class DocumentWriterSummaryDto(
    val id: String,
    val name: String,
    val licenseNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0
)

data class CreateLegalRequestDto(
    val advisorId: String,
    val district: String,
    val mandal: String = "",
    val village: String = "",
    val surveyNumber: String = "",
    val propertyTitle: String,
    val description: String
)

data class LegalDocumentItemDto(
    val id: String,
    val documentType: String,
    val title: String,
    val originalFileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String,
    val uploadedByRole: String,
    val uploadedAt: Long,
    val verificationStatus: String,
    val remarks: String
)

data class LegalAdvisorRequestDto(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String?, // Redacted if PENDING
    val advisorId: String,
    val advisorName: String,
    val advisorPhone: String?, // Redacted if PENDING
    val advisorEnrollment: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val propertyTitle: String,
    val description: String,
    val status: String,
    val verificationSummary: String,
    val documents: List<LegalDocumentItemDto>,
    val createdAt: Long,
    val acceptedAt: Long?,
    val updatedAt: Long
)

data class ReviewDocumentDto(
    val verificationStatus: String,
    val remarks: String
)

data class SubmitLegalReviewDto(
    val status: String,
    val verificationSummary: String
)

data class LegalChatMessageDto(
    val id: String,
    val requestId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val sentAt: Long
)

data class SendLegalMessageDto(
    val text: String,
    val senderName: String
)

data class UploadDocumentResponseDto(
    val message: String,
    val document: LegalDocumentItemDto,
    val request: LegalAdvisorRequestDto
)
