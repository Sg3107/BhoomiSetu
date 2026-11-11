package com.satya.bhoomisetu.data.repository

import com.google.gson.Gson
import com.satya.bhoomisetu.data.local.dao.UserDao
import com.satya.bhoomisetu.data.local.entity.SurveyorProfileEntity
import com.satya.bhoomisetu.data.local.entity.UserEntity
import com.satya.bhoomisetu.data.remote.AuthApiService
import com.satya.bhoomisetu.data.remote.SessionManager
import com.satya.bhoomisetu.data.remote.dto.ErrorResponseDto
import com.satya.bhoomisetu.data.remote.dto.LoginRequest
import com.satya.bhoomisetu.data.remote.dto.RegisterRequest
import com.satya.bhoomisetu.data.remote.dto.SurveyorProfileRequestDto
import com.satya.bhoomisetu.domain.model.SurveyorProfile
import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.domain.model.VerificationStatus
import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.util.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val gson: Gson
) : AuthRepository {

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: UserRole,
        surveyorProfile: SurveyorProfile?,
        legalAdvisorProfile: com.satya.bhoomisetu.domain.model.LegalAdvisorProfile?,
        documentWriterProfile: com.satya.bhoomisetu.domain.model.DocumentWriterProfile?
    ): Result<String> {
        return try {
            val surveyorDto = surveyorProfile?.let {
                SurveyorProfileRequestDto(
                    licenseNumber = it.licenseNumber,
                    serviceAreas = it.serviceAreas,
                    yearsOfExperience = it.yearsOfExperience
                )
            }

            val legalAdvisorDto = legalAdvisorProfile?.let {
                com.satya.bhoomisetu.data.remote.dto.LegalAdvisorProfileRequestDto(
                    enrollmentNumber = it.enrollmentNumber,
                    districts = it.districts,
                    yearsOfExperience = it.yearsOfExperience,
                    specialization = it.specialization
                )
            }

            val documentWriterDto = documentWriterProfile?.let {
                com.satya.bhoomisetu.data.remote.dto.DocumentWriterProfileRequestDto(
                    licenseNumber = it.licenseNumber,
                    districts = it.districts,
                    yearsOfExperience = it.yearsOfExperience
                )
            }

            val request = RegisterRequest(
                name = name,
                phone = phone,
                email = email,
                password = password,
                role = role.name,
                surveyorProfile = surveyorDto,
                legalAdvisorProfile = legalAdvisorDto,
                documentWriterProfile = documentWriterDto
            )

            val response = authApiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.email)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.Error(Exception(e.localizedMessage ?: "Registration failed. Check network connection."))
        }
    }

    override suspend fun verifyEmailOtp(email: String, otp: String): Result<User> {
        return try {
            val response = authApiService.verifyEmailOtp(
                com.satya.bhoomisetu.data.remote.dto.VerifyEmailOtpRequest(email = email.trim(), otp = otp.trim())
            )
            if (response.isSuccessful && response.body() != null) {
                saveUserSession(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.Error(Exception(e.localizedMessage ?: "Failed to verify OTP"))
        }
    }

    override suspend fun resendEmailOtp(email: String): Result<String> {
        return try {
            val response = authApiService.resendEmailOtp(
                com.satya.bhoomisetu.data.remote.dto.ResendEmailOtpRequest(email = email.trim())
            )
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.message)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.Error(Exception(e.localizedMessage ?: "Failed to resend verification code"))
        }
    }

    override suspend fun loginWithGoogle(idToken: String, role: UserRole): Result<User> {
        return try {
            val response = authApiService.googleAuth(
                com.satya.bhoomisetu.data.remote.dto.GoogleAuthRequest(idToken = idToken, role = role.name)
            )
            if (response.isSuccessful && response.body() != null) {
                saveUserSession(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.Error(Exception(e.localizedMessage ?: "Google sign-in failed"))
        }
    }

    override suspend fun login(phoneOrEmail: String, password: String): Result<User> {
        return try {
            val cleaned = phoneOrEmail.trim()
            val req = if (cleaned.contains("@")) {
                LoginRequest(email = cleaned, emailOrPhone = cleaned, password = password)
            } else {
                LoginRequest(phone = cleaned, emailOrPhone = cleaned, password = password)
            }
            val response = authApiService.login(req)
            if (response.isSuccessful && response.body() != null) {
                saveUserSession(response.body()!!)
            } else {
                val errorBodyStr = response.errorBody()?.string()
                val parsedError = parseErrorResponse(errorBodyStr)
                if (response.code() == 403 && parsedError?.error?.code == "EMAIL_NOT_VERIFIED") {
                    val unverifiedEmail = parsedError.email ?: if (cleaned.contains("@")) cleaned else ""
                    Result.Error(com.satya.bhoomisetu.domain.repository.EmailNotVerifiedException(
                        email = unverifiedEmail,
                        message = parsedError.error.message
                    ))
                } else {
                    Result.Error(Exception(parsedError?.error?.message ?: "Invalid email or password"))
                }
            }
        } catch (e: Exception) {
            if (e is com.satya.bhoomisetu.domain.repository.EmailNotVerifiedException) {
                Result.Error(e)
            } else {
                fallbackLocalLogin(phoneOrEmail, e.localizedMessage ?: "Network error during login")
            }
        }
    }

    private suspend fun saveUserSession(body: com.satya.bhoomisetu.data.remote.dto.AuthResponse): Result<User> {
        val user = User(
            id = body.user.id,
            name = body.user.name,
            phone = body.user.phone,
            email = body.user.email,
            role = try { UserRole.valueOf(body.user.role) } catch (e: Exception) { UserRole.USER },
            createdAt = body.user.createdAt
        )

        // Cache in Room
        userDao.insertUser(UserEntity.fromDomainModel(user))
        body.surveyorProfile?.let { profileDto ->
            val profile = SurveyorProfile(
                userId = user.id,
                licenseNumber = profileDto.licenseNumber,
                serviceAreas = profileDto.serviceAreas,
                yearsOfExperience = profileDto.yearsOfExperience,
                idProofUrl = profileDto.idProofUrl,
                isAvailable = profileDto.isAvailable,
                verificationStatus = try { VerificationStatus.valueOf(profileDto.verificationStatus) } catch (e: Exception) { VerificationStatus.PENDING }
            )
            userDao.insertSurveyorProfile(SurveyorProfileEntity.fromDomainModel(profile))
        }

        // Save session
        sessionManager.saveSession(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            userId = user.id
        )

        return Result.Success(user)
    }

    override suspend fun logout() {
        sessionManager.clearSession()
        userDao.clearUsers()
        userDao.clearSurveyorProfiles()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCurrentUser(): Flow<User?> {
        return sessionManager.userIdFlow.flatMapLatest { userId ->
            if (userId.isNullOrEmpty()) {
                flowOf(null)
            } else {
                userDao.observeUserById(userId).map { entity ->
                    entity?.toDomainModel()
                }
            }
        }
    }

    override suspend fun getSurveyorProfile(userId: String): Result<SurveyorProfile?> {
        return try {
            val entity = userDao.getSurveyorProfileByUserId(userId)
            Result.Success(entity?.toDomainModel())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun fallbackLocalLogin(identifier: String, originalError: String): Result<User> {
        val userEntity = userDao.getUserByPhone(identifier)
        return if (userEntity != null) {
            val user = userEntity.toDomainModel()
            sessionManager.saveSession(
                accessToken = "mock_jwt_access_token_${user.id}",
                refreshToken = "mock_jwt_refresh_token_${user.id}",
                userId = user.id
            )
            Result.Success(user)
        } else {
            Result.Error(Exception("Unable to connect to server and no offline account found for $identifier. Please verify your credentials."))
        }
    }

    private fun parseErrorResponse(errorJson: String?): ErrorResponseDto? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            gson.fromJson(errorJson, ErrorResponseDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseErrorMessage(errorJson: String?): String {
        return parseErrorResponse(errorJson)?.error?.message ?: "Authentication failed"
    }
}
