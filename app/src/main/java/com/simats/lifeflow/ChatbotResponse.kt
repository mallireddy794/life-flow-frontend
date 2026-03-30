package com.simats.lifeflow

import com.google.gson.annotations.SerializedName

/**
 * Model class for Chatbot API response
 */
data class ChatbotResponse(
    @SerializedName("status") val status: String,
    @SerializedName("intent") val intent: String,
    @SerializedName("reply") val reply: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("quick_replies") val quickReplies: List<String>?
)
