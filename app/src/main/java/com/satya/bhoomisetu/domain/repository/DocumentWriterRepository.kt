package com.satya.bhoomisetu.domain.repository

import android.content.Context
import android.net.Uri
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.DocumentWriterRequest
import com.satya.bhoomisetu.domain.model.DocumentWriterSummary
import com.satya.bhoomisetu.util.Result

interface DocumentWriterRepository {
    suspend fun getDocumentWriters(district: String?): Result<List<DocumentWriterSummary>>

    suspend fun createWriterRequest(
        writerId: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        deedType: String,
        propertyTitle: String,
        description: String
    ): Result<DocumentWriterRequest>

    suspend fun getMyRequests(): Result<List<DocumentWriterRequest>>

    suspend fun getWriterRequests(): Result<List<DocumentWriterRequest>>

    suspend fun getRequestDetail(requestId: String): Result<DocumentWriterRequest>

    suspend fun acceptRequest(requestId: String): Result<DocumentWriterRequest>

    suspend fun rejectRequest(requestId: String): Result<DocumentWriterRequest>

    suspend fun uploadClientDocument(
        requestId: String,
        fileUri: Uri,
        documentType: String,
        title: String,
        context: Context
    ): Result<DocumentWriterRequest>

    suspend fun uploadDeedDraft(
        requestId: String,
        fileUri: Uri,
        title: String,
        notes: String,
        isFinal: Boolean,
        context: Context
    ): Result<DocumentWriterRequest>

    suspend fun completeRequest(requestId: String): Result<DocumentWriterRequest>

    suspend fun getMessages(requestId: String): Result<List<ChatMessage>>

    suspend fun sendMessage(requestId: String, text: String, senderName: String): Result<ChatMessage>
}
