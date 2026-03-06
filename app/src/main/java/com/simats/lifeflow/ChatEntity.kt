package com.simats.lifeflow

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatEntity(
    @PrimaryKey val id: Int,
    val chat_id: String, // Matches the backend chat_id format "min_max"
    val sender_id: Int,
    val receiver_id: Int,
    val message: String,
    val created_at: String?
)
