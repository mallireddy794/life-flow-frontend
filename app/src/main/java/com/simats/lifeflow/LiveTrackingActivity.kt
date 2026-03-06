package com.simats.lifeflow

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor

class LiveTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var donorMarker: Marker? = null
    private var patientPos = LatLng(12.9716, 77.5946) // Example: Bangalore
    private var donorPos = LatLng(12.9800, 77.6000)
    
    private val handler = Handler(Looper.getMainLooper())
    private var distance = 1.8
    private var eta = 8
    
    private lateinit var tvDist: TextView
    private lateinit var tvEta: TextView

    private val movingRunnable = object : Runnable {
        // ... (rest of runnable code)
        override fun run() {
            if (distance > 0.05) {
                val lat = donorPos.latitude + (patientPos.latitude - donorPos.latitude) * 0.05
                val lng = donorPos.longitude + (patientPos.longitude - donorPos.longitude) * 0.05
                donorPos = LatLng(lat, lng)
                
                donorMarker?.position = donorPos
                
                distance -= 0.05
                if (eta > 1) {
                    val random = (0..2).random()
                    if (random == 0) eta -= 1
                }
                
                updateUi()
                handler.postDelayed(this, 3000)
            } else {
                tvDist.text = "Arrived"
                tvEta.text = "Now"
                findViewById<TextView>(R.id.tv_donor_status).text = "Donor has arrived!"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_tracking)

        tvDist = findViewById(R.id.tv_dist)
        tvEta = findViewById(R.id.tv_eta)
        val tvName = findViewById<TextView>(R.id.tv_donor_name)
        val tvStatus = findViewById<TextView>(R.id.tv_donor_status)
        val role = intent.getStringExtra("ROLE") ?: "PATIENT"

        if (role == "DONOR") {
            tvName.text = "City General Hospital"
            tvStatus.text = "Navigating to Hospital"
        } else {
            tvName.text = "Sarah Johnson"
            tvStatus.text = "On the way to you"
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Add Patient Marker
        mMap.addMarker(
            MarkerOptions()
                .position(patientPos)
                .title("You")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        // Add Donor Marker
        donorMarker = mMap.addMarker(
            MarkerOptions()
                .position(donorPos)
                .title("Donor: Sarah Johnson")
                .icon(bitmapDescriptorFromVector(R.drawable.ic_location))
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(donorPos, 14f))
        
        handler.postDelayed(movingRunnable, 2000)
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable?.intrinsicWidth ?: 0, vectorDrawable?.intrinsicHeight ?: 0, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun updateUi() {
        tvDist.text = String.format("%.2f km", distance)
        tvEta.text = "$eta mins"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(movingRunnable)
    }
}
