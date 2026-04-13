package com.blooddonation.app

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RateDonorActivity : BaseActivity() {

    private var donorId: Int = -1
    private var patientId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_donor)

        donorId = intent.getIntExtra("DONOR_ID", -1)
        val donorName = intent.getStringExtra("DONOR_NAME") ?: ""

        if (donorId == -1 || donorName.isEmpty()) {
            Toast.makeText(this, "No donor selected. Please select a donor first.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Get current patient's ID
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        patientId = sharedPrefs.getInt("user_id", -1)

        val tvDonorName = findViewById<TextView>(R.id.tv_donor_name)
        val ratingBar = findViewById<RatingBar>(R.id.rating_bar)
        val etReview = findViewById<TextInputEditText>(R.id.et_review)
        val btnSubmit = findViewById<AppCompatButton>(R.id.btn_submit_rating)
        val ivClose = findViewById<ImageView>(R.id.iv_close)

        tvDonorName.text = donorName

        ivClose.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val reviewText = etReview.text?.toString() ?: ""

            if (donorId == -1 || patientId == -1) {
                Toast.makeText(this, "Error: Missing donor or patient ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitRating(donorId, patientId, rating, reviewText)
        }
    }

    private fun submitRating(donorId: Int, patientId: Int, rating: Int, reviewText: String) {
        val request = DonorRatingRequest(donorId, patientId, rating, reviewText)
        
        ApiClient.instance.rateDonor(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val sentimentScore = response.body()?.get("sentiment_score")
                    val message = "Thank you! Rating submitted."
                    Toast.makeText(this@RateDonorActivity, message, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorString = response.errorBody()?.string()
                    var errorMsg = response.message()
                    try {
                        if (!errorString.isNullOrEmpty()) {
                            val jsonObj = org.json.JSONObject(errorString)
                            if (jsonObj.has("error")) {
                                errorMsg = jsonObj.getString("error")
                            }
                        }
                    } catch (e: Exception) {
                        // Keep generic if parsing fails
                    }
                    Toast.makeText(this@RateDonorActivity, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@RateDonorActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
