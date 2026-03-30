package com.simats.lifeflow

/**
 * Model class for the Chatbot UI message representation.
 * Named ChatbotMessage to avoid conflict with the peer-to-peer ChatMessage in DataModels.kt
 */
data class ChatbotMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: String,
    val intent: String? = null,
    val quickReplies: List<String>? = null
)
