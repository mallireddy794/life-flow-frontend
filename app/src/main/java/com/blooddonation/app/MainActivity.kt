package com.blooddonation.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fabNotifications = findViewById<FloatingActionButton>(R.id.fab_notifications)
        val fabSettings = findViewById<FloatingActionButton>(R.id.fab_settings)

        fabNotifications.setOnClickListener {
            startActivity(Intent(this, PatientNotificationsActivity::class.java))
        }

        fabSettings.setOnClickListener {
            startActivity(Intent(this, PatientSettingsActivity::class.java))
        }
    }
}