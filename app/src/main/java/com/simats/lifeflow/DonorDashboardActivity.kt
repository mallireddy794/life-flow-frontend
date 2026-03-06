package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonorDashboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchAvailability: SwitchCompat
    private lateinit var tvAvailabilityStatus: TextView
    private lateinit var nearbyRequestsSection: LinearLayout
    private lateinit var rvNearbyRequests: RecyclerView
    private lateinit var adapter: NearbyRequestAdapter
    private var isInitializing = true
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_dashboard)

        // Try getting user_id from Intent first
        userId = intent.getIntExtra("user_id", -1)
        
        // Fallback to "app_prefs" then "user" prefs to be safe
        if (userId == -1) {
            val appPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            userId = appPrefs.getInt("user_id", -1)
        }
        if (userId == -1) {
            val userPrefs = getSharedPreferences("user", Context.MODE_PRIVATE)
            userId = userPrefs.getInt("user_id", -1)
        }

        sharedPreferences = getSharedPreferences("donor_prefs", Context.MODE_PRIVATE)

        // Initialize Views
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnNotification = findViewById<ImageView>(R.id.btn_notification)
        val btnSettings = findViewById<ImageView>(R.id.btn_settings)
        switchAvailability = findViewById(R.id.switch_availability)
        tvAvailabilityStatus = findViewById(R.id.tv_availability_status)
        nearbyRequestsSection = findViewById(R.id.nearby_requests_section)
        rvNearbyRequests = findViewById(R.id.rv_nearby_requests)
        
        val btnChatPatient = findViewById<Button>(R.id.btn_chat_patient)
        val btnViewRequirements = findViewById<Button>(R.id.btn_view_requirements)
        val btnHistory = findViewById<Button>(R.id.btn_history)
        val btnViewMap = findViewById<LinearLayout>(R.id.btn_view_map)
        val tvUserName = findViewById<TextView>(R.id.tv_user_name)

        // Set User Name
        val appPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userName = appPrefs.getString("user_name", "Donor")
        tvUserName.text = userName

        // Setup RecyclerView
        adapter = NearbyRequestAdapter(emptyList())
        rvNearbyRequests.adapter = adapter

        btnViewMap.setOnClickListener {
            val intent = Intent(this, DonorMapActivity::class.java)
            startActivity(intent)
        }

        // Load Saved State
        val isAvailable = sharedPreferences.getBoolean("donor_is_available", false)
        switchAvailability.isChecked = isAvailable
        updateUI(isAvailable)
        isInitializing = false

        // Switch Listener
        switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            if (!isInitializing) {
                saveAvailability(isChecked)
                updateUI(isChecked)
            }
        }

        // Click Listeners
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        btnNotification.setOnClickListener {
            val intent = Intent(this, DonorNotificationsActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, DonorSettingsActivity::class.java)
            startActivity(intent)
        }

        btnChatPatient.setOnClickListener {
            if (userId != -1) {
                fetchLatestPatientAndChat()
            } else {
                Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
            }
        }

        btnViewRequirements.setOnClickListener {
            val intent = Intent(this, DonorRequirementsActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, DonationHistoryActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isInitializing && switchAvailability.isChecked) {
            loadDummyRequests()
        }
    }

    private fun fetchLatestPatientAndChat() {
        ApiClient.instance.getPatients().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val patients = response.body() ?: emptyList()
                    if (patients.isNotEmpty()) {
                        val latestPatient = patients[0]
                        val intent = Intent(this@DonorDashboardActivity, PatientChatActivity::class.java)
                        intent.putExtra("SENDER_ID", userId)
                        intent.putExtra("RECEIVER_ID", latestPatient.id)
                        intent.putExtra("RECEIVER_NAME", latestPatient.name)
                        intent.putExtra("CHAT_STATUS", "CONNECTED")
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@DonorDashboardActivity, "No active patients found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@DonorDashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(isAvailable: Boolean) {
        if (isAvailable) {
            tvAvailabilityStatus.text = "Available to Donate Now"
            tvAvailabilityStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green))
            nearbyRequestsSection.visibility = View.VISIBLE
            loadDummyRequests()
        } else {
            tvAvailabilityStatus.text = "Not Available"
            tvAvailabilityStatus.setTextColor(ContextCompat.getColor(this, R.color.text_gray))
            nearbyRequestsSection.visibility = View.GONE
            adapter.updateList(emptyList())
        }
    }

    private fun saveAvailability(isAvailable: Boolean) {
        sharedPreferences.edit().putBoolean("donor_is_available", isAvailable).apply()
    }

    private fun loadDummyRequests() {
        val hospitals = listOf("City Hospital", "Red Cross Center", "St. Mary's Clinic", "General Hospital", "LifeLine Medical")
        val groups = listOf("O+", "A-", "B+", "AB-", "O-")
        val distances = listOf("1.2 km away", "2.5 km away", "3.8 km away", "4.0 km away", "5.2 km away")
        val urgencies = listOf("Urgent", "Normal", "Critical")

        val random = java.util.Random()
        val count = 2 + random.nextInt(3) // 2 to 4 requests
        
        val dummyData = mutableListOf<NearbyRequest>()
        for (i in 0 until count) {
            dummyData.add(
                NearbyRequest(
                    bloodGroup = groups[random.nextInt(groups.size)],
                    units = 1 + random.nextInt(3),
                    hospitalName = hospitals[random.nextInt(hospitals.size)],
                    location = distances[random.nextInt(distances.size)],
                    urgency = urgencies[random.nextInt(urgencies.size)],
                    patientId = 101 + i
                )
            )
        }
        
        adapter.updateList(dummyData)
        
        // Update Title with Count
        val tvNearbyTitle = findViewById<TextView>(R.id.tv_nearby_requests_title)
        tvNearbyTitle?.text = "Nearby Blood Requests (${dummyData.size})"
    }
}
