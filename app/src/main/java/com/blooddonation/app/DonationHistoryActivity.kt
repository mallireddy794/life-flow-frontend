package com.blooddonation.app

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonationHistoryActivity : BaseActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_history)

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
            fetchDonationHistory(userId)
        } else {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDonationHistory(userId: Int) {
        ApiClient.instance.getDonationHistory(userId).enqueue(object : Callback<DonationHistoryResponse> {
            override fun onResponse(call: Call<DonationHistoryResponse>, response: Response<DonationHistoryResponse>) {
                if (response.isSuccessful) {
                    val history = response.body()?.history ?: emptyList()
                    displayHistory(history)
                } else {
                    displayHistory(emptyList())
                }
            }

            override fun onFailure(call: Call<DonationHistoryResponse>, t: Throwable) {
                Toast.makeText(this@DonationHistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                displayHistory(emptyList())
            }
        })
    }

    private fun displayHistory(history: List<Donation>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (history.isEmpty()) {
            val emptyView = inflater.inflate(R.layout.item_donation_history, container, false)
            emptyView.findViewById<TextView>(R.id.tv_date).text = "No history found"
            emptyView.findViewById<TextView>(R.id.tv_location).text = "Your donation records will appear here"
            emptyView.findViewById<TextView>(R.id.tv_units).text = ""
            emptyView.findViewById<TextView>(R.id.tv_status).visibility = View.GONE
            emptyView.findViewById<ImageView>(R.id.iv_icon).setImageResource(R.drawable.ic_clock)
            container.addView(emptyView)
            return
        }

        // Real history display logic
        for (donation in history) {
            val itemView = inflater.inflate(R.layout.item_donation_history, container, false)
            
            val tvDate = itemView.findViewById<TextView>(R.id.tv_date)
            val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
            val tvUnits = itemView.findViewById<TextView>(R.id.tv_units)
            val tvStatus = itemView.findViewById<TextView>(R.id.tv_status)

            tvDate.text = donation.donation_date
            tvLocation.text = donation.location ?: "Medical Center"
            
            val unitStr = if (donation.units <= 1) "unit" else "units"
            tvUnits.text = "${donation.units} $unitStr"
            
            if (donation.notes?.contains("Scheduled", ignoreCase = true) == true) {
                tvStatus.text = "Scheduled"
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                tvStatus.setTextColor(getColor(R.color.text_gray))
            } else {
                tvStatus.text = "Completed"
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                tvStatus.setTextColor(getColor(R.color.success_emerald))
            }

            itemView.setOnClickListener {
                val intent = Intent(this, AppointmentConfirmationActivity::class.java)
                val dateTime = donation.donation_date.split(" ")
                intent.putExtra("DATE", if (dateTime.isNotEmpty()) dateTime[0] else donation.donation_date)
                intent.putExtra("TIME", if (dateTime.size > 1) dateTime[1].substringBeforeLast(":") else "10:00 AM")
                intent.putExtra("RECEIVER_ID", -1)
                intent.putExtra("RECEIVER_NAME", donation.location ?: "Medical Center")
                intent.putExtra("BLOOD_GROUP", donation.blood_group)
                intent.putExtra("UNITS", donation.units)
                intent.putExtra("FROM_ACCEPT", false)
                intent.putExtra("IS_VIEW_ONLY", true) // Prevent re-saving to database
                startActivity(intent)
            }

            container.addView(itemView)
        }
    }
}
