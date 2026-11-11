package com.satya.bhoomisetu.data.remote

import com.satya.bhoomisetu.data.remote.dto.CreateDocumentWriterRequestDto
import com.satya.bhoomisetu.data.remote.dto.DocumentWriterRequestDto
import com.satya.bhoomisetu.data.remote.dto.DocumentWriterSummaryDto
import com.satya.bhoomisetu.data.remote.dto.LegalChatMessageDto
import com.satya.bhoomisetu.data.remote.dto.SendWriterMessageDto
import com.satya.bhoomisetu.data.remote.dto.UploadClientDocumentResponseDto
import com.satya.bhoomisetu.data.remote.dto.UploadDraftDocumentResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface DocumentWriterApiService {

    @GET("document-writers")
    suspend fun getDocumentWriters(
        @Header("Authorization") token: String,
        @Query("district") district: String? = null
    ): Response<List<DocumentWriterSummaryDto>>

    @POST("document-writer-requests")
    suspend fun createWriterRequest(
        @Header("Authorization") token: String,
        @Body body: CreateDocumentWriterRequestDto
    ): Response<DocumentWriterRequestDto>

    @GET("document-writer-requests/mine")
    suspend fun getMyRequests(
        @Header("Authorization") token: String
    ): Response<List<DocumentWriterRequestDto>>

    @GET("document-writer-requests/writer")
    suspend fun getWriterRequests(
        @Header("Authorization") token: String
    ): Response<List<DocumentWriterRequestDto>>

    @GET("document-writer-requests/{id}")
    suspend fun getRequestDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DocumentWriterRequestDto>

    @POST("document-writer-requests/{id}/accept")
    suspend fun acceptRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DocumentWriterRequestDto>

    @POST("document-writer-requests/{id}/reject")
    suspend fun rejectRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DocumentWriterRequestDto>

    @Multipart
    @POST("document-writer-requests/{id}/documents")
    suspend fun uploadClientDocument(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("documentType") docType: RequestBody,
        @Part("title") title: RequestBody
    ): Response<UploadClientDocumentResponseDto>

    @Multipart
    @POST("document-writer-requests/{id}/drafts")
    suspend fun uploadDeedDraft(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("notes") notes: RequestBody,
        @Part("isFinal") isFinal: RequestBody
    ): Response<UploadDraftDocumentResponseDto>

    @POST("document-writer-requests/{id}/complete")
    suspend fun completeRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DocumentWriterRequestDto>

    @GET("document-writer-requests/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<List<LegalChatMessageDto>>

    @POST("document-writer-requests/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: SendWriterMessageDto
    ): Response<LegalChatMessageDto>
}
