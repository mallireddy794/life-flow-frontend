package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class Onboarding2Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        val btnNext = findViewById<Button>(R.id.btn_next)
        val btnSkip = findViewById<TextView>(R.id.btn_skip)

        btnNext.setOnClickListener {
            val intent = Intent(this, Onboarding3Activity::class.java)
            startActivity(intent)
        }

        btnSkip.setOnClickListener {
            val intent = Intent(this, RoleSelectionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
