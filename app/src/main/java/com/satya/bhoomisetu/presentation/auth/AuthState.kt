package com.satya.bhoomisetu.presentation.auth

import com.satya.bhoomisetu.domain.model.User

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
}
