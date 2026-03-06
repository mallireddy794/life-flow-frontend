package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DonorSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_settings)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnLogout = findViewById<Button>(R.id.btn_logout)
        
        // Items
        val itemProfile = findViewById<LinearLayout>(R.id.item_profile)
        val itemRequirements = findViewById<LinearLayout>(R.id.item_requirements)
        val itemHistory = findViewById<LinearLayout>(R.id.item_history)
        val itemAvailability = findViewById<LinearLayout>(R.id.item_availability)
        val itemNotifications = findViewById<LinearLayout>(R.id.item_notifications)
        val itemPrivacy = findViewById<LinearLayout>(R.id.item_privacy)
        val itemLanguage = findViewById<LinearLayout>(R.id.item_language)
        val itemAccessibility = findViewById<LinearLayout>(R.id.item_accessibility)
        val itemHelp = findViewById<LinearLayout>(R.id.item_help)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        itemProfile.setOnClickListener {
            startActivity(Intent(this, DonorProfileActivity::class.java))
        }

        itemRequirements.setOnClickListener {
            startActivity(Intent(this, DonorRequirementsActivity::class.java))
        }

        itemHistory.setOnClickListener {
            startActivity(Intent(this, DonationHistoryActivity::class.java))
        }

        itemAvailability.setOnClickListener {
            startActivity(Intent(this, AvailabilityStatusActivity::class.java))
        }

        itemNotifications.setOnClickListener {
            startActivity(Intent(this, DonorNotificationsActivity::class.java))
        }

        itemPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyAndConsentActivity::class.java))
        }

        itemLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        itemAccessibility.setOnClickListener {
            startActivity(Intent(this, AccessibilityActivity::class.java))
        }

        itemHelp.setOnClickListener {
            startActivity(Intent(this, HelpAndSupportActivity::class.java))
        }

        btnLogout.setOnClickListener {
            startActivity(Intent(this, LogoutConfirmActivity::class.java))
        }
    }
}
