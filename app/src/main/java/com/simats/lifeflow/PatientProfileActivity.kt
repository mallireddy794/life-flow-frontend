package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class PatientProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val etFullName = findViewById<EditText>(R.id.et_full_name)
        val etPhone = findViewById<EditText>(R.id.et_phone)
        val etBloodType = findViewById<EditText>(R.id.et_blood_type)
        val etUnitsNeeded = findViewById<EditText>(R.id.et_units_needed)
        val etLocation = findViewById<EditText>(R.id.et_location)
        val btnContinue = findViewById<AppCompatButton>(R.id.btn_continue)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnContinue.setOnClickListener {
            val name = etFullName.text.toString()
            val phone = etPhone.text.toString()
            val bloodType = etBloodType.text.toString()
            val units = etUnitsNeeded.text.toString()
            val location = etLocation.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && bloodType.isNotEmpty() && units.isNotEmpty() && location.isNotEmpty()) {
                Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show()
                // Navigate to Patient Dashboard
                val intent = Intent(this, PatientDashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
