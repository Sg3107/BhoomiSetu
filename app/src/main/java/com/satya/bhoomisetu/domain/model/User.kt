package com.satya.bhoomisetu.domain.model

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    USER,
    SURVEYOR,
    LEGAL_ADVISOR,
    DOCUMENT_WRITER
}

