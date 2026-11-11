package com.satya.bhoomisetu.data.remote.dto

import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.DocumentWriterRequest
import com.satya.bhoomisetu.domain.model.DocumentWriterSummary
import com.satya.bhoomisetu.domain.model.WriterClientDocument
import com.satya.bhoomisetu.domain.model.WriterDraftDocument

data class CreateDocumentWriterRequestDto(
    val writerId: String,
    val district: String,
    val mandal: String = "",
    val village: String = "",
    val surveyNumber: String = "",
    val deedType: String = "SALE_DEED",
    val propertyTitle: String,
    val description: String
)

data class WriterClientDocumentDto(
    val id: String,
    val documentType: String,
    val title: String,
    val originalFileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String,
    val uploadedAt: Long
)

data class WriterDraftDocumentDto(
    val id: String,
    val title: String,
    val originalFileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String,
    val notes: String = "",
    val uploadedAt: Long,
    val isFinal: Boolean = true
)

data class DocumentWriterRequestDto(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String?,
    val writerId: String,
    val writerName: String,
    val writerPhone: String?,
    val writerLicense: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val deedType: String,
    val propertyTitle: String,
    val description: String,
    val status: String,
    val clientDocuments: List<WriterClientDocumentDto> = emptyList(),
    val draftDocuments: List<WriterDraftDocumentDto> = emptyList(),
    val createdAt: Long,
    val acceptedAt: Long?,
    val completedAt: Long?,
    val updatedAt: Long
)

data class SendWriterMessageDto(
    val text: String,
    val senderName: String
)

data class UploadClientDocumentResponseDto(
    val message: String,
    val document: WriterClientDocumentDto,
    val request: DocumentWriterRequestDto
)

data class UploadDraftDocumentResponseDto(
    val message: String,
    val draft: WriterDraftDocumentDto,
    val request: DocumentWriterRequestDto
)

// Extension functions to domain models
fun DocumentWriterSummaryDto.toDomain() = DocumentWriterSummary(
    id = id,
    name = name,
    licenseNumber = licenseNumber,
    districts = districts,
    yearsOfExperience = yearsOfExperience
)

fun WriterClientDocumentDto.toDomain() = WriterClientDocument(
    id = id,
    documentType = documentType,
    title = title,
    originalFileName = originalFileName,
    fileUrl = fileUrl,
    fileSize = fileSize,
    mimeType = mimeType,
    uploadedAt = uploadedAt
)

fun WriterDraftDocumentDto.toDomain() = WriterDraftDocument(
    id = id,
    title = title,
    originalFileName = originalFileName,
    fileUrl = fileUrl,
    fileSize = fileSize,
    mimeType = mimeType,
    notes = notes,
    uploadedAt = uploadedAt,
    isFinal = isFinal
)

fun DocumentWriterRequestDto.toDomain() = DocumentWriterRequest(
    id = id,
    userId = userId,
    userName = userName,
    userPhone = userPhone,
    writerId = writerId,
    writerName = writerName,
    writerPhone = writerPhone,
    writerLicense = writerLicense,
    district = district,
    mandal = mandal,
    village = village,
    surveyNumber = surveyNumber,
    deedType = deedType,
    propertyTitle = propertyTitle,
    description = description,
    status = status,
    clientDocuments = clientDocuments.map { it.toDomain() },
    draftDocuments = draftDocuments.map { it.toDomain() },
    createdAt = createdAt,
    acceptedAt = acceptedAt,
    completedAt = completedAt,
    updatedAt = updatedAt
)
