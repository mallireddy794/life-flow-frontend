package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AiMatchesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_matches)

        val ivBack = findViewById<ImageView>(R.id.iv_back)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fetchDonors()
    }

    private fun fetchDonors() {
        ApiClient.instance.getDonors().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val donors = response.body() ?: emptyList()
                    setupDonorsList(donors.shuffled())
                } else {
                    Toast.makeText(this@AiMatchesActivity, "Failed to load donors", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@AiMatchesActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDonorsList(donors: List<User>) {
        val tvName1 = findViewById<TextView>(R.id.tv_donor1_name)
        val tvName2 = findViewById<TextView>(R.id.tv_donor2_name)
        val tvName3 = findViewById<TextView>(R.id.tv_donor3_name)
        
        val btnContact1 = findViewById<AppCompatButton>(R.id.btn_contact1)
        val btnContact2 = findViewById<AppCompatButton>(R.id.btn_contact2)
        val btnContact3 = findViewById<AppCompatButton>(R.id.btn_contact3)

        if (donors.isNotEmpty()) {
            tvName1.text = donors[0].name
            btnContact1.setOnClickListener { openChat(donors[0]) }
        }
        if (donors.size > 1) {
            tvName2.text = donors[1].name
            btnContact2.setOnClickListener { openChat(donors[1]) }
        }
        if (donors.size > 2) {
            tvName3.text = donors[2].name
            btnContact3.setOnClickListener { openChat(donors[2]) }
        }
    }

    private fun openChat(donor: User) {
        val intent = Intent(this, PatientChatActivity::class.java)
        intent.putExtra("RECEIVER_ID", donor.id)
        intent.putExtra("RECEIVER_NAME", donor.name)
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedUserId = sharedPrefs.getInt("user_id", -1)
        intent.putExtra("SENDER_ID", savedUserId) 
        startActivity(intent)
    }
}
