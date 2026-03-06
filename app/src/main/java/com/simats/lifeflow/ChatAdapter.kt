package com.simats.lifeflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val currentUserId: Int) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var messages = mutableListOf<ChatMessage>()

    fun setMessages(newMessages: List<ChatMessage>) {
        this.messages = newMessages.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        // Return 0 for sent, 1 for received
        return if (messages[position].sender_id == currentUserId) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == 0) R.layout.item_chat_message_sent else R.layout.item_chat_message_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.message
        
        // WhatsApp-style Time Formatting
        val rawTime = message.created_at
        holder.tvTime.text = formatTime(rawTime)
    }

    private fun formatTime(rawTime: String?): String {
        if (rawTime.isNullOrEmpty()) return ""
        return try {
            if (rawTime.startsWith("T")) {
                // Temporary optimistic timestamp
                rawTime.substring(1)
            } else if (rawTime.contains("T")) {
                // ISO format: 2023-10-27T14:13:00
                val timePart = rawTime.substringAfter("T")
                if (timePart.contains(":")) {
                    val parts = timePart.split(":")
                    if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
                } else {
                    timePart
                }
            } else {
                rawTime
            }
        } catch (e: Exception) {
            rawTime ?: ""
        }
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }
}
