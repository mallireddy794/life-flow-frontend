package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import android.os.Looper
import android.location.Location
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DonorDashboardActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchAvailability: SwitchCompat
    private lateinit var tvAvailabilityStatus: TextView
    private lateinit var nearbyRequestsSection: ConstraintLayout
    private lateinit var rvNearbyRequests: RecyclerView
    private lateinit var adapter: NearbyRequestAdapter
    private var isInitializing = true
    private lateinit var btnChatPatient: AppCompatButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var userId: Int = -1

    // Rating widgets
    private lateinit var rbMyRating: RatingBar
    private lateinit var tvMyRatingValue: TextView
    private lateinit var tvTotalReviews: TextView
    private lateinit var llLatestReview: LinearLayout
    private lateinit var tvLatestReviewText: TextView
    private lateinit var tvLatestSentiment: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_dashboard)

        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            val appPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            userId = appPrefs.getInt("user_id", -1)
        }

        sharedPreferences = getSharedPreferences("donor_prefs", Context.MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                updateLocationOnServer(location)
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

        val btnNotification = findViewById<ImageView>(R.id.btn_notification)
        switchAvailability = findViewById(R.id.switch_availability)
        tvAvailabilityStatus = findViewById(R.id.tv_availability_status)
        nearbyRequestsSection = findViewById(R.id.nearby_requests_section)
        rvNearbyRequests = findViewById(R.id.rv_nearby_requests)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        btnChatPatient = findViewById(R.id.btn_chat_patient)
        val btnHistory = findViewById<AppCompatButton>(R.id.btn_history)
        val btnViewMap = findViewById<LinearLayout>(R.id.btn_view_map_donor)
        val tvUserName = findViewById<TextView>(R.id.tv_user_name)

        val appPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userName = appPrefs.getString("user_name", "Donor")
        tvUserName.text = userName

        // Initialize rating widgets
        rbMyRating = findViewById(R.id.rb_my_rating)
        tvMyRatingValue = findViewById(R.id.tv_my_rating_value)
        tvTotalReviews = findViewById(R.id.tv_total_reviews)
        llLatestReview = findViewById(R.id.ll_latest_review)
        tvLatestReviewText = findViewById(R.id.tv_latest_review_text)
        tvLatestSentiment = findViewById(R.id.tv_latest_sentiment)

        rvNearbyRequests.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = NearbyRequestAdapter(emptyList())
        rvNearbyRequests.adapter = adapter

        btnViewMap?.setOnClickListener {
            startActivity(Intent(this, FreeMapActivity::class.java))
        }

        val isAvailable = sharedPreferences.getBoolean("donor_is_available", false)
        switchAvailability.isChecked = isAvailable
        updateUI(isAvailable)
        isInitializing = false

        switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            if (!isInitializing) {
                saveAvailability(isChecked)
                updateUI(isChecked)
            }
        }

        btnNotification.setOnClickListener {
            startActivity(Intent(this, DonorNotificationsActivity::class.java))
        }

        btnChatPatient.setOnClickListener {
            fetchLatestPatientAndChat()
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, DonationHistoryActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatInboxActivity::class.java))
                    false
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, DonorSettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationAndFetchRequests()
        if (userId != -1) loadDonorRatings()
    }

    private fun checkLocationAndFetchRequests() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                updateLocationOnServer(location)
            } else {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
                fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            }
        }
        loadDonorRequests()
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
            btnChatPatient.isEnabled = true
            btnChatPatient.alpha = 1.0f
        } else {
            tvAvailabilityStatus.text = "Not Available"
            tvAvailabilityStatus.setTextColor(ContextCompat.getColor(this, R.color.text_gray))
            btnChatPatient.isEnabled = false
            btnChatPatient.alpha = 0.5f
        }
        nearbyRequestsSection.visibility = if (isAvailable) View.VISIBLE else View.GONE
    }

    private fun saveAvailability(isAvailable: Boolean) {
        sharedPreferences.edit().putBoolean("donor_is_available", isAvailable).apply()
        
        if (userId != -1) {
            val params = mapOf("is_available" to isAvailable)
            ApiClient.instance.toggleAvailability(userId, params).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    android.util.Log.d("DonorDashboard", "Availability synced: $isAvailable")
                }
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@DonorDashboardActivity, "Sync failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadDonorRequests() {
        if (userId == -1) return
        
        val allRequests = mutableListOf<NearbyRequest>()
        
        // Fetch direct requests (personal to this donor)
        ApiClient.instance.getDonorRequests(userId).enqueue(object : Callback<List<NearbyRequest>> {
            override fun onResponse(call: Call<List<NearbyRequest>>, response: Response<List<NearbyRequest>>) {
                if (response.isSuccessful) {
                    allRequests.addAll(response.body() ?: emptyList())
                    fetchNearbyGlobalRequests(allRequests)
                } else {
                    fetchNearbyGlobalRequests(allRequests)
                }
            }
            override fun onFailure(call: Call<List<NearbyRequest>>, t: Throwable) {
                fetchNearbyGlobalRequests(allRequests)
            }
        })
    }

    private fun fetchNearbyGlobalRequests(existingRequests: MutableList<NearbyRequest>) {
        ApiClient.instance.getNearbyRequests(userId).enqueue(object : Callback<NearbyRequestsResponse> {
            override fun onResponse(call: Call<NearbyRequestsResponse>, response: Response<NearbyRequestsResponse>) {
                if (response.isSuccessful) {
                    val nearby = response.body()?.requests ?: emptyList()
                    // Filter duplicates by ID just in case
                    val merged = (existingRequests + nearby).distinctBy { it.id }
                    adapter.updateList(merged)
                    
                    findViewById<TextView>(R.id.tv_nearby_requests_title)?.text = 
                        if (merged.isEmpty()) "No Blood Requests Yet" 
                        else "Blood Requests for You (${merged.size})"
                } else {
                    adapter.updateList(existingRequests)
                }
            }
            override fun onFailure(call: Call<NearbyRequestsResponse>, t: Throwable) {
                adapter.updateList(existingRequests)
            }
        })
    }

    private fun updateLocationOnServer(loc: Location) {
        if (userId == -1) return
        val locationUpdate = LocationUpdate(userId, loc.latitude, loc.longitude)
        ApiClient.instance.updateLocation(locationUpdate).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                android.util.Log.d("DonorDashboard", "Location synced")
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                android.util.Log.e("DonorDashboard", "Location sync failed")
            }
        })
    }

    private fun loadDonorRatings() {
        ApiClient.instance.getDonorReviews(userId).enqueue(object : Callback<DonorReviewsResponse> {
            override fun onResponse(call: Call<DonorReviewsResponse>, response: Response<DonorReviewsResponse>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return
                    val avg = data.averageRating
                    val total = data.totalReviews
                    rbMyRating.rating = avg
                    tvMyRatingValue.text = if (total == 0) "No ratings yet" else String.format("%.1f / 5.0", avg)
                    tvTotalReviews.text = if (total == 1) "1 review" else "$total reviews"

                    // Find latest actual review text
                    val latestReview = data.reviews.firstOrNull { !it.reviewText.isNullOrEmpty() }
                    if (latestReview != null) {
                        llLatestReview.visibility = View.VISIBLE
                        tvLatestReviewText.text = "\"${latestReview.reviewText}\""
                        val sentimentScore = latestReview.sentimentScore
                        tvLatestSentiment.text = getSentimentLabel(sentimentScore)
                        tvLatestSentiment.setTextColor(
                            if (sentimentScore >= 0.5f) 0xFF166534.toInt() else 0xFF991B1B.toInt()
                        )
                    } else {
                        llLatestReview.visibility = View.GONE
                    }
                }
            }
            override fun onFailure(call: Call<DonorReviewsResponse>, t: Throwable) {
                android.util.Log.e("DonorDashboard", "Failed to load ratings: ${t.message}")
            }
        })
    }

    private fun getSentimentLabel(score: Float): String = when {
        score >= 0.8f -> "Very Positive"
        score >= 0.6f -> "Positive"
        score >= 0.4f -> "Neutral"
        score >= 0.2f -> "Negative"
        score > 0f    -> "Very Negative"
        else          -> "Neutral"
    }
}
