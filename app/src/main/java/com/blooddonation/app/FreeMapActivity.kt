package com.blooddonation.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FreeMapActivity : BaseActivity() {

    private lateinit var map: MapView
    private lateinit var tvStatus: TextView
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isFirstLocation = true
    private var lastLocation: Location? = null
    
    private var userRole: String = "patient"
    private var userId: Int = -1

    private var requiredBloodType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSM Configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_free_map)

        // Init views
        map = findViewById(R.id.mapview)
        tvStatus = findViewById(R.id.tv_matching_count)
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val btnRefresh = findViewById<AppCompatButton>(R.id.btn_refresh)

        // Load data from Intent
        requiredBloodType = intent.getStringExtra("BLOOD_TYPE")
        val isEmergency = intent.getBooleanExtra("EMERGENCY_MODE", false)
        
        if (requiredBloodType != null) {
            tvStatus.text = "Searching for $requiredBloodType donors..."
        } else {
            tvStatus.text = "Searching for ALL donors..."
        }

        // Load preferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userRole = prefs.getString("user_role", "patient") ?: "patient"
        userId = prefs.getInt("user_id", -1)

        // Configure Map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(13.0)

        // Init location
        fused = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()

        ivBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener {
            lastLocation?.let { loadNearbyData(it.latitude, it.longitude) }
                ?: getLastKnownAndLoad()
        }

        checkPermissions()
        getLastKnownAndLoad()  // Load immediately without waiting for live GPS
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownAndLoad() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null && isFirstLocation) {
                isFirstLocation = false
                lastLocation = loc
                val point = GeoPoint(loc.latitude, loc.longitude)
                map.controller.animateTo(point)
                updateUserMarker(point)
                loadNearbyData(loc.latitude, loc.longitude)
                updateLocationOnServer(loc)
            }
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                lastLocation = location
                
                val point = GeoPoint(location.latitude, location.longitude)
                
                if (isFirstLocation) {
                    map.controller.animateTo(point)
                    isFirstLocation = false
                    loadNearbyData(location.latitude, location.longitude)
                }
                
                updateUserMarker(point)
                updateLocationOnServer(location)
            }
        }
    }

    private fun updateUserMarker(point: GeoPoint) {
        map.overlays.removeAll { it is Marker && it.title == "You" }
        val startMarker = Marker(map)
        startMarker.position = point
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.title = "You"
        
        val icon = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_location)?.mutate()
        icon?.setTint(android.graphics.Color.BLUE)
        startMarker.icon = icon
        
        map.overlays.add(startMarker)
        map.invalidate()
    }

    private fun loadNearbyData(lat: Double, lng: Double) {
        tvStatus.text = "Loading data..."
        if (userRole == "patient") {
            loadDonors(lat, lng)
        } else {
            loadPatients(lat, lng)
        }
    }

    private fun loadDonors(lat: Double, lng: Double) {
        val bloodGroup = requiredBloodType ?: "ALL"
        ApiClient.instance.getNearbyDonors(bloodGroup, lat, lng, 50000.0)
            .enqueue(object : Callback<List<NearbyDonor>> {
                override fun onResponse(call: Call<List<NearbyDonor>>, response: Response<List<NearbyDonor>>) {
                    if (response.isSuccessful) {
                        val donors = response.body() ?: emptyList()
                        tvStatus.text = "Found ${donors.size} Donors nearby"
                        showDonorMarkers(donors)
                    }
                }
                override fun onFailure(call: Call<List<NearbyDonor>>, t: Throwable) {
                    tvStatus.text = "Error loading donors"
                }
            })
    }

    private fun showDonorMarkers(donors: List<NearbyDonor>) {
        // Keep "You" marker as first
        val userMarker = map.overlays.find { it is Marker && it.title == "You" }
        map.overlays.clear()
        userMarker?.let { map.overlays.add(it) }

        for (d in donors) {
            val dLat = d.latitude ?: continue
            val dLng = d.longitude ?: continue
            val marker = Marker(map)
            marker.position = GeoPoint(dLat, dLng)
            marker.title = "${d.name ?: "Donor"} (${d.bloodGroup ?: "?"})"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            val icon = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_location)?.mutate()
            icon?.setTint(androidx.core.content.ContextCompat.getColor(this, R.color.bright_red))
            marker.icon = icon
            
            marker.setOnMarkerClickListener { m, _ ->
                showDonorOptions(d.donorUserId ?: -1, d.name ?: "Donor")
                true
            }
            
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    private fun showDonorOptions(donorId: Int, donorName: String) {
        if (donorId == -1) return
        AlertDialog.Builder(this)
            .setTitle(donorName)
            .setMessage("Choose an option for this donor")
            .setPositiveButton("Message") { _, _ -> openChatWithDonor(donorId, donorName) }
            .setNeutralButton("Send Request") { _, _ -> sendRequestToDonor(donorId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openChatWithDonor(donorId: Int, donorName: String) {
        val patientId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        val intent = Intent(this, PatientChatActivity::class.java)
        intent.putExtra("SENDER_ID", patientId)
        intent.putExtra("RECEIVER_ID", donorId)
        intent.putExtra("RECEIVER_NAME", donorName)
        intent.putExtra("CHAT_STATUS", "CONNECTED")
        startActivity(intent)
    }

    private fun sendRequestToDonor(donorId: Int) {
        Toast.makeText(this, "Request notification sent to donor", Toast.LENGTH_SHORT).show()
    }

    private fun loadPatients(lat: Double, lng: Double) {
        ApiClient.instance.getNearbyPatients(lat, lng, 10.0)
            .enqueue(object : Callback<List<NearbyPatient>> {
                override fun onResponse(call: Call<List<NearbyPatient>>, response: Response<List<NearbyPatient>>) {
                    if (response.isSuccessful) {
                        val patients = response.body() ?: emptyList()
                        tvStatus.text = "Found ${patients.size} Requests nearby"
                        showMarkers(patients.map { Triple(GeoPoint(it.latitude ?: 0.0, it.longitude ?: 0.0), it.name ?: "Patient", it.blood_group ?: "") })
                    }
                }
                override fun onFailure(call: Call<List<NearbyPatient>>, t: Throwable) {
                    tvStatus.text = "Error loading patients"
                }
            })
    }

    private fun showMarkers(data: List<Triple<GeoPoint, String, String>>) {
        // Keep "You" marker as first
        val userMarker = map.overlays.find { it is Marker && it.title == "You" }
        map.overlays.clear()
        userMarker?.let { map.overlays.add(it) }

        for (item in data) {
            val marker = Marker(map)
            marker.position = item.first
            marker.title = "${item.second} (${item.third})"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            val icon = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_location)?.mutate()
            icon?.setTint(androidx.core.content.ContextCompat.getColor(this, R.color.bright_red))
            marker.icon = icon
            
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    private fun updateLocationOnServer(loc: Location) {
        if (userId == -1) return
        val locationUpdate = LocationUpdate(userId, loc.latitude, loc.longitude)
        ApiClient.instance.updateLocation(locationUpdate).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {}
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {}
        })
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000).build()
        fused.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(locationCallback)
    }
}
