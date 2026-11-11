package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.util.Result
import javax.inject.Inject

class VerifyEmailOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<User> {
        val trimmedOtp = otp.trim()
        if (trimmedOtp.length != 6 || !trimmedOtp.all { it.isDigit() }) {
            return Result.Error(IllegalArgumentException("Please enter the complete 6-digit OTP code."))
        }
        return authRepository.verifyEmailOtp(email.trim(), trimmedOtp)
    }
}
