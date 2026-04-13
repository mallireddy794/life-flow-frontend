package com.blooddonation.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : BaseActivity() {

    private var isPasswordVisible = false
    private val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPhone = findViewById<EditText>(R.id.et_phone)
        val etBlood = findViewById<EditText>(R.id.et_blood)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val ivShowPassword = findViewById<ImageView>(R.id.iv_show_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvLogin = findViewById<TextView>(R.id.tv_login)
        
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

        etBlood.isFocusable = false
        etBlood.isClickable = true
        etBlood.setOnClickListener {
            showBloodGroupDialog(etBlood)
        }

        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val bloodGroup = etBlood.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || bloodGroup.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return@setOnClickListener
            } else if (!isValidEmail(email)) {
                etEmail.error = "Enter valid email"
                etEmail.requestFocus()
                return@setOnClickListener
            } else {
                etEmail.error = null
                // Optional: show small confirmation toast for email validity as requested
                Toast.makeText(this, "Valid Email ✅", Toast.LENGTH_SHORT).show()
            }

            if (!isValidPhone(phone)) {
                etPhone.error = "Enter valid 10-digit phone number"
                etPhone.requestFocus()
                return@setOnClickListener
            } else {
                etPhone.error = null
            }
            
            if (!isValidPassword(password)) {
                etPassword.error = "Password must be at least 8 characters, with 1 uppercase, 1 special character, and 1 number"
                etPassword.requestFocus()
                return@setOnClickListener
            } else {
                etPassword.error = null
            }

            val role = intent.getStringExtra("role") ?: "donor"
            registerUser(name, email, phone, bloodGroup, password, role)
        }

        tvLogin.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showBloodGroupDialog(etBlood: EditText) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select blood group")
        builder.setItems(bloodGroups) { _, which ->
            etBlood.setText(bloodGroups[which])
        }
        builder.show()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        val pattern = java.util.regex.Pattern.compile(passwordPattern)
        return pattern.matcher(password).matches()
    }

    private fun registerUser(name: String, email: String, phone: String, bloodGroup: String, pass: String, role: String) {
        val signupData = mapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "blood_group" to bloodGroup,
            "password" to pass,
            "role" to role
        )

        Log.d("RegisterAPI", "Attempting signup at: /signup with data: $signupData")

        ApiClient.instance.signup(signupData).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, RegistrationSuccessActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegisterAPI", "Error Code: $code, Body: $errorBody")
                    
                    val message = try {
                        JSONObject(errorBody).getString("error")
                    } catch (e: Exception) {
                        response.message() ?: "Unknown error"
                    }
                    Toast.makeText(this@RegisterActivity, "Registration failed ($code): $message", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("RegisterAPI", "Network Failure", t)
                Toast.makeText(this@RegisterActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
