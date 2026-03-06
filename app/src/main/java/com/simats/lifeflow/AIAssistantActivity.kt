package com.simats.lifeflow

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AIAssistantActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_assistant)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSend = findViewById<CardView>(R.id.btn_send)
        
        // Quick Reply Buttons
        val qrHowOften = findViewById<TextView>(R.id.qr_how_often)
        val qrRequirements = findViewById<TextView>(R.id.qr_requirements)
        val qrNearby = findViewById<TextView>(R.id.qr_nearby)
        val qrEligibility = findViewById<TextView>(R.id.qr_eligibility)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSend.setOnClickListener {
            Toast.makeText(this, "Message sent to AI", Toast.LENGTH_SHORT).show()
        }

        qrHowOften.setOnClickListener {
            Toast.makeText(this, "You can donate every 3 months", Toast.LENGTH_SHORT).show()
        }

        qrRequirements.setOnClickListener {
            Toast.makeText(this, "Requirements: 18-65 years, >50kg", Toast.LENGTH_SHORT).show()
        }

        qrNearby.setOnClickListener {
            Toast.makeText(this, "Searching for hospitals nearby...", Toast.LENGTH_SHORT).show()
        }

        qrEligibility.setOnClickListener {
            Toast.makeText(this, "Opening eligibility quiz...", Toast.LENGTH_SHORT).show()
        }
    }
}
