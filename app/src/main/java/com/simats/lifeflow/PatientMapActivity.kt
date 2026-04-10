package com.simats.lifeflow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientMapActivity : BaseActivity() {

    private lateinit var map: MapView
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Passed from EmergencyRequestActivity
    private var requiredBloodGroup: String = "O+"
    private var isEmergencyMode: Boolean = false
    private var fromEmergency: Boolean = false
    private var units: Int = 1

    private lateinit var tvMatchingCount: TextView
    private lateinit var tvEmergencyBloodType: TextView
    private lateinit var pbSearching: ProgressBar
    private lateinit var llEmergencyBanner: LinearLayout
    private lateinit var rvNearbyDonors: RecyclerView
    private lateinit var llDonorList: LinearLayout
    private lateinit var tvNoDonors: TextView

    private val nearbyDonorList = mutableListOf<NearbyDonor>()
    private lateinit var donorListAdapter: NearbyDonorListAdapter

    private var isFirstLocationUpdate = true
    private var lastLoadedLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSM Configuration (Free Maps)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_patient_map)

        // Read extras from EmergencyRequestActivity
        requiredBloodGroup = intent.getStringExtra("BLOOD_TYPE") ?: "O+"
        isEmergencyMode = intent.getBooleanExtra("EMERGENCY_MODE", false)
        fromEmergency = intent.getBooleanExtra("FROM_EMERGENCY", false)
        units = intent.getIntExtra("UNITS_NEEDED", intent.getIntExtra("UNITS", 1))

        tvMatchingCount = findViewById(R.id.tv_matching_count)
        llEmergencyBanner = findViewById(R.id.ll_emergency_banner)
        tvEmergencyBloodType = findViewById(R.id.tv_emergency_blood_type)
        pbSearching = findViewById(R.id.pb_searching)
        rvNearbyDonors = findViewById(R.id.rv_nearby_donors)
        llDonorList = findViewById(R.id.ll_donor_list)
        tvNoDonors = findViewById(R.id.tv_no_donors)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val btnViewAll = findViewById<AppCompatButton>(R.id.btn_view_all)

        // Initialize OsmDroid Map
        map = findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.5)

        fused = LocationServices.getFusedLocationProviderClient(this)

        ivBack?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnViewAll?.setOnClickListener {
            val intent = Intent(this, AiMatchesActivity::class.java)
            startActivity(intent)
        }

        // Show emergency banner if coming from emergency request
        if (fromEmergency || isEmergencyMode) {
            llEmergencyBanner.visibility = View.VISIBLE
            tvEmergencyBloodType.text = "Blood Type: $requiredBloodGroup · $units unit(s) required"
        }

        // Setup donor list RecyclerView
        donorListAdapter = NearbyDonorListAdapter(nearbyDonorList) { donor: NearbyDonor ->
            donor.donorUserId?.let { id ->
                showDonorOptions(id, donor.name ?: "Donor", donor.bloodGroup ?: requiredBloodGroup)
            }
        }
        rvNearbyDonors.layoutManager = LinearLayoutManager(this).apply {
            isItemPrefetchEnabled = false
        }
        rvNearbyDonors.adapter = donorListAdapter

        setupLocationCallback()
        checkLocationPermission()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                updateMapWithLocation(location)
                if (lastLoadedLocation == null || location.distanceTo(lastLoadedLocation!!) > 10) {
                    updateLocationOnServer(location)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fused.isInitialized && ::locationCallback.isInitialized) {
            fused.removeLocationUpdates(locationCallback)
        }
        if (::map.isInitialized) {
            map.onDetach()
        }
    }

    private fun updateLocationOnServer(loc: Location) {
        val userId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        if (userId == -1) return
        val locationUpdate = LocationUpdate(userId, loc.latitude, loc.longitude)
        ApiClient.instance.updateLocation(locationUpdate).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                Log.d("PatientMap", "Location updated on server")
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("PatientMap", "Failed to update location", t)
            }
        })
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000)
                .setMinUpdateIntervalMillis(10000)
                .build()
            fused.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            if (!isFinishing) Toast.makeText(this, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    private fun updateMapWithLocation(loc: Location) {
        val patientPos = GeoPoint(loc.latitude, loc.longitude)

        if (isFirstLocationUpdate) {
            map.controller.animateTo(patientPos)
            isFirstLocationUpdate = false
            loadNearbyDonors(requiredBloodGroup, loc.latitude, loc.longitude)
        } else if (lastLoadedLocation == null || loc.distanceTo(lastLoadedLocation!!) > 500) {
            if (!isFinishing) loadNearbyDonors(requiredBloodGroup, loc.latitude, loc.longitude)
        }
        
        updateUserMarker(patientPos)
    }

    private fun updateUserMarker(point: GeoPoint) {
        // Remove existing "You" marker
        map.overlays.removeAll { it is Marker && it.title == "You" }
        
        val startMarker = Marker(map)
        startMarker.position = point
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.title = "You"
        
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_location)?.mutate()
        icon?.setTint(android.graphics.Color.BLUE)
        startMarker.icon = icon
        
        map.overlays.add(startMarker)
        map.invalidate()
    }

    private fun loadNearbyDonors(bg: String, lat: Double, lng: Double) {
        // Show searching indicator
        pbSearching.visibility = View.VISIBLE
        tvMatchingCount.text = "Searching donors..."
        llDonorList.visibility = View.GONE

        Log.d("PatientMap", "Loading donors for $bg at $lat, $lng")

        // Use "ALL" to get all blood groups if needed
        val bloodGroupQuery = bg.uppercase()
        
        if (isEmergencyMode || fromEmergency) {
            val patientId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
            val req = EmergencySearchRequest(
                patientId = patientId,
                bloodGroup = bloodGroupQuery,
                lat = lat,
                lng = lng,
                unitsRequired = units,
                radiusKm = 10.0
            )

            ApiClient.instance.emergencyDonors(req).enqueue(object : Callback<EmergencySearchResponse> {
                override fun onResponse(call: Call<EmergencySearchResponse>, response: Response<EmergencySearchResponse>) {
                    pbSearching.visibility = View.GONE
                    if (response.isSuccessful && !isFinishing) {
                        val body = response.body()
                        val rankedDonors = body?.nearbyDonors ?: emptyList()
                        if (!isFinishing) tvMatchingCount.text = "${rankedDonors.size} AI-Matched Donor(s)"
                        
                        val mappedDonors = rankedDonors.map { r ->
                            NearbyDonor(
                                donorUserId = r.donorId,
                                name = r.name,
                                phone = r.phone,
                                bloodGroup = r.bloodGroup,
                                city = r.city,
                                latitude = r.latitude,
                                longitude = r.longitude,
                                distanceKm = r.distanceKm,
                                pastAcceptanceRate = r.pastAcceptanceRate,
                                responseTimeAvg = r.responseTimeAvg
                            )
                        }
                        updateMapWithDonors(mappedDonors, lat, lng)
                    } else {
                        Log.e("PatientMap", "Error loading AI donors: ${response.code()}")
                        if (!isFinishing) Toast.makeText(this@PatientMapActivity, "AI Match failed (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<EmergencySearchResponse>, t: Throwable) {
                    pbSearching.visibility = View.GONE
                    Log.e("PatientMap", "Network failure", t)
                    if (!isFinishing) Toast.makeText(this@PatientMapActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            ApiClient.instance.getNearbyDonors(bloodGroupQuery, lat, lng, 10.0)
                .enqueue(object : Callback<List<NearbyDonor>> {
                    override fun onResponse(call: Call<List<NearbyDonor>>, response: Response<List<NearbyDonor>>) {
                        pbSearching.visibility = View.GONE
                        if (response.isSuccessful && !isFinishing) {
                            val donors = response.body() ?: emptyList()
                            tvMatchingCount.text = "${donors.size} Donor${if (donors.size != 1) "s" else ""} Nearby"
                            updateMapWithDonors(donors, lat, lng)
                        } else if (!isFinishing) {
                            Log.e("PatientMap", "Error loading donors: ${response.code()}")
                            Toast.makeText(this@PatientMapActivity, "Could not load donors (${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<NearbyDonor>>, t: Throwable) {
                        pbSearching.visibility = View.GONE
                        Log.e("PatientMap", "Network failure", t)
                        if (!isFinishing) Toast.makeText(this@PatientMapActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun updateMapWithDonors(donors: List<NearbyDonor>, lat: Double, lng: Double) {
        // Clear only donor/hospital markers, keep "You"
        map.overlays.removeAll { it is Marker && it.title != "You" }
        
        loadNearbyHospitals(lat, lng)

        nearbyDonorList.clear()
        nearbyDonorList.addAll(donors)
        donorListAdapter.notifyDataSetChanged()

        // Show / hide donor list panel
        llDonorList.visibility = View.VISIBLE
        tvNoDonors.visibility = if (donors.isEmpty()) View.VISIBLE else View.GONE
        rvNearbyDonors.visibility = if (donors.isNotEmpty()) View.VISIBLE else View.GONE

        for (d in donors) {
            val dLat = d.latitude ?: continue
            val dLng = d.longitude ?: continue
            
            val marker = Marker(map)
            marker.position = GeoPoint(dLat, dLng)
            marker.title = "${d.name ?: "Donor"} (${d.bloodGroup ?: "?"})"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            val icon = ContextCompat.getDrawable(this@PatientMapActivity, R.drawable.ic_location)?.mutate()
            icon?.setTint(ContextCompat.getColor(this@PatientMapActivity, R.color.bright_red))
            marker.icon = icon
            
            marker.setOnMarkerClickListener { m, _ ->
                showDonorOptions(d.donorUserId ?: -1, d.name ?: "Donor", d.bloodGroup ?: requiredBloodGroup)
                true
            }
            
            map.overlays.add(marker)
        }
        map.invalidate()

        lastLoadedLocation = Location("").apply {
            latitude = lat
            longitude = lng
        }

        if (donors.isEmpty()) {
            Toast.makeText(this@PatientMapActivity, "No donors found nearby. Expanding search...", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadNearbyHospitals(lat: Double, lng: Double) {
        val hospitalOffsets = listOf(Pair(0.005, 0.005), Pair(-0.008, 0.002), Pair(0.003, -0.007))
        val hospitalNames = listOf("City General Hospital", "St. Mary Medical Center", "Regional Health Center")
        hospitalOffsets.forEachIndexed { index, offset ->
            val marker = Marker(map)
            marker.position = GeoPoint(lat + offset.first, lng + offset.second)
            marker.title = hospitalNames[index]
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            val icon = ContextCompat.getDrawable(this, R.drawable.ic_location)?.mutate()
            icon?.setTint(ContextCompat.getColor(this, R.color.success_green))
            marker.icon = icon
            
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    /** Shows a dialog: Chat (to fix appointment) OR Send Request OR Rate Donor */
    private fun showDonorOptions(donorId: Int, donorName: String, bloodGroup: String) {
        val options = arrayOf("💬 Chat & Fix Appointment", "📩 Send Request", "⭐ Rate Donor")
        AlertDialog.Builder(this)
            .setTitle("🩸 $donorName")
            .setMessage("Blood Type: $bloodGroup")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openChatWithDonor(donorId, donorName, bloodGroup)
                    1 -> sendRequestToDonor(donorId, donorName)
                    2 -> openRateDonor(donorId, donorName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openRateDonor(donorId: Int, donorName: String) {
        val intent = Intent(this, RateDonorActivity::class.java)
        intent.putExtra("DONOR_ID", donorId)
        intent.putExtra("DONOR_NAME", donorName)
        startActivity(intent)
    }


    private fun openChatWithDonor(donorId: Int, donorName: String, bloodGroup: String) {
        val patientId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        val intent = Intent(this, PatientChatActivity::class.java)
        intent.putExtra("SENDER_ID", patientId)
        intent.putExtra("RECEIVER_ID", donorId)
        intent.putExtra("RECEIVER_NAME", donorName)
        intent.putExtra("BLOOD_GROUP", bloodGroup)
        intent.putExtra("CHAT_STATUS", "CONNECTED")
        // Pre-fill appointment suggestion if coming from emergency
        if (fromEmergency || isEmergencyMode) {
            intent.putExtra("IS_EMERGENCY", true)
            intent.putExtra("UNITS", units)
        }
        startActivity(intent)
    }

    private fun sendRequestToDonor(donorId: Int, donorName: String) {
        val patientId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        if (patientId == -1) return

        val requestBody = SendRequestToDonor(
            patientId = patientId,
            donorId = donorId,
            bloodGroup = requiredBloodGroup,
            unitsNeeded = units,
            urgency = if (isEmergencyMode) "EMERGENCY" else "HIGH",
            message = "Emergency need blood - Direct Request from Map"
        )

        Log.d("PatientMap", "Sending request: patientId=$patientId, donorId=$donorId, bloodGroup=$requiredBloodGroup, units=$units")

        ApiClient.instance.sendRequestToDonor(requestBody).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@PatientMapActivity, "✅ Request sent to $donorName!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorResp = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("PatientMap", "Failed: ${response.code()} - $errorResp")
                    Toast.makeText(this@PatientMapActivity, "Failed: $errorResp", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("PatientMap", "Network failure", t)
                Toast.makeText(this@PatientMapActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
