package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_confirmation)

        val tvDate = findViewById<TextView>(R.id.tv_conf_date)
        val tvTime = findViewById<TextView>(R.id.tv_conf_time)
        val btnHome = findViewById<Button>(R.id.btn_back_home)
        val btnChat = findViewById<Button>(R.id.btn_view_history) // Using the existing ID from XML

        // Retrieve passed data
        val selectedDate = intent.getStringExtra("DATE") ?: ""
        val selectedTime = intent.getStringExtra("TIME") ?: ""
        val fromAccept = intent.getBooleanExtra("FROM_ACCEPT", false)
        val receiverId = intent.getIntExtra("RECEIVER_ID", -1)
        val receiverName = intent.getStringExtra("RECEIVER_NAME") ?: "Patient"
        val bloodGroup = intent.getStringExtra("BLOOD_GROUP") ?: "O+"
        val units = intent.getIntExtra("UNITS", 1)

        // Get donor ID from SharedPreferences
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)

        tvDate.text = selectedDate
        tvTime.text = selectedTime
        
        findViewById<TextView>(R.id.tv_conf_location).text = receiverName
        findViewById<TextView>(R.id.tv_subtitle).text = "Your donation for $receiverName has been scheduled"

        if (userId != -1) {
            saveDonationToHistory(userId, selectedDate, bloodGroup, units, receiverName)
        } else {
            Toast.makeText(this, "Error: User session not found.", Toast.LENGTH_LONG).show()
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, DonorDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        if (fromAccept) {
            btnChat.text = "Chat with Patient"
            btnChat.setOnClickListener {
                val intent = Intent(this, PatientChatActivity::class.java)
                intent.putExtra("SENDER_ID", userId)
                intent.putExtra("RECEIVER_ID", receiverId)
                intent.putExtra("RECEIVER_NAME", receiverName)
                intent.putExtra("CHAT_STATUS", "CONNECTED")
                startActivity(intent)
                finish()
            }
            
            // Allow clicking history from a different way or just change logic?
            // Let's make btnHome go to dashboard where they can see history.
        } else {
            btnChat.setOnClickListener {
                val intent = Intent(this, DonationHistoryActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
        }
    }

    private fun saveDonationToHistory(userId: Int, date: String, bloodGroup: String, units: Int, location: String) {
        val donation = Donation(
            donor_id = userId,
            donation_date = date,
            units = units,
            blood_group = bloodGroup,
            location = location,
            notes = "Scheduled appointment"
        )

        ApiClient.instance.addDonation(donation).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AppointmentConfirmationActivity, "Donation history updated!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@AppointmentConfirmationActivity, "History sync failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
