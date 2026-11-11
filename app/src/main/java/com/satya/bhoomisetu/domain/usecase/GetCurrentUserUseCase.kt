package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return authRepository.getCurrentUser()
    }
}
