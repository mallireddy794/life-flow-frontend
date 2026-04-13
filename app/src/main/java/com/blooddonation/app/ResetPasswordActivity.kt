package com.blooddonation.app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : BaseActivity() {

    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val ivShowNewPassword = findViewById<ImageView>(R.id.iv_show_new_password)
        val ivShowConfirmPassword = findViewById<ImageView>(R.id.iv_show_confirm_password)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        val email = intent.getStringExtra("recovery_email") ?: ""
        val otp = intent.getStringExtra("otp_code") ?: ""
        Log.d("ResetPassword", "Email received: $email, OTP received: $otp")

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        ivShowNewPassword.setOnClickListener {
            isNewPasswordVisible = !isPasswordVisible(etNewPassword)
            togglePasswordVisibility(etNewPassword, ivShowNewPassword, isNewPasswordVisible)
        }

        ivShowConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isPasswordVisible(etConfirmPassword)
            togglePasswordVisibility(etConfirmPassword, ivShowConfirmPassword, isConfirmPasswordVisible)
        }

        btnSubmit.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(newPassword)) {
                Toast.makeText(this, "Password must be at least 8 characters, with 1 uppercase, 1 special character, and 1 number", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Session error: Email not found. Please try again from Forgot Password.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            updatePasswordInBackend(email, otp, newPassword)
        }
    }

    private fun isPasswordVisible(editText: EditText): Boolean {
        return editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        val pattern = java.util.regex.Pattern.compile(passwordPattern)
        return pattern.matcher(password).matches()
    }

    private fun updatePasswordInBackend(email: String, otp: String, newPass: String) {
        val resetData = mutableMapOf(
            "email" to email,
            "new_password" to newPass
        )

        if (otp.isNotEmpty()) {
            resetData["otp"] = otp
        }

        Log.d("ResetPassword", "Attempting password reset for: $email")
        Log.d("ResetPassword", "Request body: $resetData")

        ApiClient.instance.resetPassword(resetData).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ResetPasswordActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string() ?: ""
                        Log.e("ResetPassword", "Error Body: $errorBody")
                        when {
                            errorBody.contains("error") -> org.json.JSONObject(errorBody).getString("error")
                            errorBody.contains("message") -> org.json.JSONObject(errorBody).getString("message")
                            else -> response.message() ?: "Unknown error"
                        }
                    } catch (e: Exception) {
                        response.message() ?: "Unknown error"
                    }

                    Log.e("ResetPassword", "Reset failed: $errorMsg")
                    Toast.makeText(this@ResetPasswordActivity, "Reset failed: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("ResetPassword", "Network Error", t)
                Toast.makeText(this@ResetPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text.length)
    }
}
