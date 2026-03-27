package com.simats.lifeflow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.math.*

class PatientDashboardActivity : BaseActivity() {

    private lateinit var tvNearbyDonors: TextView
    private lateinit var tvNearbyHospitals: TextView
    private lateinit var tvMatchCount: TextView
    private lateinit var tvSearching: TextView
    private lateinit var llDashboardDonors: LinearLayout
    private lateinit var llDashboardHospitals: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)

        tvNearbyDonors = findViewById(R.id.tv_nearby_donors_count)
        tvNearbyHospitals = findViewById(R.id.tv_nearby_hospitals_count)
        tvMatchCount = findViewById(R.id.tv_match_count)
        tvSearching = findViewById(R.id.tv_searching)
        llDashboardDonors = findViewById(R.id.ll_dashboard_donors)
        llDashboardHospitals = findViewById(R.id.ll_dashboard_hospitals)

        val ivNotifications = findViewById<ImageView>(R.id.iv_notifications)
        val btnViewMap = findViewById<AppCompatButton>(R.id.btn_view_map)
        val btnTrackRequest = findViewById<AppCompatButton>(R.id.btn_track_request)
        val btnViewMatches = findViewById<AppCompatButton>(R.id.btn_view_matches)
        val btnEmergency = findViewById<AppCompatButton>(R.id.btn_emergency)
        val btnChat = findViewById<AppCompatButton>(R.id.btn_chat)
        val btnNearbyDonors = findViewById<LinearLayout>(R.id.btn_nearby_donors)
        val btnNearbyHospitals = findViewById<LinearLayout>(R.id.btn_nearby_hospitals)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        btnNearbyDonors.setOnClickListener {
            startActivity(Intent(this, FreeMapActivity::class.java))
        }

        btnNearbyHospitals.setOnClickListener {
            startActivity(Intent(this, NearbyHospitalsActivity::class.java))
        }

        ivNotifications.setOnClickListener {
            startActivity(Intent(this, PatientNotificationsActivity::class.java))
        }

        btnViewMap.setOnClickListener {
            startActivity(Intent(this, FreeMapActivity::class.java))
        }

        btnTrackRequest.setOnClickListener {
            startActivity(Intent(this, RequestTrackingActivity::class.java))
        }

        btnViewMatches.setOnClickListener {
            startActivity(Intent(this, AiMatchesActivity::class.java))
        }

        btnEmergency.setOnClickListener {
            startActivity(Intent(this, EmergencyRequestActivity::class.java))
        }

        btnChat.setOnClickListener {
            if (userId != -1) fetchLatestDonorAndChat(userId)
            else Toast.makeText(this, "User session expired", Toast.LENGTH_SHORT).show()
        }

        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_chat -> { startActivity(Intent(this, ChatInboxActivity::class.java)); false }
                R.id.nav_settings -> { startActivity(Intent(this, PatientSettingsActivity::class.java)); false }
                else -> false
            }
        }

        // Fetch live stats using GPS
        fetchLiveStats(userId)
    }

    @SuppressLint("MissingPermission")
    private fun fetchLiveStats(userId: Int) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            // Fallback: use stored location
            loadStatsFromServerLocation(userId)
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                updateLocationOnServer(loc.latitude, loc.longitude, userId)
                loadLiveDonorCount(loc.latitude, loc.longitude)
                loadLiveHospitalCount(loc.latitude, loc.longitude)
            } else {
                loadStatsFromServerLocation(userId)
            }
        }.addOnFailureListener {
            loadStatsFromServerLocation(userId)
        }
    }

    private fun loadStatsFromServerLocation(userId: Int) {
        if (userId == -1) return
        ApiClient.instance.getUserLocation(userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                val body = response.body() ?: return
                val lat = (body["latitude"] as? Double) ?: return
                val lng = (body["longitude"] as? Double) ?: return
                loadLiveDonorCount(lat, lng)
                loadLiveHospitalCount(lat, lng)
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
        })
    }

    private fun loadLiveDonorCount(lat: Double, lng: Double) {
        tvSearching.text = "Searching for nearby donors..."
        ApiClient.instance.getNearbyDonors("ALL", lat, lng, 50000.0)
            .enqueue(object : Callback<List<NearbyDonor>> {
                override fun onResponse(call: Call<List<NearbyDonor>>, response: Response<List<NearbyDonor>>) {
                    if (response.isSuccessful) {
                        val count = response.body()?.size ?: 0
                        tvNearbyDonors.text = count.toString()
                        tvMatchCount.text = count.toString()
                        tvSearching.text = if (count > 0) "Found $count donors near you" else "No donors found nearby"
                        if (count > 0) {
                            findViewById<TextView>(R.id.tv_matched_status)?.text = "Donors Matched"
                            
                            llDashboardDonors.removeAllViews()
                            val inflater = android.view.LayoutInflater.from(this@PatientDashboardActivity)
                            val topDonors = response.body()?.sortedBy { it.distanceKm ?: 0.0 }?.take(3) ?: emptyList()
                            for (donor in topDonors) {
                                val view = inflater.inflate(R.layout.item_donor_card_small, llDashboardDonors, false)
                                val bg = donor.bloodGroup
                                view.findViewById<TextView>(R.id.tv_donor_bg).text = if (!bg.isNullOrEmpty()) bg.substring(0, min(2, bg.length)) else "O+"
                                view.findViewById<TextView>(R.id.tv_donor_name).text = donor.name ?: "Unknown Donor"
                                view.findViewById<TextView>(R.id.tv_donor_dist).text = String.format("%.1f km away", donor.distanceKm ?: 0.0)
                                llDashboardDonors.addView(view)
                            }
                        } else {
                            llDashboardDonors.removeAllViews()
                        }
                    }
                }
                override fun onFailure(call: Call<List<NearbyDonor>>, t: Throwable) {
                    tvSearching.text = "Could not load donor count"
                }
            })
    }

    private fun loadLiveHospitalCount(lat: Double, lng: Double) {
        val query = """
            [out:json][timeout:15];
            (
              node["amenity"="hospital"](around:5000,$lat,$lng);
              way["amenity"="hospital"](around:5000,$lat,$lng);
              node["amenity"="clinic"](around:5000,$lat,$lng);
            );
            out center 20;
        """.trimIndent()

        val body = query.toRequestBody("text/plain".toMediaType())
        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter")
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {}
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val json = response.body?.string() ?: return
                try {
                    val root = JSONObject(json)
                    val elements = root.getJSONArray("elements")
                    val count = elements.length()
                    
                    data class Hospital(val name: String, val dist: Double, val type: String)
                    val hospitals = mutableListOf<Hospital>()

                    for (i in 0 until elements.length()) {
                        val el = elements.getJSONObject(i)
                        val tags = el.optJSONObject("tags") ?: continue
                        val name = tags.optString("name", "").takeIf { it.isNotEmpty() } ?: continue
                        val amenity = tags.optString("amenity", "hospital")

                        val eLat = if (el.has("lat")) el.getDouble("lat")
                                   else el.optJSONObject("center")?.getDouble("lat") ?: continue
                        val eLng = if (el.has("lon")) el.getDouble("lon")
                                   else el.optJSONObject("center")?.getDouble("lon") ?: continue

                        val dist = haversine(lat, lng, eLat, eLng)
                        hospitals.add(Hospital(name, dist, amenity))
                    }
                    hospitals.sortBy { it.dist }

                    runOnUiThread { 
                        tvNearbyHospitals.text = count.toString() 
                        llDashboardHospitals.removeAllViews()
                        val inflater = android.view.LayoutInflater.from(this@PatientDashboardActivity)
                        val topHospitals = hospitals.take(3)
                        for (h in topHospitals) {
                            val card = inflater.inflate(R.layout.item_hospital_card, llDashboardHospitals, false)
                            card.findViewById<TextView>(R.id.tv_hosp_name).text = h.name
                            card.findViewById<TextView>(R.id.tv_hosp_dist).text = String.format("%.1f km away • %s", h.dist, h.type.replaceFirstChar { it.uppercase() })
                            val badge = card.findViewById<TextView>(R.id.tv_hosp_badge)
                            badge.text = if (h.dist < 2.0) "Nearby" else if (h.dist < 4.0) "Moderate" else "Far"
                            badge.setBackgroundResource(
                                if (h.dist < 2.0) R.drawable.bg_availability_high
                                else if (h.dist < 4.0) R.drawable.bg_availability_medium
                                else R.drawable.bg_availability_low
                            )
                            badge.setTextColor(
                                if (h.dist < 2.0) 0xFF166534.toInt()
                                else if (h.dist < 4.0) 0xFF854D0E.toInt()
                                else 0xFF991B1B.toInt()
                            )
                            llDashboardHospitals.addView(card)
                        }
                    }
                } catch (e: Exception) {
                    // Silent fail - keep default
                }
            }
        })
    }

    private fun fetchLatestDonorAndChat(userId: Int) {
        ApiClient.instance.getDonors().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val donors = response.body() ?: emptyList()
                    if (donors.isNotEmpty()) {
                        val donor = donors.random()
                        val intent = Intent(this@PatientDashboardActivity, PatientChatActivity::class.java)
                        intent.putExtra("SENDER_ID", userId)
                        intent.putExtra("RECEIVER_ID", donor.id)
                        intent.putExtra("RECEIVER_NAME", donor.name)
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

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat/2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon/2).pow(2)
        return R * 2 * asin(sqrt(a))
    }

    private fun updateLocationOnServer(lat: Double, lng: Double, userId: Int) {
        if (userId == -1) return
        val locationUpdate = LocationUpdate(userId, lat, lng)
        ApiClient.instance.updateLocation(locationUpdate).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                android.util.Log.d("PatientDashboard", "Location synchronized with server")
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {}
        })
    }
}
