package com.satya.bhoomisetu.domain.model

data class ChatMessage(
    val id: String,
    val requestId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val sentAt: Long
)
