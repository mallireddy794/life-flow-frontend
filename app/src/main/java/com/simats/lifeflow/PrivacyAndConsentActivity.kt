package com.simats.lifeflow

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PrivacyAndConsentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_consent)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSave = findViewById<Button>(R.id.btn_save_privacy)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnSave.setOnClickListener {
            Toast.makeText(this, "Privacy preferences saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
