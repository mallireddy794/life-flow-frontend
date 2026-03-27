package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmergencyRequestActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_request)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val etPatientName = findViewById<EditText>(R.id.et_patient_name)
        val etBloodType = findViewById<EditText>(R.id.et_blood_type)
        val etUnits = findViewById<EditText>(R.id.et_units)
        val etHospitalName = findViewById<EditText>(R.id.et_hospital_name)
        val etLocation = findViewById<EditText>(R.id.et_location)
        val etContact = findViewById<EditText>(R.id.et_contact)
        val btnSend = findViewById<AppCompatButton>(R.id.btn_send_emergency)
        val switchEmergency = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_emergency)
        val pbLoading = findViewById<ProgressBar>(R.id.pb_loading)
        val tvStatus = findViewById<TextView>(R.id.tv_searching_status)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSend.setOnClickListener {
            val patientName = etPatientName.text.toString().trim()
            val bloodType = etBloodType.text.toString().trim()
            val unitsStr = etUnits.text.toString().trim()
            val hospitalName = etHospitalName.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val contact = etContact.text.toString().trim()

            if (patientName.isNotEmpty() && bloodType.isNotEmpty() && unitsStr.isNotEmpty() && 
                hospitalName.isNotEmpty() && location.isNotEmpty() && contact.isNotEmpty()) {
                
                val units = unitsStr.toIntOrNull() ?: 0
                val urgency = if (switchEmergency.isChecked) "URGENT" else "NORMAL"
                
                // Get userId
                val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = sharedPrefs.getInt("user_id", -1)

                if (userId == -1) {
                    Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Show loading
                btnSend.visibility = View.GONE
                pbLoading.visibility = View.VISIBLE
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "Sending request to database..."

                // Try to get location first to ensure it's visible in donor role (nearby)
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            val locationUpdate = LocationUpdate(userId, loc.latitude, loc.longitude)
                            ApiClient.instance.updateLocation(locationUpdate).enqueue(object : Callback<Map<String, String>> {
                                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                                    actuallyCreateRequest(userId, patientName, bloodType, units, urgency, location, hospitalName, contact, pbLoading, btnSend, tvStatus)
                                }
                                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                    actuallyCreateRequest(userId, patientName, bloodType, units, urgency, location, hospitalName, contact, pbLoading, btnSend, tvStatus)
                                }
                            })
                        } else {
                            actuallyCreateRequest(userId, patientName, bloodType, units, urgency, location, hospitalName, contact, pbLoading, btnSend, tvStatus)
                        }
                    }
                } else {
                    actuallyCreateRequest(userId, patientName, bloodType, units, urgency, location, hospitalName, contact, pbLoading, btnSend, tvStatus)
                }

            } else {
                Toast.makeText(this, "Please fill all blood requirements", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actuallyCreateRequest(userId: Int, patientName: String, bloodType: String, units: Int, urgency: String, city: String, hospital: String, contact: String, pbLoading: ProgressBar, btnSend: AppCompatButton, tvStatus: TextView) {
        val request = BloodRequest(
            patient_name = patientName,
            hospital_name = hospital,
            contact_number = contact,
            blood_group = bloodType,
            units_required = units,
            urgency_level = urgency,
            city = city
        )

        ApiClient.instance.createRequest(userId, request).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    tvStatus.text = "Request stored! Finding donors..."
                    android.util.Log.d("EmergencyRequest", "Request PERSISTED in database with status PENDING")
                    
                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                    handler.postDelayed({
                        val intent = Intent(this@EmergencyRequestActivity, PatientMapActivity::class.java)
                        intent.putExtra("BLOOD_TYPE", bloodType)
                        intent.putExtra("EMERGENCY_MODE", true)
                        intent.putExtra("UNITS_NEEDED", units)
                        startActivity(intent)
                        finish()
                    }, 500)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    android.util.Log.e("EmergencyRequest", "Server Error (${response.code()}): $errorBody")
                    Toast.makeText(this@EmergencyRequestActivity, "Server Error: $errorBody", Toast.LENGTH_LONG).show()
                    tvStatus.text = "Failure: Server rejected request"
                    btnSend.visibility = View.VISIBLE
                    pbLoading.visibility = View.GONE
                    tvStatus.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                btnSend.visibility = View.VISIBLE
                pbLoading.visibility = View.GONE
                tvStatus.text = "Connection error"
                Toast.makeText(this@EmergencyRequestActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
