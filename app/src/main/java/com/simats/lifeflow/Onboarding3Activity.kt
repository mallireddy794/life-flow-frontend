package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class Onboarding3Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding3)

        val btnGetStarted = findViewById<Button>(R.id.btn_get_started)

        btnGetStarted.setOnClickListener {
            // Final step - go to Role Selection
            val intent = Intent(this, RoleSelectionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
