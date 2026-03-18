package com.simats.lifeflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InboxAdapter(private val onChatClick: (InboxItem) -> Unit) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    private var items = mutableListOf<InboxItem>()

    fun setItems(newItems: List<InboxItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.other_user?.name ?: "Unknown User"
        holder.tvLastMessage.text = item.last_message
        
        // Safely format time
        try {
            holder.tvTime.text = if (item.last_time.contains("T")) {
                item.last_time.substringAfter("T").substringBefore(".")
            } else if (item.last_time.contains(" ")) {
                item.last_time.substringAfter(" ").substringBeforeLast(":")
            } else {
                item.last_time
            }
        } catch (e: Exception) {
            holder.tvTime.text = item.last_time
        }
        
        if (item.unread_count > 0) {
            holder.tvUnread.visibility = View.VISIBLE
            holder.tvUnread.text = item.unread_count.toString()
        } else {
            holder.tvUnread.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onChatClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvUnread: TextView = itemView.findViewById(R.id.tv_unread_count)
    }
}
