package com.satya.bhoomisetu.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.satya.bhoomisetu.data.remote.DocumentWriterApiService
import com.satya.bhoomisetu.data.remote.SessionManager
import com.satya.bhoomisetu.data.remote.dto.CreateDocumentWriterRequestDto
import com.satya.bhoomisetu.data.remote.dto.SendWriterMessageDto
import com.satya.bhoomisetu.data.remote.dto.toDomain
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.DocumentWriterRequest
import com.satya.bhoomisetu.domain.model.DocumentWriterSummary
import com.satya.bhoomisetu.domain.repository.DocumentWriterRepository
import com.satya.bhoomisetu.util.Result
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentWriterRepositoryImpl @Inject constructor(
    private val api: DocumentWriterApiService,
    private val sessionManager: SessionManager
) : DocumentWriterRepository {

    private suspend fun bearerToken(): String {
        val token = sessionManager.accessTokenFlow.first() ?: ""
        return "Bearer $token"
    }

    private suspend fun <T, R> safeCall(
        call: suspend () -> Response<T>,
        map: (T) -> R
    ): Result<R> = try {
        val response = call()
        if (response.isSuccessful && response.body() != null) {
            Result.Success(map(response.body()!!))
        } else {
            val errorBody = response.errorBody()?.string()
            val message = parseErrorMessage(errorBody) ?: "Request failed with code ${response.code()}"
            Result.Error(Exception(message), message)
        }
    } catch (e: Exception) {
        Result.Error(e, e.localizedMessage ?: "Network error occurred")
    }

    private suspend fun <T, R> safeListCall(
        call: suspend () -> Response<List<T>>,
        map: (T) -> R
    ): Result<List<R>> = try {
        val response = call()
        if (response.isSuccessful && response.body() != null) {
            Result.Success(response.body()!!.map(map))
        } else {
            val errorBody = response.errorBody()?.string()
            val message = parseErrorMessage(errorBody) ?: "Request failed with code ${response.code()}"
            Result.Error(Exception(message), message)
        }
    } catch (e: Exception) {
        Result.Error(e, e.localizedMessage ?: "Network error occurred")
    }

    private fun parseErrorMessage(errorBody: String?): String? = try {
        if (!errorBody.isNullOrBlank()) {
            val json = JSONObject(errorBody)
            if (json.has("error")) {
                val errObj = json.getJSONObject("error")
                if (errObj.has("message")) errObj.getString("message") else null
            } else if (json.has("message")) {
                json.getString("message")
            } else null
        } else null
    } catch (e: Exception) {
        null
    }

    override suspend fun getDocumentWriters(district: String?): Result<List<DocumentWriterSummary>> =
        safeListCall(
            call = { api.getDocumentWriters(bearerToken(), district) },
            map = { it.toDomain() }
        )

    override suspend fun createWriterRequest(
        writerId: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        deedType: String,
        propertyTitle: String,
        description: String
    ): Result<DocumentWriterRequest> = safeCall(
        call = {
            api.createWriterRequest(
                bearerToken(),
                CreateDocumentWriterRequestDto(
                    writerId = writerId,
                    district = district,
                    mandal = mandal,
                    village = village,
                    surveyNumber = surveyNumber,
                    deedType = deedType,
                    propertyTitle = propertyTitle,
                    description = description
                )
            )
        },
        map = { it.toDomain() }
    )

    override suspend fun getMyRequests(): Result<List<DocumentWriterRequest>> =
        safeListCall(
            call = { api.getMyRequests(bearerToken()) },
            map = { it.toDomain() }
        )

    override suspend fun getWriterRequests(): Result<List<DocumentWriterRequest>> =
        safeListCall(
            call = { api.getWriterRequests(bearerToken()) },
            map = { it.toDomain() }
        )

    override suspend fun getRequestDetail(requestId: String): Result<DocumentWriterRequest> =
        safeCall(
            call = { api.getRequestDetail(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun acceptRequest(requestId: String): Result<DocumentWriterRequest> =
        safeCall(
            call = { api.acceptRequest(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun rejectRequest(requestId: String): Result<DocumentWriterRequest> =
        safeCall(
            call = { api.rejectRequest(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun uploadClientDocument(
        requestId: String,
        fileUri: Uri,
        documentType: String,
        title: String,
        context: Context
    ): Result<DocumentWriterRequest> = try {
        val contentResolver = context.contentResolver
        var fileName = "document_${System.currentTimeMillis()}"
        var mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"

        contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val foundName = cursor.getString(nameIndex)
                if (!foundName.isNullOrBlank()) fileName = foundName
            }
        }

        val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
        val bytes = inputStream?.readBytes() ?: ByteArray(0)
        inputStream?.close()

        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val bodyPart = MultipartBody.Part.createFormData("file", fileName, requestFile)
        val docTypeBody = documentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadClientDocument(bearerToken(), requestId, bodyPart, docTypeBody, titleBody)
        if (response.isSuccessful && response.body() != null) {
            Result.Success(response.body()!!.request.toDomain())
        } else {
            val errorBody = response.errorBody()?.string()
            val msg = parseErrorMessage(errorBody) ?: "Failed to upload document (HTTP ${response.code()})"
            Result.Error(Exception(msg), msg)
        }
    } catch (e: Exception) {
        Result.Error(e, e.localizedMessage ?: "Failed to read or upload file")
    }

    override suspend fun uploadDeedDraft(
        requestId: String,
        fileUri: Uri,
        title: String,
        notes: String,
        isFinal: Boolean,
        context: Context
    ): Result<DocumentWriterRequest> = try {
        val contentResolver = context.contentResolver
        var fileName = "deed_draft_${System.currentTimeMillis()}"
        var mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"

        contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val foundName = cursor.getString(nameIndex)
                if (!foundName.isNullOrBlank()) fileName = foundName
            }
        }

        val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
        val bytes = inputStream?.readBytes() ?: ByteArray(0)
        inputStream?.close()

        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val bodyPart = MultipartBody.Part.createFormData("file", fileName, requestFile)
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val notesBody = notes.toRequestBody("text/plain".toMediaTypeOrNull())
        val isFinalBody = isFinal.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadDeedDraft(bearerToken(), requestId, bodyPart, titleBody, notesBody, isFinalBody)
        if (response.isSuccessful && response.body() != null) {
            Result.Success(response.body()!!.request.toDomain())
        } else {
            val errorBody = response.errorBody()?.string()
            val msg = parseErrorMessage(errorBody) ?: "Failed to upload deed draft (HTTP ${response.code()})"
            Result.Error(Exception(msg), msg)
        }
    } catch (e: Exception) {
        Result.Error(e, e.localizedMessage ?: "Failed to read or upload deed draft")
    }

    override suspend fun completeRequest(requestId: String): Result<DocumentWriterRequest> =
        safeCall(
            call = { api.completeRequest(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun getMessages(requestId: String): Result<List<ChatMessage>> =
        safeListCall(
            call = { api.getMessages(bearerToken(), requestId) },
            map = {
                ChatMessage(
                    id = it.id,
                    requestId = it.requestId,
                    senderId = it.senderId,
                    senderName = it.senderName,
                    text = it.text,
                    sentAt = it.sentAt
                )
            }
        )

    override suspend fun sendMessage(
        requestId: String,
        text: String,
        senderName: String
    ): Result<ChatMessage> = safeCall(
        call = { api.sendMessage(bearerToken(), requestId, SendWriterMessageDto(text, senderName)) },
        map = {
            ChatMessage(
                id = it.id,
                requestId = it.requestId,
                senderId = it.senderId,
                senderName = it.senderName,
                text = it.text,
                sentAt = it.sentAt
            )
        }
    )
}
