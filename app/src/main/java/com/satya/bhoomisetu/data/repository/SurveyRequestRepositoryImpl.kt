package com.satya.bhoomisetu.data.repository

import com.satya.bhoomisetu.data.remote.SessionManager
import com.satya.bhoomisetu.data.remote.SurveyRequestApiService
import com.satya.bhoomisetu.data.remote.dto.AcceptRequestBody
import com.satya.bhoomisetu.data.remote.dto.CreateSurveyRequestBody
import com.satya.bhoomisetu.data.remote.dto.SendMessageBody
import com.satya.bhoomisetu.domain.model.ChatMessage
import com.satya.bhoomisetu.domain.model.SurveyRequest
import com.satya.bhoomisetu.domain.repository.SurveyRequestRepository
import com.satya.bhoomisetu.util.Result
import kotlinx.coroutines.flow.first
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyRequestRepositoryImpl @Inject constructor(
    private val api: SurveyRequestApiService,
    private val sessionManager: SessionManager
) : SurveyRequestRepository {

    private suspend fun bearerToken(): String {
        val token = sessionManager.accessTokenFlow.first() ?: ""
        return "Bearer $token"
    }

    override suspend fun createRequest(
        requesterName: String,
        requesterPhone: String,
        district: String,
        mandal: String,
        village: String,
        surveyNumber: String,
        description: String
    ): Result<SurveyRequest> = safeCall(
        call = {
            api.createRequest(
                auth = bearerToken(),
                body = CreateSurveyRequestBody(
                    requesterName = requesterName,
                    requesterPhone = requesterPhone,
                    district = district,
                    mandal = mandal,
                    village = village,
                    surveyNumber = surveyNumber,
                    description = description
                )
            )
        },
        map = { it.toDomain() }
    )

    override suspend fun getOpenRequests(district: String?, mandal: String?): Result<List<SurveyRequest>> =
        safeListCall(
            call = { api.getOpenRequests(auth = bearerToken(), district = district, mandal = mandal) },
            map = { it.toDomain() }
        )

    override suspend fun getMyRequests(): Result<List<SurveyRequest>> =
        safeListCall(
            call = { api.getMyRequests(auth = bearerToken()) },
            map = { it.toDomain() }
        )

    override suspend fun getAssignedRequests(): Result<List<SurveyRequest>> =
        safeListCall(
            call = { api.getAssignedRequests(auth = bearerToken()) },
            map = { it.toDomain() }
        )

    override suspend fun acceptRequest(requestId: String, surveyorName: String): Result<SurveyRequest> =
        safeCall(
            call = {
                api.acceptRequest(
                    auth = bearerToken(),
                    requestId = requestId,
                    body = AcceptRequestBody(surveyorName = surveyorName)
                )
            },
            map = { it.toDomain() }
        )

    override suspend fun completeRequest(requestId: String): Result<SurveyRequest> =
        safeCall(
            call = { api.completeRequest(auth = bearerToken(), requestId = requestId) },
            map = { it.toDomain() }
        )

    override suspend fun getMessages(requestId: String): Result<List<ChatMessage>> =
        safeListCall(
            call = { api.getMessages(auth = bearerToken(), requestId = requestId) },
            map = { it.toDomain() }
        )

    override suspend fun sendMessage(
        requestId: String,
        senderName: String,
        text: String
    ): Result<ChatMessage> = safeCall(
        call = {
            api.sendMessage(
                auth = bearerToken(),
                requestId = requestId,
                body = SendMessageBody(senderName = senderName, text = text)
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
