package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val role = intent.getStringExtra("role") ?: "donor"

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val ivShowPassword = findViewById<ImageView>(R.id.iv_show_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
        val tvCreateAccount = findViewById<TextView>(R.id.tv_create_account)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivShowPassword.setImageResource(R.drawable.ic_eye)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivShowPassword.setImageResource(R.drawable.ic_eye)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password, role)
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            intent.putExtra("role", role)
            startActivity(intent)
        }

        tvCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("role", role)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, pass: String, role: String) {
        val loginData = mapOf("email" to email, "password" to pass)
        ApiClient.instance.login(loginData).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    Toast.makeText(this@LoginActivity, "Login Successful: ${user?.name}", Toast.LENGTH_SHORT).show()
                    
                    // Save user id to SharedPreferences for global access
                    val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().apply {
                        putInt("user_id", user?.id ?: -1)
                        putString("user_name", user?.name)
                    }.apply()
                    
                    val userRole = user?.role ?: role
                    
                    val targetActivity = when (userRole) {
                        "donor" -> DonorProfileActivity::class.java
                        "patient" -> PatientDashboardActivity::class.java
                        else -> DonorProfileActivity::class.java
                    }
                    
                    val nextIntent = Intent(this@LoginActivity, targetActivity)
                    nextIntent.putExtra("user_id", user?.id)
                    startActivity(nextIntent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: Invalid credentials", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
