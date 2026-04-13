package com.blooddonation.app

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class PatientNotificationsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_notifications)

        val btnBack = findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        // Populate Mock Notifications
        setupNotification(
            findViewById(R.id.notif_1),
            "Blood Match Found!",
            "98% match - Donor John Smith is 1.2 km away",
            "2 min ago",
            R.drawable.ic_check,
            R.drawable.bg_icon_circle_green,
            R.color.success_green
        )

        setupNotification(
            findViewById(R.id.notif_2),
            "New Donor Available",
            "3 donors with O+ blood are available in your area",
            "15 min ago",
            R.drawable.ic_person,
            R.drawable.bg_icon_circle_blue,
            R.color.info_blue
        )

        setupNotification(
            findViewById(R.id.notif_3),
            "Donor Response",
            "A nearby donor accepted your blood request",
            "30 min ago",
            R.drawable.ic_chat,
            R.drawable.bg_icon_circle_green,
            R.color.success_green
        )

        setupNotification(
            findViewById(R.id.notif_4),
            "Request Update",
            "Your blood request is being processed",
            "1 hour ago",
            R.drawable.ic_clock,
            R.drawable.bg_icon_circle_blue,
            R.color.info_blue
        )

        setupNotification(
            findViewById(R.id.notif_5),
            "Emergency Alert Sent",
            "Your emergency request has been sent to 12 nearby donors",
            "2 hours ago",
            R.drawable.ic_bolt,
            R.drawable.bg_icon_circle_red,
            R.color.bright_red
        )
    }

    private fun setupNotification(
        view: LinearLayout,
        title: String,
        message: String,
        time: String,
        iconRes: Int,
        bgRes: Int,
        tintColorRes: Int
    ) {
        view.findViewById<TextView>(R.id.tv_notification_title).text = title
        view.findViewById<TextView>(R.id.tv_notification_message).text = message
        view.findViewById<TextView>(R.id.tv_notification_time).text = time
        
        val icon = view.findViewById<ImageView>(R.id.iv_notification_icon)
        icon.setImageResource(iconRes)
        icon.setColorFilter(ContextCompat.getColor(this, tintColorRes))
        
        view.findViewById<android.widget.FrameLayout>(R.id.layout_icon_bg).setBackgroundResource(bgRes)
    }
}
