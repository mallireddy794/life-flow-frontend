package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class EmergencyRequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_request)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val etPatientName = findViewById<EditText>(R.id.et_patient_name)
        val etBloodType = findViewById<EditText>(R.id.et_blood_type)
        val etUnits = findViewById<EditText>(R.id.et_units)
        val etHospitalName = findViewById<EditText>(R.id.et_hospital_name)
        val etLocation = findViewById<EditText>(R.id.et_location)
        val etContact = findViewById<EditText>(R.id.et_contact)
        val btnSend = findViewById<AppCompatButton>(R.id.btn_send_emergency)
        val switchEmergency = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_emergency)
        val pbLoading = findViewById<android.widget.ProgressBar>(R.id.pb_loading)
        val tvStatus = findViewById<android.widget.TextView>(R.id.tv_searching_status)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSend.setOnClickListener {
            val patientName = etPatientName.text.toString()
            val bloodType = etBloodType.text.toString()
            val units = etUnits.text.toString()
            val hospitalName = etHospitalName.text.toString()
            val location = etLocation.text.toString()
            val contact = etContact.text.toString()

            if (patientName.isNotEmpty() && bloodType.isNotEmpty() && units.isNotEmpty() && 
                hospitalName.isNotEmpty() && location.isNotEmpty() && contact.isNotEmpty()) {
                
                // Start Searching UI
                btnSend.visibility = android.view.View.GONE
                pbLoading.visibility = android.view.View.VISIBLE
                tvStatus.visibility = android.view.View.VISIBLE
                tvStatus.text = "Detecting GPS location..."

                // Simulate search delays
                tvStatus.postDelayed({ tvStatus.text = "Searching for $bloodType donors in 5 KM..." }, 1000)
                tvStatus.postDelayed({ tvStatus.text = "Matching donors found!" }, 2500)
                
                tvStatus.postDelayed({
                    Toast.makeText(this, "3 Donors found nearby!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PatientMapActivity::class.java)
                    intent.putExtra("BLOOD_TYPE", bloodType)
                    intent.putExtra("EMERGENCY_MODE", switchEmergency.isChecked)
                    intent.putExtra("UNITS", units)
                    startActivity(intent)
                    finish()
                }, 3500)

            } else {
                Toast.makeText(this, "Please fill all blood requirements", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
