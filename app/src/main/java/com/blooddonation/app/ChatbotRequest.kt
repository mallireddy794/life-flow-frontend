package com.blooddonation.app

import com.google.gson.annotations.SerializedName

/**
 * Model class for Chatbot API request
 */
data class ChatbotRequest(
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("role") val role: String?,
    @SerializedName("message") val message: String
)
