package com.blooddonation.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

class RoleSelectionActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val cardDonor = findViewById<LinearLayout>(R.id.card_donor)
        val cardPatient = findViewById<LinearLayout>(R.id.card_patient)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cardDonor.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("role", "donor")
            startActivity(intent)
        }

        cardPatient.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("role", "patient")
            startActivity(intent)
        }
    }
}
