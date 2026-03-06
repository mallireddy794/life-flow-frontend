package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpVerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        val email = intent.getStringExtra("recovery_email") ?: ""

        val otps = arrayOf(
            findViewById<EditText>(R.id.et_otp1),
            findViewById<EditText>(R.id.et_otp2),
            findViewById<EditText>(R.id.et_otp3),
            findViewById<EditText>(R.id.et_otp4),
            findViewById<EditText>(R.id.et_otp5),
            findViewById<EditText>(R.id.et_otp6)
        )

        setupOtpInputs(otps)

        findViewById<Button>(R.id.btn_verify).setOnClickListener {
            val otpCode = otps.joinToString("") { it.text.toString() }
            if (otpCode.length == 6) {
                verifyOtp(email, otpCode)
            } else {
                Toast.makeText(this, "Please enter 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.tv_resend_otp).setOnClickListener {
            // Simplified resend for now
            Toast.makeText(this, "OTP Resent", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.widget.ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun verifyOtp(email: String, otp: String) {
        val verifyData = mapOf("email" to email, "otp" to otp)
        
        ApiClient.instance.verifyOtp(verifyData).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@OtpVerificationActivity, "OTP Verified", Toast.LENGTH_SHORT).show()
                    
                    val fromForgotPassword = intent.getBooleanExtra("from_forgot_password", false)
                    if (fromForgotPassword) {
                        val intent = Intent(this@OtpVerificationActivity, ResetPasswordActivity::class.java)
                        intent.putExtra("recovery_email", email)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@OtpVerificationActivity, RegistrationSuccessActivity::class.java)
                        startActivity(intent)
                    }
                    finish()
                } else {
                    Toast.makeText(this@OtpVerificationActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@OtpVerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupOtpInputs(otps: Array<EditText>) {
        for (i in otps.indices) {
            otps[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otps.size - 1) {
                        otps[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            otps[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && otps[i].text.isEmpty() && i > 0) {
                    otps[i - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }
    }
}
