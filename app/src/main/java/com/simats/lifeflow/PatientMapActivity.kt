package com.simats.lifeflow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

class PatientMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fused: FusedLocationProviderClient
    private var requiredBloodGroup: String = "O+"
    private lateinit var tvMatchingCount: TextView
    
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

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Setup Marker Click Listener
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
        getPatientLocationAndLoadDonors()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPatientLocationAndLoadDonors()
        } else {
            Toast.makeText(this, "Location permission required to find donors", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPatientLocationAndLoadDonors() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fused.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc == null) {
                Toast.makeText(this, "Could not determine your location", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val patientLat = loc.latitude
            val patientLng = loc.longitude
            val patientPos = LatLng(patientLat, patientLng)

            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(patientPos)
                    .title("You (Patient)")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(patientPos, 13f))

            loadNearbyDonors(requiredBloodGroup, patientLat, patientLng)
        }
    }

    private fun loadNearbyDonors(bg: String, lat: Double, lng: Double) {
        ApiClient.instance.getNearbyDonors(bg, lat, lng, 5.0)
            .enqueue(object : Callback<List<NearbyDonor>> {
                override fun onResponse(call: Call<List<NearbyDonor>>, response: Response<List<NearbyDonor>>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@PatientMapActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val donors = response.body() ?: emptyList()
                    tvMatchingCount.text = "${donors.size} Donors Nearby"

                    if (donors.isEmpty()) {
                        Toast.makeText(this@PatientMapActivity, "No donors found within 5km", Toast.LENGTH_LONG).show()
                        return
                    }

                    for (d in donors) {
                        val dLat = d.latitude ?: continue
                        val dLng = d.longitude ?: continue
                        val pos = LatLng(dLat, dLng)

                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title("${d.name} (${d.blood_group})")
                                .snippet("${String.format("%.2f", d.distance_km ?: 0.0)} km away")
                        )
                        marker?.tag = d.donor_user_id
                    }
                }

                override fun onFailure(call: Call<List<NearbyDonor>>, t: Throwable) {
                    Toast.makeText(this@PatientMapActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val patientId = prefs.getInt("user_id", 0)

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
}
