package com.satya.bhoomisetu.data.remote

import com.satya.bhoomisetu.data.remote.dto.AuthResponse
import com.satya.bhoomisetu.data.remote.dto.GoogleAuthRequest
import com.satya.bhoomisetu.data.remote.dto.LoginRequest
import com.satya.bhoomisetu.data.remote.dto.RefreshTokenRequest
import com.satya.bhoomisetu.data.remote.dto.RegisterRequest
import com.satya.bhoomisetu.data.remote.dto.RegisterResponseDto
import com.satya.bhoomisetu.data.remote.dto.ResendEmailOtpRequest
import com.satya.bhoomisetu.data.remote.dto.SimpleMessageResponseDto
import com.satya.bhoomisetu.data.remote.dto.UserDto
import com.satya.bhoomisetu.data.remote.dto.VerifyEmailOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponseDto>

    @POST("auth/verify-email-otp")
    suspend fun verifyEmailOtp(@Body request: VerifyEmailOtpRequest): Response<AuthResponse>

    @POST("auth/resend-email-otp")
    suspend fun resendEmailOtp(@Body request: ResendEmailOtpRequest): Response<SimpleMessageResponseDto>

    @POST("auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") authHeader: String): Response<UserDto>
}
