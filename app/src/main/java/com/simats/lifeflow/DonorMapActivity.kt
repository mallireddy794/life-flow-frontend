package com.simats.lifeflow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonorMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvMatchingCount: TextView
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isFirstLocationUpdate = true
    private var lastLoadedLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_map)

        tvMatchingCount = findViewById(R.id.tv_matching_count)
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val btnViewList = findViewById<AppCompatButton>(R.id.btn_view_list)

        MapsInitializer.initialize(applicationContext)
        fused = LocationServices.getFusedLocationProviderClient(this)

        ivBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnViewList?.setOnClickListener {
            finish()
        }

        setupLocationCallback()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        
        if (mapFragment == null) {
            Toast.makeText(this, "Map Error: Fragment not found", Toast.LENGTH_LONG).show()
            return
        }
        
        mapFragment.getMapAsync(this)
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                updateMapWithLocation(location)
                
                // Only update server if moved significantly (> 10m) to save bandwidth/prevent timeouts
                if (lastLoadedLocation == null || location.distanceTo(lastLoadedLocation!!) > 10) {
                    updateLocationOnServer(location)
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
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000)
                .setMinUpdateIntervalMillis(10000)
                .build()

            fused.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            Toast.makeText(this, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapWithLocation(loc: Location) {
        if (!::mMap.isInitialized) return
        
        val donorPos = LatLng(loc.latitude, loc.longitude)

        if (isFirstLocationUpdate) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(donorPos, 13f))
            isFirstLocationUpdate = false
            loadNearbyPatients(loc.latitude, loc.longitude)
        } else if (lastLoadedLocation == null || loc.distanceTo(lastLoadedLocation!!) > 500) {
            // Refresh requests if moved more than 500m
            loadNearbyPatients(loc.latitude, loc.longitude)
        }
    }

    private fun updateLocationOnServer(loc: Location) {
        val userId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        if (userId == -1) return

        val locationUpdate = LocationUpdate(userId, loc.latitude, loc.longitude)

        ApiClient.instance.updateLocation(locationUpdate).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                Log.d("DonorMap", "Location updated")
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("DonorMap", "Location update failed", t)
            }
        })
    }

    private fun loadNearbyPatients(lat: Double, lng: Double) {
        if (!::mMap.isInitialized) return

        ApiClient.instance.getNearbyPatients(lat, lng, 10.0)
            .enqueue(object : Callback<List<NearbyPatient>> {
                override fun onResponse(call: Call<List<NearbyPatient>>, response: Response<List<NearbyPatient>>) {
                    if (response.isSuccessful && !isFinishing) {
                        val patients = response.body() ?: emptyList()
                        tvMatchingCount.text = "${patients.size} Requests Nearby"

                        mMap.clear()
                        
                        for (p in patients) {
                            val pLat = p.latitude ?: continue
                            val pLng = p.longitude ?: continue
                            val pos = LatLng(pLat, pLng)
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(pos)
                                    .title("${p.name ?: "Patient"} (${p.blood_group ?: "?"})")
                                    .snippet("Hospital: ${p.hospital_name ?: "Home"}")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            )
                        }
                        
                        lastLoadedLocation = Location("").apply {
                            latitude = lat
                            longitude = lng
                        }
                    } else {
                        Log.e("DonorMap", "Error loading patients: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<NearbyPatient>>, t: Throwable) {
                    Log.e("DonorMap", "Network failure loading patients", t)
                    if (!isFinishing) {
                        Toast.makeText(this@DonorMapActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fused.isInitialized && ::locationCallback.isInitialized) {
            fused.removeLocationUpdates(locationCallback)
        }
    }
}
