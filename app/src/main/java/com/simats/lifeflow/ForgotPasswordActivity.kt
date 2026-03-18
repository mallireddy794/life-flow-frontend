package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val etInput = findViewById<EditText>(R.id.et_recovery_input)
        val btnSend = findViewById<Button>(R.id.btn_send)
        val tvLogin = findViewById<TextView>(R.id.tv_login)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        tvLogin.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val email = etInput.text.toString().trim()
            if (email.isNotEmpty()) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // Assuming role is 'donor' by default for forgot password
                    // or you could get it from intent if needed.
                    val role = intent.getStringExtra("role") ?: "donor"
                    sendOtp(email, role)
                } else {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOtp(email: String, role: String) {
        val otpData = mapOf("email" to email, "role" to role)
        
        ApiClient.instance.sendOtp(otpData).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val message = response.body()?.get("message") ?: "OTP sent"
                    Toast.makeText(this@ForgotPasswordActivity, message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ForgotPasswordActivity, OtpVerificationActivity::class.java)
                    intent.putExtra("from_forgot_password", true)
                    intent.putExtra("recovery_email", email)
                    startActivity(intent)
                } else {
                    val errorMsg = "Failed to send OTP: ${response.message()}"
                    Toast.makeText(this@ForgotPasswordActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@ForgotPasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
