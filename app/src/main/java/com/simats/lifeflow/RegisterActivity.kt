package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

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

            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && bloodGroup.isNotEmpty() && password.isNotEmpty()) {
                val role = intent.getStringExtra("role") ?: "donor"
                registerUser(name, email, phone, bloodGroup, password, role)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
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
