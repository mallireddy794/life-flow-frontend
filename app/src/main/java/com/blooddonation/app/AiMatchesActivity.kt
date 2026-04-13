package com.blooddonation.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AiMatchesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_matches)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fetchDonors()
    }

    private fun fetchDonors() {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)
        val userBloodGroup = sharedPrefs.getString("blood_group", "ALL") ?: "ALL"

        val fused = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    callEmergencyApi(userId, userBloodGroup, loc.latitude, loc.longitude)
                } else {
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callEmergencyApi(userId: Int, bloodGroup: String, lat: Double, lng: Double) {
        val req = EmergencySearchRequest(
            patientId = userId,
            bloodGroup = bloodGroup,
            lat = lat,
            lng = lng,
            unitsRequired = 1,
            radiusKm = 10.0
        )

        ApiClient.instance.emergencyDonors(req).enqueue(object : Callback<EmergencySearchResponse> {
            override fun onResponse(
                call: Call<EmergencySearchResponse>,
                response: Response<EmergencySearchResponse>
            ) {
                if (response.isSuccessful) {
                    val rankedDonors = response.body()?.nearbyDonors ?: emptyList()
                    setupDonorsList(rankedDonors)
                } else {
                    Toast.makeText(this@AiMatchesActivity, "AI search failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EmergencySearchResponse>, t: Throwable) {
                Toast.makeText(this@AiMatchesActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDonorsList(donors: List<RankedDonor>) {
        bindDonorCard(
            donors.getOrNull(0),
            nameView    = findViewById(R.id.tv_donor1_name),
            bloodView   = findViewById(R.id.tv_blood1),
            distView    = findViewById(R.id.tv_dist1),
            ratingView  = findViewById(R.id.tv_rating1),
            sentView    = findViewById(R.id.tv_sentiment1),
            contactBtn  = findViewById(R.id.btn_contact1),
            rateBtn     = findViewById(R.id.btn_rate1)
        )
        bindDonorCard(
            donors.getOrNull(1),
            nameView    = findViewById(R.id.tv_donor2_name),
            bloodView   = findViewById(R.id.tv_blood2),
            distView    = findViewById(R.id.tv_dist2),
            ratingView  = findViewById(R.id.tv_rating2),
            sentView    = findViewById(R.id.tv_sentiment2),
            contactBtn  = findViewById(R.id.btn_contact2),
            rateBtn     = findViewById(R.id.btn_rate2)
        )
        bindDonorCard(
            donors.getOrNull(2),
            nameView    = findViewById(R.id.tv_donor3_name),
            bloodView   = findViewById(R.id.tv_blood3),
            distView    = findViewById(R.id.tv_dist3),
            ratingView  = findViewById(R.id.tv_rating3),
            sentView    = findViewById(R.id.tv_sentiment3),
            contactBtn  = findViewById(R.id.btn_contact3),
            rateBtn     = findViewById(R.id.btn_rate3)
        )
    }

    private fun bindDonorCard(
        donor: RankedDonor?,
        nameView: TextView,
        bloodView: TextView,
        distView: TextView,
        ratingView: TextView,
        sentView: TextView,
        contactBtn: AppCompatButton,
        rateBtn: AppCompatButton
    ) {
        if (donor == null) {
            nameView.text = "No donor found"
            bloodView.text = "Blood Group: —"
            distView.text = "— km"
            ratingView.text = "No ratings yet"
            sentView.text = ""
            contactBtn.isEnabled = false
            contactBtn.alpha = 0.4f
            rateBtn.isEnabled = false
            rateBtn.alpha = 0.4f
            return
        }

        nameView.text  = donor.name ?: "Unknown Donor"
        bloodView.text = "Blood Group: ${donor.bloodGroup ?: "—"}"
        distView.text  = if (donor.distanceKm != null) String.format("%.1f km", donor.distanceKm) else "— km"

        val totalReviews = donor.totalReviews
        ratingView.text = if (totalReviews == 0)
            "No ratings yet"
        else
            String.format("%.1f (%d review%s)", donor.avgRating, totalReviews, if (totalReviews == 1) "" else "s")

        sentView.text = getSentimentLabel(donor.sentimentScore)

        contactBtn.setOnClickListener {
            openChat(donor.donorId, donor.name ?: "Donor")
        }

        rateBtn.setOnClickListener {
            openRateDonor(donor.donorId, donor.name ?: "Donor")
        }
    }

    private fun getSentimentLabel(score: Float): String = when {
        score >= 0.8f -> "Very Positive"
        score >= 0.6f -> "Positive"
        score >= 0.4f -> "Neutral"
        score >= 0.2f -> "Negative"
        score > 0f    -> "Very Negative"
        else          -> ""
    }

    private fun openChat(receiverId: Int, receiverName: String) {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val senderId = sharedPrefs.getInt("user_id", -1)
        val intent = Intent(this, PatientChatActivity::class.java)
        intent.putExtra("SENDER_ID", senderId)
        intent.putExtra("RECEIVER_ID", receiverId)
        intent.putExtra("RECEIVER_NAME", receiverName)
        startActivity(intent)
    }

    private fun openRateDonor(donorId: Int, donorName: String) {
        val intent = Intent(this, RateDonorActivity::class.java)
        intent.putExtra("DONOR_ID", donorId)
        intent.putExtra("DONOR_NAME", donorName)
        startActivity(intent)
    }
}
