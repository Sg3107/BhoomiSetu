package com.satya.bhoomisetu.data.remote.dto

import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.SurveyRequest
import com.satya.bhoomisetu.domain.model.SurveyRequestStatus

// ── Request bodies ──────────────────────────────────────────────────────────

data class CreateSurveyRequestBody(
    val requesterName: String,
    val requesterPhone: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val description: String
)

data class AcceptRequestBody(val surveyorName: String)

data class SendMessageBody(val senderName: String, val text: String)

// ── Response DTOs ────────────────────────────────────────────────────────────

data class SurveyRequestDto(
    val id: String,
    val requesterId: String,
    val requesterName: String,
    val requesterPhone: String,
    val district: String,
    val mandal: String,
    val village: String?,
    val surveyNumber: String?,
    val description: String,
    val status: String,
    val surveyorId: String?,
    val surveyorName: String?,
    val surveyorPhone: String?,
    val createdAt: Long,
    val acceptedAt: Long?
) {
    fun toDomain(): SurveyRequest = SurveyRequest(
        id = id,
        requesterId = requesterId,
        requesterName = requesterName,
        requesterPhone = requesterPhone,
        district = district,
        mandal = mandal,
        village = village ?: "",
        surveyNumber = surveyNumber ?: "",
        description = description,
        status = runCatching { SurveyRequestStatus.valueOf(status) }.getOrDefault(SurveyRequestStatus.OPEN),
        surveyorId = surveyorId,
        surveyorName = surveyorName,
        surveyorPhone = surveyorPhone,
        createdAt = createdAt,
        acceptedAt = acceptedAt
    )
}

data class ChatMessageDto(
    val id: String,
    val requestId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val sentAt: Long
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        requestId = requestId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        sentAt = sentAt
    )
}
