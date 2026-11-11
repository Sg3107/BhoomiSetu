package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.util.Result
import com.satya.bhoomisetu.util.Validators
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneOrEmail: String, password: String): Result<User> {
        val trimmed = phoneOrEmail.trim()
        val isValid = if (trimmed.contains("@")) {
            Validators.isValidEmail(trimmed)
        } else {
            Validators.isValidPhone(trimmed)
        }

        if (!isValid) {
            return Result.Error(IllegalArgumentException("Please enter a valid email address or 10-digit mobile number."))
        }
        if (!Validators.isValidPassword(password)) {
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters."))
        }
        return authRepository.login(phoneOrEmail = trimmed, password = password)
    }
}
