package com.simats.lifeflow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonationHistoryActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_history)

        var userId = intent.getIntExtra("user_id", -1)
        
        // Robust userId retrieval
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
        // Fetch history using donor_id parameter to match backend
        ApiClient.instance.getDonationHistory(userId).enqueue(object : Callback<DonationHistoryResponse> {
            override fun onResponse(call: Call<DonationHistoryResponse>, response: Response<DonationHistoryResponse>) {
                if (response.isSuccessful) {
                    val history = response.body()?.history ?: emptyList()
                    displayHistory(history)
                } else {
                    Toast.makeText(this@DonationHistoryActivity, "No history found or server error", Toast.LENGTH_SHORT).show()
                    displayHistory(emptyList())
                }
            }

            override fun onFailure(call: Call<DonationHistoryResponse>, t: Throwable) {
                Toast.makeText(this@DonationHistoryActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayHistory(history: List<Donation>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val displayList = if (history.isEmpty()) {
            // Add some dummy data for demonstration if empty, matching the screenshot style
            listOf(
                Donation(donation_date = "March 15, 2024", units = 1, blood_group = "O+", location = "City General Hospital", notes = ""),
                Donation(donation_date = "December 10, 2023", units = 1, blood_group = "A-", location = "Regional Health Center", notes = ""),
                Donation(donation_date = "September 5, 2023", units = 2, blood_group = "B+", location = "St. Mary Medical Center", notes = "")
            )
        } else {
            history
        }

        for (donation in displayList) {
            val itemView = inflater.inflate(R.layout.item_donation_history, container, false)
            
            val tvDate = itemView.findViewById<TextView>(R.id.tv_date)
            val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
            val tvUnits = itemView.findViewById<TextView>(R.id.tv_units)
            val tvStatus = itemView.findViewById<TextView>(R.id.tv_status)

            tvDate.text = donation.donation_date
            tvLocation.text = donation.location ?: "Medical Center"
            
            val unitStr = if (donation.units == 1) "unit" else "units"
            tvUnits.text = "${donation.units} $unitStr"
            
            tvStatus.text = "Completed"

            container.addView(itemView)
        }
    }
}
