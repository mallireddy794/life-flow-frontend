package com.simats.lifeflow

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LanguageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnApply = findViewById<Button>(R.id.btn_apply_language)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnApply.setOnClickListener {
            Toast.makeText(this, "Language settings applied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
