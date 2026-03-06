package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class PatientSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_settings)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnLogout = findViewById<AppCompatButton>(R.id.btn_logout)

        // Setting Items
        val itemProfile = findViewById<LinearLayout>(R.id.item_profile)
        val itemHistory = findViewById<LinearLayout>(R.id.item_history)
        val itemLocation = findViewById<LinearLayout>(R.id.item_location)
        val itemNotifications = findViewById<LinearLayout>(R.id.item_notifications)
        val itemPrivacy = findViewById<LinearLayout>(R.id.item_privacy)
        val itemLanguage = findViewById<LinearLayout>(R.id.item_language)
        val itemAccessibility = findViewById<LinearLayout>(R.id.item_accessibility)
        val itemHelp = findViewById<LinearLayout>(R.id.item_help)

        // Initialize Item Contents
        setupSettingItem(itemProfile, "Patient Profile", R.drawable.ic_person)
        setupSettingItem(itemHistory, "Request History", R.drawable.ic_document)
        setupSettingItem(itemLocation, "Location Preferences", R.drawable.ic_location)
        setupSettingItem(itemNotifications, "Notification Preferences", R.drawable.ic_bell)
        setupSettingItem(itemPrivacy, "Privacy & Security", R.drawable.ic_lock)
        setupSettingItem(itemLanguage, "Language", R.drawable.ic_globe)
        setupSettingItem(itemAccessibility, "Accessibility", R.drawable.ic_accessibility)
        setupSettingItem(itemHelp, "Help & Support", R.drawable.ic_help)

        btnBack.setOnClickListener {
            finish()
        }

        itemProfile.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
        }

        itemHistory.setOnClickListener {
            startActivity(Intent(this, RequestHistoryActivity::class.java))
        }

        itemLocation.setOnClickListener {
            startActivity(Intent(this, PatientMapActivity::class.java))
        }

        itemNotifications.setOnClickListener {
            startActivity(Intent(this, PatientNotificationsActivity::class.java))
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
            val intent = Intent(this, LogoutConfirmActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSettingItem(view: LinearLayout, title: String, iconRes: Int) {
        view.findViewById<TextView>(R.id.tv_item_title).text = title
        view.findViewById<ImageView>(R.id.iv_icon).setImageResource(iconRes)
    }
}
