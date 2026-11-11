package com.satya.bhoomisetu.domain.repository

import com.satya.bhoomisetu.domain.model.SurveyorProfile
import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.util.Result
import kotlinx.coroutines.flow.Flow

class EmailNotVerifiedException(val email: String, message: String) : Exception(message)

interface AuthRepository {
    suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: UserRole,
        surveyorProfile: SurveyorProfile? = null,
        legalAdvisorProfile: com.satya.bhoomisetu.domain.model.LegalAdvisorProfile? = null,
        documentWriterProfile: com.satya.bhoomisetu.domain.model.DocumentWriterProfile? = null
    ): Result<String>

    suspend fun verifyEmailOtp(email: String, otp: String): Result<User>

    suspend fun resendEmailOtp(email: String): Result<String>

    suspend fun login(phoneOrEmail: String, password: String): Result<User>

    suspend fun loginWithGoogle(idToken: String, role: UserRole = UserRole.USER): Result<User>

    suspend fun logout()

    fun getCurrentUser(): Flow<User?>

    suspend fun getSurveyorProfile(userId: String): Result<SurveyorProfile?>
}
