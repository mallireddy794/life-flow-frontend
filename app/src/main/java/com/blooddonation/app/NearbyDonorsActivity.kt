package com.blooddonation.app

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NearbyDonorsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_donors)

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
                    displayDonors(donors.shuffled())
                } else {
                    Toast.makeText(this@NearbyDonorsActivity, "Failed to load donors", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@NearbyDonorsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayDonors(donors: List<User>) {
        val donorViews = listOf(
            Pair(findViewById<TextView>(R.id.tv_name1), findViewById<AppCompatButton>(R.id.btn_call1)),
            Pair(findViewById<TextView>(R.id.tv_name2), findViewById<AppCompatButton>(R.id.btn_call2)),
            Pair(findViewById<TextView>(R.id.tv_name3), findViewById<AppCompatButton>(R.id.btn_call3)),
            Pair(findViewById<TextView>(R.id.tv_name4), findViewById<AppCompatButton>(R.id.btn_call4))
        )

        // Hide all first
        donorViews.forEach { (nameTv, callBtn) ->
            (nameTv.parent as? View)?.visibility = View.GONE
        }

        donors.forEachIndexed { index, donor ->
            if (index < donorViews.size) {
                val (nameTv, callBtn) = donorViews[index]
                (nameTv.parent as? View)?.visibility = View.VISIBLE
                nameTv.text = donor.name
                callBtn.setOnClickListener {
                    Toast.makeText(this@NearbyDonorsActivity, "Calling ${donor.name}...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
