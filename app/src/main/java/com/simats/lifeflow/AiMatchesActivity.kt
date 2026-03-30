package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AiMatchesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_matches)

        val ivBack = findViewById<ImageView>(R.id.iv_back)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fetchDonors()
    }

    private fun fetchDonors() {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)
        val userBloodGroup = sharedPrefs.getString("blood_group", "ALL") ?: "ALL"
        
        // Use GPS to get current location
        val fused = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    callEmergencyApi(userId, userBloodGroup, loc.latitude, loc.longitude)
                } else {
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callEmergencyApi(userId: Int, bloodGroup: String, lat: Double, lng: Double) {
        val req = EmergencySearchRequest(
            patientId = userId,
            bloodGroup = bloodGroup,
            lat = lat,
            lng = lng,
            unitsRequired = 1,
            radiusKm = 10.0
        )

        ApiClient.instance.emergencyDonors(req).enqueue(object : Callback<EmergencySearchResponse> {
            override fun onResponse(call: Call<EmergencySearchResponse>, response: Response<EmergencySearchResponse>) {
                if (response.isSuccessful) {
                    val rankedDonors = response.body()?.nearbyDonors ?: emptyList()
                    setupDonorsList(rankedDonors)
                } else {
                    Toast.makeText(this@AiMatchesActivity, "AI search failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EmergencySearchResponse>, t: Throwable) {
                Toast.makeText(this@AiMatchesActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDonorsList(donors: List<RankedDonor>) {
        val tvName1 = findViewById<TextView>(R.id.tv_donor1_name)
        val tvName2 = findViewById<TextView>(R.id.tv_donor2_name)
        val tvName3 = findViewById<TextView>(R.id.tv_donor3_name)
        
        val btnContact1 = findViewById<AppCompatButton>(R.id.btn_contact1)
        val btnContact2 = findViewById<AppCompatButton>(R.id.btn_contact2)
        val btnContact3 = findViewById<AppCompatButton>(R.id.btn_contact3)

        if (donors.isNotEmpty()) {
            tvName1.text = donors[0].name
            btnContact1.setOnClickListener { openChat(donors[0].donorId, donors[0].name ?: "Donor") }
        }
        if (donors.size > 1) {
            tvName2.text = donors[1].name
            btnContact2.setOnClickListener { openChat(donors[1].donorId, donors[1].name ?: "Donor") }
        }
        if (donors.size > 2) {
            tvName3.text = donors[2].name
            btnContact3.setOnClickListener { openChat(donors[2].donorId, donors[2].name ?: "Donor") }
        }
    }

    private fun openChat(receiverId: Int, receiverName: String) {
        val intent = Intent(this, PatientChatActivity::class.java)
        intent.putExtra("RECEIVER_ID", receiverId)
        intent.putExtra("RECEIVER_NAME", receiverName)
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedUserId = sharedPrefs.getInt("user_id", -1)
        intent.putExtra("SENDER_ID", savedUserId) 
        startActivity(intent)
    }
}
