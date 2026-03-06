package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class HospitalSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_settings)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnLogout = findViewById<AppCompatButton>(R.id.btn_logout)

        val itemProfile = findViewById<LinearLayout>(R.id.item_profile)
        val itemInventory = findViewById<LinearLayout>(R.id.item_inventory)
        val itemAiSettings = findViewById<LinearLayout>(R.id.item_ai_settings)
        val itemAlerts = findViewById<LinearLayout>(R.id.item_alerts)
        val itemSecurity = findViewById<LinearLayout>(R.id.item_security)
        val itemLanguage = findViewById<LinearLayout>(R.id.item_language)
        val itemAccessibility = findViewById<LinearLayout>(R.id.item_accessibility)
        val itemHelp = findViewById<LinearLayout>(R.id.item_help)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnLogout.setOnClickListener {
            // For now, just show a toast and go to Login
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        itemProfile.setOnClickListener {
            Toast.makeText(this, "Hospital Profile clicked", Toast.LENGTH_SHORT).show()
        }

        itemInventory.setOnClickListener {
            startActivity(Intent(this, InventoryAnalyticsActivity::class.java))
        }

        itemAiSettings.setOnClickListener {
            startActivity(Intent(this, AIInsightsActivity::class.java))
        }

        itemAlerts.setOnClickListener {
            Toast.makeText(this, "Alert Preferences clicked", Toast.LENGTH_SHORT).show()
        }

        itemSecurity.setOnClickListener {
            Toast.makeText(this, "Security & Compliance clicked", Toast.LENGTH_SHORT).show()
        }

        itemLanguage.setOnClickListener {
            Toast.makeText(this, "Language clicked", Toast.LENGTH_SHORT).show()
        }

        itemAccessibility.setOnClickListener {
            Toast.makeText(this, "Accessibility clicked", Toast.LENGTH_SHORT).show()
        }

        itemHelp.setOnClickListener {
            Toast.makeText(this, "Help & Support clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
