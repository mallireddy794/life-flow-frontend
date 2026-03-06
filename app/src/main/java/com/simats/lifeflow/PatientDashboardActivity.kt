package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val ivNotifications = findViewById<ImageView>(R.id.iv_notifications)
        val ivSettings = findViewById<ImageView>(R.id.iv_settings)
        val btnViewMap = findViewById<AppCompatButton>(R.id.btn_view_map)
        val btnTrackRequest = findViewById<AppCompatButton>(R.id.btn_track_request)
        val btnViewMatches = findViewById<AppCompatButton>(R.id.btn_view_matches)
        val btnEmergency = findViewById<AppCompatButton>(R.id.btn_emergency)
        val btnChat = findViewById<AppCompatButton>(R.id.btn_chat)
        val btnNearbyDonors = findViewById<LinearLayout>(R.id.btn_nearby_donors)
        val btnNearbyHospitals = findViewById<LinearLayout>(R.id.btn_nearby_hospitals)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnNearbyDonors.setOnClickListener {
            val intent = Intent(this, NearbyDonorsActivity::class.java)
            startActivity(intent)
        }

        btnNearbyHospitals.setOnClickListener {
            val intent = Intent(this, NearbyHospitalsActivity::class.java)
            startActivity(intent)
        }

        ivNotifications.setOnClickListener {
            val intent = Intent(this, PatientNotificationsActivity::class.java)
            startActivity(intent)
        }

        ivSettings.setOnClickListener {
            val intent = Intent(this, PatientSettingsActivity::class.java)
            startActivity(intent)
        }

        btnViewMap.setOnClickListener {
            val intent = Intent(this, PatientMapActivity::class.java)
            startActivity(intent)
        }

        btnTrackRequest.setOnClickListener {
            val intent = Intent(this, RequestTrackingActivity::class.java)
            startActivity(intent)
        }

        btnViewMatches.setOnClickListener {
            val intent = Intent(this, AiMatchesActivity::class.java)
            startActivity(intent)
        }

        btnEmergency.setOnClickListener {
            val intent = Intent(this, EmergencyRequestActivity::class.java)
            startActivity(intent)
        }

        btnChat.setOnClickListener {
            if (userId != -1) {
                fetchLatestDonorAndChat(userId)
            } else {
                Toast.makeText(this, "User session expired", Toast.LENGTH_SHORT).show()
            }
        }

        fetchDashboardStats()
    }

    private fun fetchDashboardStats() {
        ApiClient.instance.getDonors().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val donors = response.body() ?: emptyList()
                    findViewById<TextView>(R.id.tv_match_count).text = donors.size.toString()
                    findViewById<TextView>(R.id.tv_nearby_donors_count).text = donors.size.toString()
                    
                    // Update location name or other status if needed
                    if (donors.isNotEmpty()) {
                        findViewById<TextView>(R.id.tv_matched_status).text = "Donors Matched"
                        findViewById<TextView>(R.id.tv_searching).text = "Found ${donors.size} donors near you"
                    }
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // Silently fail or log
            }
        })
    }

    private fun fetchLatestDonorAndChat(userId: Int) {
        ApiClient.instance.getDonors().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val donors = response.body() ?: emptyList()
                    if (donors.isNotEmpty()) {
                        val latestDonor = donors.random()
                        val intent = Intent(this@PatientDashboardActivity, PatientChatActivity::class.java)
                        intent.putExtra("SENDER_ID", userId)
                        intent.putExtra("RECEIVER_ID", latestDonor.id)
                        intent.putExtra("RECEIVER_NAME", latestDonor.name)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@PatientDashboardActivity, "No donors available yet", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@PatientDashboardActivity, "Connection error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
