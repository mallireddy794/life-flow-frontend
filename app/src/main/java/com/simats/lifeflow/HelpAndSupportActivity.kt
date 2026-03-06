package com.simats.lifeflow

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HelpAndSupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val cardAiChat = findViewById<androidx.cardview.widget.CardView>(R.id.card_ai_chat)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        cardAiChat.setOnClickListener {
            android.content.Intent(this, AIAssistantActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}
