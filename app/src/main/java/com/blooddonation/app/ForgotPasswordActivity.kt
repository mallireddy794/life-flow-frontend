package com.blooddonation.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import org.json.JSONObject
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
        
        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty()) {
                    if (!isValidEmail(email)) {
                        etInput.error = "Invalid email"
                    } else {
                        etInput.error = null
                    }
                }
            }
        })

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        tvLogin.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val email = etInput.text.toString().trim()
            if (email.isNotEmpty()) {
                if (isValidEmail(email)) {
                    etInput.error = null
                    // Optional: show small confirmation toast for email validity as requested
                    Toast.makeText(this, "Valid Email ✅", Toast.LENGTH_SHORT).show()
                    val role = intent.getStringExtra("role") ?: "donor"
                    // Use forgotPassword endpoint which is specifically for recovery
                    initiatePasswordRecovery(email, role)
                } else {
                    etInput.error = "Enter valid email"
                    etInput.requestFocus()
                }
            } else {
                etInput.error = "Email is required"
                etInput.requestFocus()
            }
        }
    }

    private fun initiatePasswordRecovery(email: String, role: String) {
        val data = mapOf("email" to email, "role" to role)
        
        Log.d("ForgotPassword", "Attempting recovery for $email ($role)")
        
        ApiClient.instance.sendOtp(data).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val message = response.body()?.get("message") ?: "Verification code sent"
                    Toast.makeText(this@ForgotPasswordActivity, message, Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(this@ForgotPasswordActivity, OtpVerificationActivity::class.java)
                    intent.putExtra("from_forgot_password", true)
                    intent.putExtra("recovery_email", email)
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("ForgotPassword", "Error response: $errorBody")
                    
                    val errorMessage = try {
                        JSONObject(errorBody).optString("error", "Server error")
                    } catch (e: Exception) {
                        response.message().ifEmpty { "Failed to send verification code" }
                    }
                    
                    Toast.makeText(this@ForgotPasswordActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("ForgotPassword", "Network failure", t)
                Toast.makeText(this@ForgotPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
