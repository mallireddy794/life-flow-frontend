package com.blooddonation.app

import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HospitalNotificationsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_notifications)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val rvNotifications = findViewById<RecyclerView>(R.id.rv_notifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        val notifications = listOf(
            HospitalNotification(
                "Critical Stock Alert",
                "AB- blood level below minimum threshold - Only 2 units remaining",
                "10 min ago",
                R.drawable.ic_exclamation_circle,
                R.color.blood_red,
                R.drawable.bg_icon_circle_pink
            ),
            HospitalNotification(
                "AI Prediction Alert",
                "O+ blood demand expected to increase by 25% next week",
                "30 min ago",
                R.drawable.ic_trending_up,
                R.color.warning_yellow,
                R.drawable.bg_icon_circle_yellow
            ),
            HospitalNotification(
                "New Blood Request",
                "5 new blood requests received - Review required",
                "1 hour ago",
                R.drawable.ic_person,
                R.color.info_blue,
                R.drawable.bg_icon_circle_blue
            ),
            HospitalNotification(
                "Donation Received",
                "10 units of O+ blood added to inventory",
                "2 hours ago",
                R.drawable.ic_blood_drop,
                R.color.success_green,
                R.drawable.bg_icon_circle_green
            ),
            HospitalNotification(
                "Stock Replenished",
                "A+ blood stock has been replenished - 25 units added",
                "3 hours ago",
                R.drawable.ic_hospital, // Placeholder for box
                R.color.success_green,
                R.drawable.bg_icon_circle_green
            ),
            HospitalNotification(
                "AI Insight Available",
                "New AI recommendations for optimal blood collection times",
                "5 hours ago",
                R.drawable.ic_brain,
                R.color.ai_purple,
                R.drawable.bg_icon_circle_purple
            ),
            HospitalNotification(
                "Donor Registration",
                "8 new donors registered in your area this week",
                "1 day ago",
                R.drawable.ic_person,
                R.color.success_green,
                R.drawable.bg_icon_circle_green
            )
        )

        rvNotifications.adapter = HospitalNotificationAdapter(notifications)
    }
}
