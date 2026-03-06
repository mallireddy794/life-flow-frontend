package com.simats.lifeflow

import androidx.room.*

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY id ASC")
    suspend fun getChatHistoryByChatId(chatId: String): List<ChatEntity>

    @Query("SELECT * FROM chat_messages WHERE (sender_id = :userId AND receiver_id = :otherId) OR (sender_id = :otherId AND receiver_id = :userId) ORDER BY id ASC")
    suspend fun getChatHistory(userId: Int, otherId: Int): List<ChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatEntity)
}
