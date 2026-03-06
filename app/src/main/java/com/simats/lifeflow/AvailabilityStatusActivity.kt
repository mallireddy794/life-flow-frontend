package com.simats.lifeflow

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AvailabilityStatusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_availability_status)

        val userId = intent.getIntExtra("user_id", -1)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val switchAvailability = findViewById<SwitchCompat>(R.id.switch_availability)
        val tvStatusName = findViewById<TextView>(R.id.tv_status_name)
        val tvStatusDesc = findViewById<TextView>(R.id.tv_status_desc)
        val btnSave = findViewById<Button>(R.id.btn_save_changes)
        val innerIndicator = findViewById<android.view.View>(R.id.inner_indicator)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvStatusName.text = "Available to Donate"
                tvStatusDesc.text = "You will receive notifications for blood requests"
                innerIndicator.background = ContextCompat.getDrawable(this, R.drawable.bg_green_circle_solid)
            } else {
                tvStatusName.text = "Unavailable to Donate"
                tvStatusDesc.text = "You will not receive new notification requests"
                innerIndicator.background = ContextCompat.getDrawable(this, R.drawable.bg_role_card) // Grayish background
            }
        }

        if (userId != -1) {
            Toast.makeText(this, "Syncing status for User: $userId", Toast.LENGTH_SHORT).show()
            fetchInitialStatus(userId, switchAvailability)
        } else {
            Toast.makeText(this, "Critical: User ID -1 (Intent Error)", Toast.LENGTH_LONG).show()
            tvStatusName.text = "Error Loading Status"
        }

        btnSave.setOnClickListener {
            if (userId != -1) {
                val isAvailable = switchAvailability.isChecked
                updateAvailabilityOnServer(userId, isAvailable)
            } else {
                Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchInitialStatus(userId: Int, switch: SwitchCompat) {
        ApiClient.instance.getDonorAvailability(userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("Availability", "Response Body: $body")
                    
                    val status = body?.get("is_available")
                    android.util.Log.d("Availability", "Raw is_available: $status (Type: ${status?.javaClass?.simpleName})")
                    
                    val isAvail = when (status) {
                        is Boolean -> status
                        is Number -> status.toInt() == 1
                        is String -> status == "1" || status.lowercase() == "true"
                        else -> false // Default to false if unknown or missing
                    }
                    
                    android.util.Log.d("Availability", "Setting switch to: $isAvail")
                    switch.isChecked = isAvail
                    updateUI(isAvail)
                    
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("Availability", "Failed to fetch status: ${response.code()} - $errorBody")
                    if (response.code() == 404) {
                        Toast.makeText(this@AvailabilityStatusActivity, "Backend GET route missing (404)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                android.util.Log.e("Availability", "Network error fetching status", t)
                Toast.makeText(this@AvailabilityStatusActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(isChecked: Boolean) {
        val tvStatusName = findViewById<TextView>(R.id.tv_status_name)
        val tvStatusDesc = findViewById<TextView>(R.id.tv_status_desc)
        val innerIndicator = findViewById<android.view.View>(R.id.inner_indicator)
        
        if (isChecked) {
            tvStatusName.text = "Available to Donate"
            tvStatusDesc.text = "You will receive notifications for blood requests"
            innerIndicator.background = ContextCompat.getDrawable(this, R.drawable.bg_green_circle_solid)
        } else {
            tvStatusName.text = "Unavailable to Donate"
            tvStatusDesc.text = "You will not receive new notification requests"
            innerIndicator.background = ContextCompat.getDrawable(this, R.drawable.bg_role_card)
        }
    }

    private fun updateAvailabilityOnServer(userId: Int, isAvailable: Boolean) {
        val data = mapOf("is_available" to isAvailable)
        
        ApiClient.instance.toggleAvailability(userId, data).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val statusText = if (isAvailable) "Available" else "Unavailable"
                    Toast.makeText(this@AvailabilityStatusActivity, "Status updated to $statusText", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (response.code() == 403) {
                    Toast.makeText(this@AvailabilityStatusActivity, "Not eligible to donate yet", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@AvailabilityStatusActivity, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@AvailabilityStatusActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
