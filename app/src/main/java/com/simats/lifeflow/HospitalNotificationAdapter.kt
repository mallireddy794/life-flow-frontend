package com.simats.lifeflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class HospitalNotificationAdapter(private val notifications: List<HospitalNotification>) :
    RecyclerView.Adapter<HospitalNotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconBg: View = view.findViewById(R.id.iv_icon_bg)
        val icon: ImageView = view.findViewById(R.id.iv_icon)
        val title: TextView = view.findViewById(R.id.tv_notif_title)
        val description: TextView = view.findViewById(R.id.tv_notif_desc)
        val time: TextView = view.findViewById(R.id.tv_notif_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.description.text = notification.description
        holder.time.text = notification.time
        holder.icon.setImageResource(notification.iconRes)
        holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, notification.iconTint))
        holder.iconBg.setBackgroundResource(notification.bgRes)
    }

    override fun getItemCount() = notifications.size
}
