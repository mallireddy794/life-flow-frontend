package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentConfirmationActivity : BaseActivity() {
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

        val isViewOnly = intent.getBooleanExtra("IS_VIEW_ONLY", false)

        if (userId != -1 && !isViewOnly) {
            saveDonationToHistory(userId, selectedDate, bloodGroup, units, receiverName)
            
            val requestId = intent.getIntExtra("REQUEST_ID", -1)
            if (fromAccept && requestId != -1) {
                updateDonorRequestStatus(requestId, "SCHEDULED")
            }
        } else if (userId == -1) {
            Toast.makeText(this, "Error: User session not found.", Toast.LENGTH_LONG).show()
        }

        if (fromAccept) {
            btnHome.text = "Chat with Patient"
            btnHome.setOnClickListener {
                val intent = Intent(this, PatientChatActivity::class.java)
                intent.putExtra("SENDER_ID", userId)
                intent.putExtra("RECEIVER_ID", receiverId)
                intent.putExtra("RECEIVER_NAME", receiverName)
                intent.putExtra("CHAT_STATUS", "CONNECTED")
                startActivity(intent)
                finish()
            }
        } else {
            btnHome.setOnClickListener {
                val intent = Intent(this, DonorDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        btnChat.setOnClickListener {
            val intent = Intent(this, DonationHistoryActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }
    }

    private fun saveDonationToHistory(userId: Int, date: String, bloodGroup: String, units: Int, location: String) {
        val formattedDateTime = formatDateTime(date, intent.getStringExtra("TIME") ?: "10:00 AM")
        
        val donation = Donation(
            donor_id = userId,
            donation_date = formattedDateTime,
            units = units,
            blood_group = bloodGroup,
            location = location,
            notes = "Scheduled appointment"
        )

        ApiClient.instance.addDonation(donation).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AppointmentConfirmationActivity, "Donation history updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AppointmentConfirmationActivity, "Sync failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@AppointmentConfirmationActivity, "History sync failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatDateTime(dateStr: String, timeStr: String): String {
        return try {
            // dateStr: "DD/MM/YYYY", timeStr: "HH:MM AM"
            val dateParts = dateStr.split("/")
            val day = dateParts[0].padStart(2, '0')
            val month = dateParts[1].padStart(2, '0')
            val year = dateParts[2]

            val timeParts = timeStr.trim().split(" ")
            val hms = timeParts[0].split(":")
            var hour = hms[0].toInt()
            val minute = if (hms.size > 1) hms[1].padStart(2, '0') else "00"
            val ampm = if (timeParts.size > 1) timeParts[1] else "AM"

            if (ampm.equals("PM", ignoreCase = true) && hour < 12) hour += 12
            if (ampm.equals("AM", ignoreCase = true) && hour == 12) hour = 0

            "$year-$month-$day ${hour.toString().padStart(2, '0')}:$minute:00"
        } catch (e: Exception) {
            // Fallback to a safe current-ish date if parsing fails
            "2026-03-27 10:00:00"
        }
    }

    private fun updateDonorRequestStatus(requestId: Int, status: String) {
        val update = RequestStatusUpdate(
            requestId = requestId,
            status = status
        )

        ApiClient.instance.updateRequestStatus(update).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    android.util.Log.d("ApptConf", "Request status updated to $status")
                }
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                android.util.Log.e("ApptConf", "Status update failed", t)
            }
        })
    }
}
