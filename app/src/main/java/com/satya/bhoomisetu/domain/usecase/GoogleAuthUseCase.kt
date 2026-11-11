package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.util.Result
import javax.inject.Inject

class GoogleAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String, role: UserRole = UserRole.USER): Result<User> {
        if (idToken.isBlank()) {
            return Result.Error(IllegalArgumentException("Google ID token cannot be blank."))
        }
        return authRepository.loginWithGoogle(idToken, role)
    }
}
