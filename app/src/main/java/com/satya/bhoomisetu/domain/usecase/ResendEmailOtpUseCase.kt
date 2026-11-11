package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.util.Result
import javax.inject.Inject

class ResendEmailOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<String> {
        return authRepository.resendEmailOtp(email.trim())
    }
}
