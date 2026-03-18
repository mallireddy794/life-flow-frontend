package com.simats.lifeflow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var requiredBloodGroup: String = "O+"
    private lateinit var tvMatchingCount: TextView
    private var isFirstLocationUpdate = true
    
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_map)

        requiredBloodGroup = intent.getStringExtra("BLOOD_TYPE") ?: "O+"
        
        tvMatchingCount = findViewById(R.id.tv_matching_count)
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val btnViewAll = findViewById<AppCompatButton>(R.id.btn_view_all)

        fused = LocationServices.getFusedLocationProviderClient(this)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnViewAll.setOnClickListener {
            val intent = Intent(this, AiMatchesActivity::class.java)
            startActivity(intent)
        }

        setupLocationCallback()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateMapWithLocation(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        mMap.setOnMarkerClickListener { marker ->
            val donorId = marker.tag as? Int
            val donorName = marker.title?.substringBefore(" (") ?: "Donor"

            if (donorId == null) return@setOnMarkerClickListener false

            showDonorOptions(donorId, donorName)
            true
        }

        checkLocationPermission()
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
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        fused.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Location permission required to find donors", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateMapWithLocation(loc: Location) {
        val patientLat = loc.latitude
        val patientLng = loc.longitude
        val patientPos = LatLng(patientLat, patientLng)

        if (isFirstLocationUpdate) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(patientPos, 14f))
            isFirstLocationUpdate = false
            loadNearbyDonors(requiredBloodGroup, patientLat, patientLng)
            loadNearbyHospitals(patientLat, patientLng)
        }
    }

    private fun loadNearbyDonors(bg: String, lat: Double, lng: Double) {
        ApiClient.instance.getNearbyDonors(bg, lat, lng, 5.0)
            .enqueue(object : Callback<List<NearbyDonor>> {
                override fun onResponse(call: Call<List<NearbyDonor>>, response: Response<List<NearbyDonor>>) {
                    if (response.isSuccessful) {
                        val donors = response.body() ?: emptyList()
                        tvMatchingCount.text = "${donors.size} Donors Nearby"

                        for (d in donors) {
                            val dLat = d.latitude ?: continue
                            val dLng = d.longitude ?: continue
                            val pos = LatLng(dLat, dLng)

                            val marker = mMap.addMarker(
                                MarkerOptions()
                                    .position(pos)
                                    .title("${d.name} (${d.blood_group})")
                                    .snippet("${String.format("%.2f", d.distance_km ?: 0.0)} km away")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            )
                            marker?.tag = d.donor_user_id
                        }
                    }
                }

                override fun onFailure(call: Call<List<NearbyDonor>>, t: Throwable) {}
            })
    }

    private fun loadNearbyHospitals(lat: Double, lng: Double) {
        val hospitalOffsets = listOf(
            Pair(0.005, 0.005),
            Pair(-0.008, 0.002),
            Pair(0.003, -0.007)
        )
        val hospitalNames = listOf("City General Hospital", "St. Mary Medical Center", "Regional Health Center")

        hospitalOffsets.forEachIndexed { index, offset ->
            val pos = LatLng(lat + offset.first, lng + offset.second)
            mMap.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(hospitalNames[index])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }
    }

    private fun showDonorOptions(donorId: Int, donorName: String) {
        AlertDialog.Builder(this)
            .setTitle(donorName)
            .setMessage("Choose an option for this donor")
            .setPositiveButton("Message") { _, _ ->
                openChatWithDonor(donorId, donorName)
            }
            .setNeutralButton("Send Request") { _, _ ->
                sendRequestToDonor(donorId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openChatWithDonor(donorId: Int, donorName: String) {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val patientId = sharedPrefs.getInt("user_id", -1)

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

    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(locationCallback)
    }
}
