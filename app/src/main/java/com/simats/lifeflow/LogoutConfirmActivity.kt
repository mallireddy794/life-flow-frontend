package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class LogoutConfirmActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout_confirm)

        val btnConfirm = findViewById<Button>(R.id.btn_confirm_logout)
        val btnCancel = findViewById<Button>(R.id.btn_cancel_logout)

        btnConfirm.setOnClickListener {
            // Redirect to Role Selection / Login
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}
