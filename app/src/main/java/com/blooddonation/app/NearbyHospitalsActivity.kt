package com.blooddonation.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.math.*

class NearbyHospitalsActivity : BaseActivity() {

    private lateinit var llHospitalList: LinearLayout
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvStatus: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospitals)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        llHospitalList = findViewById(R.id.ll_hospital_list)
        pbLoading = findViewById(R.id.pb_hospital_loading)
        tvStatus = findViewById(R.id.tv_hospital_status)

        ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        fetchHospitals()
    }

    @SuppressLint("MissingPermission")
    private fun fetchHospitals() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        pbLoading.visibility = View.VISIBLE
        tvStatus.text = "Getting your location..."

        val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    queryOverpass(loc.latitude, loc.longitude)
                } else {
                    // fallback: use stored user location from server
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val userId = prefs.getInt("user_id", -1)
                    if (userId != -1) {
                        ApiClient.instance.getUserLocation(userId).enqueue(object : retrofit2.Callback<Map<String, Any>> {
                            override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                                val body = response.body()
                                val lat = (body?.get("latitude") as? Double)
                                val lng = (body?.get("longitude") as? Double)
                                if (lat != null && lng != null) {
                                    queryOverpass(lat, lng)
                                } else {
                                    runOnUiThread { showError("Location not available. Please enable GPS.") }
                                }
                            }
                            override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                                runOnUiThread { showError("Could not get location.") }
                            }
                        })
                    } else {
                        showError("Please enable GPS or login again.")
                    }
                }
            }
            .addOnFailureListener { showError("GPS unavailable.") }
    }

    private fun queryOverpass(lat: Double, lng: Double) {
        runOnUiThread { tvStatus.text = "Finding hospitals near you..." }

        val radius = 5000  // 5km
        val query = """
            [out:json][timeout:25];
            (
              node["amenity"="hospital"](around:$radius,$lat,$lng);
              way["amenity"="hospital"](around:$radius,$lat,$lng);
              node["amenity"="clinic"](around:$radius,$lat,$lng);
              way["amenity"="clinic"](around:$radius,$lat,$lng);
            );
            out center 20;
        """.trimIndent()

        val body = query.toRequestBody("text/plain".toMediaType())
        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showError("Network error: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: ""
                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    try {
                        val root = JSONObject(json)
                        val elements = root.getJSONArray("elements")
                        if (elements.length() == 0) {
                            tvStatus.text = "No hospitals found within 5km"
                            return@runOnUiThread
                        }

                        // Build hospital list with distance
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
                        tvStatus.text = "Found ${hospitals.size} hospitals within 5km"
                        llHospitalList.removeAllViews()

                        val inflater = LayoutInflater.from(this@NearbyHospitalsActivity)
                        for (h in hospitals) {
                            val card = inflater.inflate(R.layout.item_hospital_card, llHospitalList, false)
                            card.findViewById<TextView>(R.id.tv_hosp_name).text = h.name
                            card.findViewById<TextView>(R.id.tv_hosp_dist).text =
                                "%.1f km away • %s".format(h.dist, h.type.replaceFirstChar { it.uppercase() })
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
                            llHospitalList.addView(card)
                        }
                    } catch (e: Exception) {
                        showError("Failed to parse hospitals: ${e.message}")
                    }
                }
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

    private fun showError(msg: String) {
        pbLoading.visibility = View.GONE
        tvStatus.text = msg
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
