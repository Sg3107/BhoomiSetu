package com.satya.bhoomisetu.data.remote

import com.satya.bhoomisetu.data.remote.dto.AcceptRequestBody
import com.satya.bhoomisetu.data.remote.dto.ChatMessageDto
import com.satya.bhoomisetu.data.remote.dto.CreateSurveyRequestBody
import com.satya.bhoomisetu.data.remote.dto.SendMessageBody
import com.satya.bhoomisetu.data.remote.dto.SurveyRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SurveyRequestApiService {

    @POST("requests")
    suspend fun createRequest(
        @Header("Authorization") auth: String,
        @Body body: CreateSurveyRequestBody
    ): Response<SurveyRequestDto>

    @GET("requests")
    suspend fun getOpenRequests(
        @Header("Authorization") auth: String,
        @Query("district") district: String? = null,
        @Query("mandal") mandal: String? = null,
        @Query("status") status: String = "OPEN"
    ): Response<List<SurveyRequestDto>>

    @GET("requests/mine")
    suspend fun getMyRequests(
        @Header("Authorization") auth: String
    ): Response<List<SurveyRequestDto>>

    @GET("requests/assigned")
    suspend fun getAssignedRequests(
        @Header("Authorization") auth: String
    ): Response<List<SurveyRequestDto>>

    @POST("requests/{id}/accept")
    suspend fun acceptRequest(
        @Header("Authorization") auth: String,
        @Path("id") requestId: String,
        @Body body: AcceptRequestBody
    ): Response<SurveyRequestDto>

    @POST("requests/{id}/complete")
    suspend fun completeRequest(
        @Header("Authorization") auth: String,
        @Path("id") requestId: String
    ): Response<SurveyRequestDto>

    @GET("requests/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") auth: String,
        @Path("id") requestId: String
    ): Response<List<ChatMessageDto>>

    @POST("requests/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") auth: String,
        @Path("id") requestId: String,
        @Body body: SendMessageBody
    ): Response<ChatMessageDto>
}
