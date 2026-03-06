package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class HospitalDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_dashboard)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val ivNotifications = findViewById<ImageView>(R.id.iv_notifications)
        val ivSettings = findViewById<ImageView>(R.id.iv_settings)
        val cardBloodStock = findViewById<View>(R.id.card_blood_stock)
        val btnAiInsights = findViewById<AppCompatButton>(R.id.btn_ai_insights)
        val btnChatSupport = findViewById<AppCompatButton>(R.id.btn_chat_support)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cardBloodStock.setOnClickListener {
            startActivity(Intent(this, InventoryAnalyticsActivity::class.java))
        }

        val cardRequestsToday = findViewById<View>(R.id.card_requests_today)
        cardRequestsToday.setOnClickListener {
            startActivity(Intent(this, RequestHistoryActivity::class.java))
        }

        val cardAvailableDonors = findViewById<View>(R.id.card_available_donors)
        cardAvailableDonors.setOnClickListener {
            startActivity(Intent(this, NearbyDonorsActivity::class.java))
        }

        ivNotifications.setOnClickListener {
            startActivity(Intent(this, HospitalNotificationsActivity::class.java))
        }


        ivSettings.setOnClickListener {
            startActivity(Intent(this, HospitalSettingsActivity::class.java))
        }


        btnAiInsights.setOnClickListener {
            startActivity(Intent(this, AIInsightsActivity::class.java))
        }

        btnChatSupport.setOnClickListener {
            val intent = Intent(this, HospitalChatActivity::class.java)
            startActivity(intent)
        }
    }
}
