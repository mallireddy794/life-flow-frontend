package com.simats.lifeflow

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DonorMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvMatchingCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_map)

        tvMatchingCount = findViewById(R.id.tv_matching_count)
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val btnViewList = findViewById<AppCompatButton>(R.id.btn_view_list)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnViewList.setOnClickListener {
            finish() // Go back to dashboard which has the list
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Example location: Bangalore
        val myPos = LatLng(12.9716, 77.5946)
        mMap.addMarker(
            MarkerOptions()
                .position(myPos)
                .title("You (Donor)")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 13f))

        // Simulated Nearby Blood Requests (Patients)
        loadDummyPatientRequests()
    }

    private fun loadDummyPatientRequests() {
        val requests = listOf(
            LatLng(12.9800, 77.6000),
            LatLng(12.9650, 77.5850),
            LatLng(12.9850, 77.5950)
        )
        
        val names = listOf("City Hospital (O+)", "Patient John (A-)", "General Clinic (B+)")

        for (i in requests.indices) {
            mMap.addMarker(
                MarkerOptions()
                    .position(requests[i])
                    .title(names[i])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
        
        tvMatchingCount.text = "${requests.size} Requests Nearby"
    }
}
