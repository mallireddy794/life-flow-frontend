package com.blooddonation.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        val etFullName = findViewById<EditText>(R.id.et_full_name)
        val etPhone = findViewById<EditText>(R.id.et_phone)
        val etBloodType = findViewById<EditText>(R.id.et_blood_type)
        val etUnitsNeeded = findViewById<EditText>(R.id.et_units_needed)
        val etLocation = findViewById<EditText>(R.id.et_location)
        val btnContinue = findViewById<AppCompatButton>(R.id.btn_continue)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = intent.getIntExtra("user_id", prefs.getInt("user_id", -1))

        btnContinue.setOnClickListener {
            val name = etFullName.text.toString()
            val phone = etPhone.text.toString()
            val bloodType = etBloodType.text.toString()
            val units = etUnitsNeeded.text.toString()
            val location = etLocation.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && bloodType.isNotEmpty() && units.isNotEmpty() && location.isNotEmpty()) {
                val profile = PatientProfile(
                    phone = phone,
                    blood_group = bloodType,
                    hospital_name = location,
                    city = location
                )

                ApiClient.instance.updatePatientProfile(userId, profile).enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@PatientProfileActivity, "Profile Saved Successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PatientProfileActivity, PatientDashboardActivity::class.java)
                            intent.putExtra("user_id", userId)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@PatientProfileActivity, "Failed to save profile on server", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        Toast.makeText(this@PatientProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
