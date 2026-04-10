package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

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
        
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty()) {
                    if (!isValidEmail(email)) {
                        etEmail.error = "Invalid email"
                    } else {
                        etEmail.error = null
                    }
                }
            }
        })

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

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                etEmail.error = "Enter valid email"
                etEmail.requestFocus()
                return@setOnClickListener
            } else {
                etEmail.error = null
                // Optional: show small confirmation toast for email validity as requested
                Toast.makeText(this, "Valid Email ✅", Toast.LENGTH_SHORT).show()
            }

            loginUser(email, password, role)
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

    private fun loginUser(email: String, pass: String, selectedRole: String) {
        val loginData = mapOf("email" to email, "password" to pass)
        ApiClient.instance.login(loginData).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    val actualRole = user?.role
                    
                    // Validate if the user's role matches the selected role
                    if (actualRole != null && actualRole != selectedRole) {
                        Toast.makeText(this@LoginActivity, "Invalid email: This account is registered as a $actualRole", Toast.LENGTH_LONG).show()
                        return
                    }
                    
                    Toast.makeText(this@LoginActivity, "Login Successful: ${user?.name}", Toast.LENGTH_SHORT).show()
                    
                    // Save user id to SharedPreferences for global access
                    val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().apply {
                        putInt("user_id", user?.id ?: -1)
                        putString("user_name", user?.name)
                        putString("user_role", actualRole)
                        putBoolean("is_profile_complete", user?.isProfileComplete ?: false)
                    }.apply()
                    
                    val nextIntent = Intent(this@LoginActivity, SubscriptionActivity::class.java)
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
