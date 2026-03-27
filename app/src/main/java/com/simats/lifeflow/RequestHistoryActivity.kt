package com.simats.lifeflow

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestHistoryActivity : BaseActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_history)

        var userId = intent.getIntExtra("user_id", -1)
        
        if (userId == -1) {
            val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            userId = sharedPrefs.getInt("user_id", -1)
        }

        container = findViewById(R.id.history_container)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (userId != -1) {
            fetchRequestHistory(userId)
        } else {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRequestHistory(userId: Int) {
        ApiClient.instance.viewRequests(userId).enqueue(object : Callback<List<PatientRequest>> {
            override fun onResponse(call: Call<List<PatientRequest>>, response: Response<List<PatientRequest>>) {
                if (response.isSuccessful) {
                    val history = response.body() ?: emptyList()
                    displayHistory(history)
                } else {
                    displayHistory(emptyList())
                }
            }

            override fun onFailure(call: Call<List<PatientRequest>>, t: Throwable) {
                Toast.makeText(this@RequestHistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                displayHistory(emptyList())
            }
        })
    }

    private fun displayHistory(history: List<PatientRequest>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (history.isEmpty()) {
            val emptyView = inflater.inflate(R.layout.view_request_history_item, container, false)
            emptyView.findViewById<TextView>(R.id.tv_date).text = "No history found"
            emptyView.findViewById<TextView>(R.id.tv_blood_type).text = "Your blood requests will appear here"
            emptyView.findViewById<TextView>(R.id.tv_units).text = ""
            emptyView.findViewById<TextView>(R.id.tv_status_badge).visibility = View.GONE
            container.addView(emptyView)
            return
        }

        for (request in history) {
            val itemView = inflater.inflate(R.layout.view_request_history_item, container, false)
            
            val tvDate = itemView.findViewById<TextView>(R.id.tv_date)
            val tvBloodType = itemView.findViewById<TextView>(R.id.tv_blood_type)
            val tvUnits = itemView.findViewById<TextView>(R.id.tv_units)
            val tvStatusBadge = itemView.findViewById<TextView>(R.id.tv_status_badge)
            val ivIcon = itemView.findViewById<ImageView>(R.id.iv_request_icon)

            // Since our backend doesn't store date for blood_requests yet (except created_at in some queries), 
            // we use a placeholder or the request ID
            tvDate.text = "Request ID: #${request.request_id}"
            tvBloodType.text = "Blood Type: ${request.blood_group}"
            
            val unitStr = if (request.units_required <= 1) "unit" else "units"
            tvUnits.text = "${request.units_required} $unitStr (${request.city})"
            
            val status = request.status.lowercase()
            tvStatusBadge.text = request.status.uppercase()
            
            when {
                status.contains("pending") -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending)
                    tvStatusBadge.setTextColor(Color.parseColor("#854D0E"))
                    ivIcon.setImageResource(R.drawable.ic_clock)
                }
                status.contains("approve") || status.contains("complete") -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_completed)
                    tvStatusBadge.setTextColor(Color.parseColor("#166534"))
                    ivIcon.setImageResource(R.drawable.ic_check_circle)
                }
                status.contains("reject") -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending) // Fallback or new reject bg
                    tvStatusBadge.setTextColor(Color.RED)
                    ivIcon.setImageResource(R.drawable.ic_close)
                }
                else -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending)
                    tvStatusBadge.setTextColor(Color.BLUE)
                }
            }

            itemView.setOnClickListener {
                val intent = Intent(this, RequestTrackingActivity::class.java)
                intent.putExtra("request_id", request.request_id)
                startActivity(intent)
            }

            container.addView(itemView)
        }
    }
}
