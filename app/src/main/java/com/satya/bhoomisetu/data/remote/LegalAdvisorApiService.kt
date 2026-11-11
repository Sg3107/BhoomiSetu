package com.satya.bhoomisetu.data.remote

import com.satya.bhoomisetu.data.remote.dto.CreateLegalRequestDto
import com.satya.bhoomisetu.data.remote.dto.DocumentWriterSummaryDto
import com.satya.bhoomisetu.data.remote.dto.LegalAdvisorRequestDto
import com.satya.bhoomisetu.data.remote.dto.LegalAdvisorSummaryDto
import com.satya.bhoomisetu.data.remote.dto.LegalChatMessageDto
import com.satya.bhoomisetu.data.remote.dto.ReviewDocumentDto
import com.satya.bhoomisetu.data.remote.dto.SendLegalMessageDto
import com.satya.bhoomisetu.data.remote.dto.SubmitLegalReviewDto
import com.satya.bhoomisetu.data.remote.dto.UploadDocumentResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface LegalAdvisorApiService {

    @GET("legal-advisors")
    suspend fun getLegalAdvisors(
        @Header("Authorization") token: String,
        @Query("district") district: String? = null
    ): Response<List<LegalAdvisorSummaryDto>>

    @GET("document-writers")
    suspend fun getDocumentWriters(
        @Header("Authorization") token: String,
        @Query("district") district: String? = null
    ): Response<List<DocumentWriterSummaryDto>>

    @POST("legal-requests")
    suspend fun createLegalRequest(
        @Header("Authorization") token: String,
        @Body body: CreateLegalRequestDto
    ): Response<LegalAdvisorRequestDto>

    @GET("legal-requests/mine")
    suspend fun getMyLegalRequests(
        @Header("Authorization") token: String
    ): Response<List<LegalAdvisorRequestDto>>

    @GET("legal-requests/advisor")
    suspend fun getAdvisorRequests(
        @Header("Authorization") token: String
    ): Response<List<LegalAdvisorRequestDto>>

    @GET("legal-requests/{id}")
    suspend fun getLegalRequestDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LegalAdvisorRequestDto>

    @POST("legal-requests/{id}/accept")
    suspend fun acceptLegalRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LegalAdvisorRequestDto>

    @POST("legal-requests/{id}/reject")
    suspend fun rejectLegalRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LegalAdvisorRequestDto>

    @Multipart
    @POST("legal-requests/{id}/documents")
    suspend fun uploadDocument(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("documentType") docType: RequestBody,
        @Part("title") title: RequestBody
    ): Response<UploadDocumentResponseDto>

    @PATCH("legal-requests/{id}/documents/{docId}")
    suspend fun reviewDocument(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("docId") docId: String,
        @Body body: ReviewDocumentDto
    ): Response<LegalAdvisorRequestDto>

    @POST("legal-requests/{id}/review")
    suspend fun submitReview(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: SubmitLegalReviewDto
    ): Response<LegalAdvisorRequestDto>

    @GET("legal-requests/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<List<LegalChatMessageDto>>

    @POST("legal-requests/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: SendLegalMessageDto
    ): Response<LegalChatMessageDto>
}
