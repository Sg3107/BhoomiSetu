package com.satya.bhoomisetu.domain.model

data class SurveyRequest(
    val id: String,
    val requesterId: String,
    val requesterName: String,
    val requesterPhone: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val description: String,
    val status: SurveyRequestStatus,
    val surveyorId: String?,
    val surveyorName: String?,
    val surveyorPhone: String?,
    val createdAt: Long,
    val acceptedAt: Long?
)

enum class SurveyRequestStatus {
    OPEN, ACCEPTED, COMPLETED, CANCELLED
}
