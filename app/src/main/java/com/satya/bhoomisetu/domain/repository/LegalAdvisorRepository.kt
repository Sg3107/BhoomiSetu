package com.satya.bhoomisetu.domain.repository

import android.content.Context
import android.net.Uri
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.DocumentWriterProfile
import com.satya.bhoomisetu.domain.model.LegalAdvisorRequest
import com.satya.bhoomisetu.domain.model.LegalAdvisorSummary
import com.satya.bhoomisetu.util.Result

interface LegalAdvisorRepository {

    suspend fun getLegalAdvisors(district: String? = null): Result<List<LegalAdvisorSummary>>

    suspend fun getDocumentWriters(district: String? = null): Result<List<DocumentWriterProfile>>

    suspend fun createLegalRequest(
        advisorId: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        propertyTitle: String,
        description: String
    ): Result<LegalAdvisorRequest>

    suspend fun getMyLegalRequests(): Result<List<LegalAdvisorRequest>>

    suspend fun getAdvisorRequests(): Result<List<LegalAdvisorRequest>>

    suspend fun getLegalRequestDetail(requestId: String): Result<LegalAdvisorRequest>

    suspend fun acceptLegalRequest(requestId: String): Result<LegalAdvisorRequest>

    suspend fun rejectLegalRequest(requestId: String): Result<LegalAdvisorRequest>

    suspend fun uploadDocument(
        requestId: String,
        fileUri: Uri,
        documentType: String,
        title: String,
        context: Context
    ): Result<LegalAdvisorRequest>

    suspend fun reviewDocument(
        requestId: String,
        docId: String,
        verificationStatus: String,
        remarks: String
    ): Result<LegalAdvisorRequest>

    suspend fun submitReview(
        requestId: String,
        status: String,
        verificationSummary: String
    ): Result<LegalAdvisorRequest>

    suspend fun getMessages(requestId: String): Result<List<ChatMessage>>

    suspend fun sendMessage(requestId: String, text: String, senderName: String): Result<ChatMessage>
}
