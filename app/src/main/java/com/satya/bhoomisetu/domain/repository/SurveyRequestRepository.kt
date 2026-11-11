package com.satya.bhoomisetu.domain.repository

import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.SurveyRequest
import com.satya.bhoomisetu.util.Result

interface SurveyRequestRepository {
    /** USER submits a new survey request. */
    suspend fun createRequest(
        requesterName: String,
        requesterPhone: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        description: String
    ): Result<SurveyRequest>

    /** SURVEYOR fetches all OPEN requests (optionally filtered). */
    suspend fun getOpenRequests(district: String? = null, mandal: String? = null): Result<List<SurveyRequest>>

    /** USER fetches their own submitted requests. */
    suspend fun getMyRequests(): Result<List<SurveyRequest>>

    /** SURVEYOR fetches their accepted/assigned jobs. */
    suspend fun getAssignedRequests(): Result<List<SurveyRequest>>

    /** SURVEYOR accepts an OPEN request. */
    suspend fun acceptRequest(requestId: String, surveyorName: String): Result<SurveyRequest>

    /** Either party marks job as COMPLETED. */
    suspend fun completeRequest(requestId: String): Result<SurveyRequest>

    /** Load chat history for a request. */
    suspend fun getMessages(requestId: String): Result<List<ChatMessage>>

    /** Send a chat message. */
    suspend fun sendMessage(requestId: String, senderName: String, text: String): Result<ChatMessage>
}
