package com.satya.bhoomisetu.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.satya.bhoomisetu.data.remote.LegalAdvisorApiService
import com.satya.bhoomisetu.data.remote.SessionManager
import com.satya.bhoomisetu.data.remote.dto.CreateLegalRequestDto
import com.satya.bhoomisetu.data.remote.dto.DocumentWriterSummaryDto
import com.satya.bhoomisetu.data.remote.dto.LegalAdvisorRequestDto
import com.satya.bhoomisetu.data.remote.dto.LegalAdvisorSummaryDto
import com.satya.bhoomisetu.data.remote.dto.LegalChatMessageDto
import com.satya.bhoomisetu.data.remote.dto.LegalDocumentItemDto
import com.satya.bhoomisetu.data.remote.dto.ReviewDocumentDto
import com.satya.bhoomisetu.data.remote.dto.SendLegalMessageDto
import com.satya.bhoomisetu.data.remote.dto.SubmitLegalReviewDto
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.DocumentWriterProfile
import com.satya.bhoomisetu.domain.model.LegalAdvisorRequest
import com.satya.bhoomisetu.domain.model.LegalAdvisorSummary
import com.satya.bhoomisetu.domain.model.LegalDocumentItem
import com.satya.bhoomisetu.domain.repository.LegalAdvisorRepository
import com.satya.bhoomisetu.util.Result
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegalAdvisorRepositoryImpl @Inject constructor(
    private val api: LegalAdvisorApiService,
    private val sessionManager: SessionManager
) : LegalAdvisorRepository {

    private suspend fun bearerToken(): String {
        val token = sessionManager.accessTokenFlow.first() ?: ""
        return "Bearer $token"
    }

    override suspend fun getLegalAdvisors(district: String?): Result<List<LegalAdvisorSummary>> =
        safeListCall(
            call = { api.getLegalAdvisors(bearerToken(), district) },
            map = { it.toDomain() }
        )

    override suspend fun getDocumentWriters(district: String?): Result<List<DocumentWriterProfile>> =
        safeListCall(
            call = { api.getDocumentWriters(bearerToken(), district) },
            map = { it.toDomain() }
        )

    override suspend fun createLegalRequest(
        advisorId: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        propertyTitle: String,
        description: String
    ): Result<LegalAdvisorRequest> = safeCall(
        call = {
            api.createLegalRequest(
                token = bearerToken(),
                body = CreateLegalRequestDto(
                    advisorId = advisorId,
                    district = district,
                    mandal = mandal,
                    village = village,
                    surveyNumber = surveyNumber,
                    propertyTitle = propertyTitle,
                    description = description
                )
            )
        },
        map = { it.toDomain() }
    )

    override suspend fun getMyLegalRequests(): Result<List<LegalAdvisorRequest>> =
        safeListCall(
            call = { api.getMyLegalRequests(bearerToken()) },
            map = { it.toDomain() }
        )

    override suspend fun getAdvisorRequests(): Result<List<LegalAdvisorRequest>> =
        safeListCall(
            call = { api.getAdvisorRequests(bearerToken()) },
            map = { it.toDomain() }
        )

    override suspend fun getLegalRequestDetail(requestId: String): Result<LegalAdvisorRequest> =
        safeCall(
            call = { api.getLegalRequestDetail(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun acceptLegalRequest(requestId: String): Result<LegalAdvisorRequest> =
        safeCall(
            call = { api.acceptLegalRequest(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun rejectLegalRequest(requestId: String): Result<LegalAdvisorRequest> =
        safeCall(
            call = { api.rejectLegalRequest(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun uploadDocument(
        requestId: String,
        fileUri: Uri,
        documentType: String,
        title: String,
        context: Context
    ): Result<LegalAdvisorRequest> = try {
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

        val response = api.uploadDocument(bearerToken(), requestId, bodyPart, docTypeBody, titleBody)
        if (response.isSuccessful && response.body() != null) {
            Result.Success(response.body()!!.request.toDomain())
        } else {
            Result.Error(Exception(response.errorBody()?.string() ?: "Upload failed (${response.code()})"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun reviewDocument(
        requestId: String,
        docId: String,
        verificationStatus: String,
        remarks: String
    ): Result<LegalAdvisorRequest> = safeCall(
        call = {
            api.reviewDocument(
                token = bearerToken(),
                id = requestId,
                docId = docId,
                body = ReviewDocumentDto(verificationStatus, remarks)
            )
        },
        map = { it.toDomain() }
    )

    override suspend fun submitReview(
        requestId: String,
        status: String,
        verificationSummary: String
    ): Result<LegalAdvisorRequest> = safeCall(
        call = {
            api.submitReview(
                token = bearerToken(),
                id = requestId,
                body = SubmitLegalReviewDto(status, verificationSummary)
            )
        },
        map = { it.toDomain() }
    )

    override suspend fun getMessages(requestId: String): Result<List<ChatMessage>> =
        safeListCall(
            call = { api.getMessages(bearerToken(), requestId) },
            map = { it.toDomain() }
        )

    override suspend fun sendMessage(
        requestId: String,
        text: String,
        senderName: String
    ): Result<ChatMessage> = safeCall(
        call = {
            api.sendMessage(
                token = bearerToken(),
                id = requestId,
                body = SendLegalMessageDto(text = text, senderName = senderName)
            )
        },
        map = { it.toDomain() }
    )

    // ── Helpers ────────────────────────────────────────────────────────────

    private suspend fun <T, D> safeCall(
        call: suspend () -> Response<T>,
        map: (T) -> D
    ): Result<D> = try {
        val response = call()
        if (response.isSuccessful && response.body() != null) {
            Result.Success(map(response.body()!!))
        } else {
            val errorMsg = parseErrorMessage(response.errorBody()?.string(), response.code())
            Result.Error(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    private suspend fun <T, D> safeListCall(
        call: suspend () -> Response<List<T>>,
        map: (T) -> D
    ): Result<List<D>> = try {
        val response = call()
        if (response.isSuccessful) {
            Result.Success(response.body()?.map(map) ?: emptyList())
        } else {
            val errorMsg = parseErrorMessage(response.errorBody()?.string(), response.code())
            Result.Error(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    private fun parseErrorMessage(errorBody: String?, code: Int): String {
        if (errorBody.isNullOrBlank()) return "Request failed ($code)"
        return try {
            val json = org.json.JSONObject(errorBody)
            if (json.has("error")) {
                val errObj = json.optJSONObject("error")
                errObj?.optString("message", errorBody) ?: errorBody
            } else if (json.has("message")) {
                json.getString("message")
            } else {
                errorBody
            }
        } catch (e: Exception) {
            errorBody
        }
    }
}

// ── Mappers ────────────────────────────────────────────────────────────────

private fun LegalAdvisorSummaryDto.toDomain() = LegalAdvisorSummary(
    id = id,
    name = name,
    enrollmentNumber = enrollmentNumber,
    districts = districts,
    yearsOfExperience = yearsOfExperience,
    specialization = specialization
)

private fun DocumentWriterSummaryDto.toDomain() = DocumentWriterProfile(
    licenseNumber = licenseNumber,
    districts = districts,
    yearsOfExperience = yearsOfExperience,
    isAvailable = true
)

private fun LegalDocumentItemDto.toDomain() = LegalDocumentItem(
    id = id,
    documentType = documentType,
    title = title,
    originalFileName = originalFileName,
    fileUrl = fileUrl,
    fileSize = fileSize,
    mimeType = mimeType,
    uploadedByRole = uploadedByRole,
    uploadedAt = uploadedAt,
    verificationStatus = verificationStatus,
    remarks = remarks
)

private fun LegalAdvisorRequestDto.toDomain() = LegalAdvisorRequest(
    id = id,
    userId = userId,
    userName = userName,
    userPhone = userPhone,
    advisorId = advisorId,
    advisorName = advisorName,
    advisorPhone = advisorPhone,
    advisorEnrollment = advisorEnrollment,
    district = district,
    mandal = mandal,
    village = village,
    surveyNumber = surveyNumber,
    propertyTitle = propertyTitle,
    description = description,
    status = status,
    verificationSummary = verificationSummary,
    documents = documents.map { it.toDomain() },
    createdAt = createdAt,
    acceptedAt = acceptedAt,
    updatedAt = updatedAt
)

private fun LegalChatMessageDto.toDomain() = ChatMessage(
    id = id,
    requestId = requestId,
    senderId = senderId,
    senderName = senderName,
    text = text,
    sentAt = sentAt
)
