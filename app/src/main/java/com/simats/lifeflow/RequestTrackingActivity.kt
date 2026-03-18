package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton

class RequestTrackingActivity : BaseActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var distance = 2.5
    private var eta = 15
    private lateinit var tvDistance: TextView
    private lateinit var tvEta: TextView

    private val trackingRunnable = object : Runnable {
        override fun run() {
            if (distance > 0.1) {
                distance -= 0.1
                if (eta > 1) eta -= 1
                
                tvDistance.text = String.format("%.1f km", distance)
                tvEta.text = "$eta min"
                
                handler.postDelayed(this, 5000) // Update every 5 seconds
            } else {
                tvDistance.text = "Arrived"
                tvEta.text = "0 min"
                findViewById<TextView>(R.id.tv_live_status).text = "Donor has arrived"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_tracking)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val btnContactDonor = findViewById<AppCompatButton>(R.id.btn_contact_donor)
        val btnCancelRequest = findViewById<AppCompatButton>(R.id.btn_cancel_request)
        val btnViewLiveMap = findViewById<AppCompatButton>(R.id.btn_view_live_map)
        
        tvDistance = findViewById(R.id.tv_distance_value)
        tvEta = findViewById(R.id.tv_eta_value)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnViewLiveMap.setOnClickListener {
            val intent = Intent(this, LiveTrackingActivity::class.java)
            intent.putExtra("ROLE", "PATIENT")
            startActivity(intent)
        }

        btnContactDonor.setOnClickListener {
            // Get current patient's ID
            val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val senderId = sharedPrefs.getInt("user_id", -1)

            // Starting chat with donor (using dummy ID 201 for this simulation)
            val intent = Intent(this, PatientChatActivity::class.java)
            intent.putExtra("SENDER_ID", senderId)
            intent.putExtra("RECEIVER_ID", 201) // Dummy Donor ID
            intent.putExtra("RECEIVER_NAME", "Sarah Johnson")
            intent.putExtra("CHAT_STATUS", "CONNECTED")
            startActivity(intent)
        }

        btnCancelRequest.setOnClickListener {
            val intent = Intent(this, CancelBloodRequestActivity::class.java)
            startActivity(intent)
        }

        // Start Live Tracking Simulation
        handler.postDelayed(trackingRunnable, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(trackingRunnable)
    }
}
