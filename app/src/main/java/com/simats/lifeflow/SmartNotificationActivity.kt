package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class SmartNotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_notification)

        val ivClose = findViewById<ImageView>(R.id.iv_close)
        val btnContactNow = findViewById<AppCompatButton>(R.id.btn_contact_now)
        val btnViewMatches = findViewById<AppCompatButton>(R.id.btn_view_matches)

        ivClose.setOnClickListener {
            finish()
        }

        btnContactNow.setOnClickListener {
            val intent = Intent(this, PatientChatActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnViewMatches.setOnClickListener {
            val intent = Intent(this, AiMatchesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
