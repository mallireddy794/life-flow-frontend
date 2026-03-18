package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonorRequirementsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_requirements)

        val userId = intent.getIntExtra("user_id", -1)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        
        val cardRequest1 = findViewById<View>(R.id.card_request_1)
        val tvPatientName1 = findViewById<TextView>(R.id.tv_patient_name_1)
        val btnAccept1 = findViewById<Button>(R.id.btn_accept_1)
        val btnDecline1 = findViewById<Button>(R.id.btn_decline_1)

        val cardRequest2 = findViewById<View>(R.id.card_request_2)
        val tvPatientName2 = findViewById<TextView>(R.id.tv_patient_name_2)
        val btnAccept2 = findViewById<Button>(R.id.btn_accept_2)
        val btnDecline2 = findViewById<Button>(R.id.btn_decline_2)

        val llNoRequests = findViewById<LinearLayout>(R.id.ll_no_requests)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (userId != -1) {
            checkAvailabilityAndFetchPatients(userId, cardRequest1, cardRequest2, tvPatientName1, tvPatientName2, llNoRequests)
        }

        btnDecline1.setOnClickListener {
            cardRequest1.visibility = View.GONE
            checkEmptyState(cardRequest1, cardRequest2, llNoRequests)
            Toast.makeText(this, "Request declined.", Toast.LENGTH_SHORT).show()
        }

        btnDecline2.setOnClickListener {
            cardRequest2.visibility = View.GONE
            checkEmptyState(cardRequest1, cardRequest2, llNoRequests)
            Toast.makeText(this, "Request declined.", Toast.LENGTH_SHORT).show()
        }

        // Default click listeners for static/placeholder data
        btnAccept1.setOnClickListener {
            val name = tvPatientName1.text.toString()
            acceptRequest(101, name)
        }

        btnAccept2.setOnClickListener {
            val name = tvPatientName2.text.toString()
            acceptRequest(102, name)
        }

        findViewById<Button>(R.id.btn_check_eligibility).setOnClickListener {
            val intent = Intent(this, EligibilityCheckActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkEmptyState(card1: View, card2: View, noRequestsLayout: LinearLayout) {
        if (card1.visibility == View.GONE && card2.visibility == View.GONE) {
            noRequestsLayout.visibility = View.VISIBLE
        }
    }

    private fun checkAvailabilityAndFetchPatients(
        userId: Int, 
        card1: View, 
        card2: View, 
        name1: TextView, 
        name2: TextView, 
        noRequestsLayout: LinearLayout
    ) {
        ApiClient.instance.getDonorAvailability(userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val status = response.body()?.get("is_available")
                    val isAvailable = when (status) {
                        is Boolean -> status
                        is Number -> status.toInt() == 1
                        is String -> status == "1" || status.lowercase() == "true"
                        else -> false
                    }

                    if (isAvailable) {
                        fetchPatients(card1, card2, name1, name2, noRequestsLayout)
                    } else {
                        card1.visibility = View.GONE
                        card2.visibility = View.GONE
                        noRequestsLayout.visibility = View.VISIBLE
                        Toast.makeText(this@DonorRequirementsActivity, "Go online to see nearby requests", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                card1.visibility = View.GONE
                card2.visibility = View.GONE
                noRequestsLayout.visibility = View.VISIBLE
            }
        })
    }

    private fun fetchPatients(card1: View, card2: View, name1: TextView, name2: TextView, noRequestsLayout: LinearLayout) {
        ApiClient.instance.getPatients().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val patients = response.body() ?: emptyList()
                    if (patients.isNotEmpty()) {
                        val p1 = patients[0]
                        val p1Id = p1.id ?: -1
                        val p1Name = p1.name ?: "Unknown"
                        
                        card1.visibility = View.VISIBLE
                        name1.text = p1Name
                        findViewById<Button>(R.id.btn_accept_1).setOnClickListener {
                            acceptRequest(p1Id, p1Name)
                        }
                        
                        if (patients.size > 1) {
                            val p2 = patients[1]
                            val p2Id = p2.id ?: -1
                            val p2Name = p2.name ?: "Unknown"
                            
                            card2.visibility = View.VISIBLE
                            name2.text = p2Name
                            findViewById<Button>(R.id.btn_accept_2).setOnClickListener {
                                acceptRequest(p2Id, p2Name)
                            }
                        } else {
                            card2.visibility = View.GONE
                        }
                        noRequestsLayout.visibility = View.GONE
                    } else {
                        card1.visibility = View.GONE
                        card2.visibility = View.GONE
                        noRequestsLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                card1.visibility = View.GONE
                card2.visibility = View.GONE
                noRequestsLayout.visibility = View.VISIBLE
            }
        })
    }

    private fun acceptRequest(patientId: Int, patientName: String) {
        Toast.makeText(this, "Request from $patientName Accepted! Please complete the eligibility check.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, EligibilityCheckActivity::class.java)
        intent.putExtra("RECEIVER_ID", patientId)
        intent.putExtra("RECEIVER_NAME", patientName)
        intent.putExtra("FROM_ACCEPT", true)
        startActivity(intent)
    }
}
