package com.blooddonation.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button

class HospitalWelcomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_welcome)

        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
