package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView

class EligibilityCheckActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eligibility_check)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnCheck = findViewById<Button>(R.id.btn_check_eligibility)
        val resultCard = findViewById<CardView>(R.id.result_card)
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)

        val fromAccept = intent.getBooleanExtra("FROM_ACCEPT", false)
        val receiverId = intent.getIntExtra("RECEIVER_ID", -1)
        val receiverName = intent.getStringExtra("RECEIVER_NAME") ?: "Patient"

        // Setup Questions Logic
        val q1 = setupQuestion(R.id.q1_yes, R.id.q1_no)
        val q2 = setupQuestion(R.id.q2_yes, R.id.q2_no)
        val q3 = setupQuestion(R.id.q3_yes, R.id.q3_no)
        val q4 = setupQuestion(R.id.q4_yes, R.id.q4_no)
        val q5 = setupQuestion(R.id.q5_yes, R.id.q5_no)
        val q6 = setupQuestion(R.id.q6_yes, R.id.q6_no)
        val q7 = setupQuestion(R.id.q7_yes, R.id.q7_no)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCheck.setOnClickListener {
            // Simplified logic: Check if all 7 questions are answered
            val allAnswered = isAnswered(q1) && isAnswered(q2) && isAnswered(q3) && 
                              isAnswered(q4) && isAnswered(q5) && isAnswered(q6) && isAnswered(q7)

            if (!allAnswered) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Logic for eligibility based on the image:
            // High probability eligibility if:
            // Q1 (Age): Yes
            // Q2 (Weight): Yes
            // Q3 (Health): Yes
            // Q4 (Surgery): No
            // Q5 (Tattoo): No
            // Q6 (Medication): No
            // Q7 (Disorders): No
            
            val isEligible = q1.first.isSelected && q2.first.isSelected && q3.first.isSelected &&
                             q4.second.isSelected && q5.second.isSelected && q6.second.isSelected && 
                             q7.second.isSelected

            if (isEligible) {
                if (fromAccept) {
                    // Update text if from accept
                    findViewById<Button>(R.id.btn_book_appointment).text = "Book Appointment"
                    findViewById<TextView>(R.id.tv_result_description).text = "Great news! You meet the basic requirements to donate blood. Please schedule your donation appointment."
                }
                resultCard.visibility = View.VISIBLE
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
            } else {
                resultCard.visibility = View.GONE
                Toast.makeText(this, "Based on your answers, you may not be eligible to donate today.", Toast.LENGTH_LONG).show()
            }
        }





        findViewById<Button>(R.id.btn_book_appointment).setOnClickListener {
            val intent = Intent(this, BookAppointmentActivity::class.java)
            if (fromAccept) {
                intent.putExtra("FROM_ACCEPT", true)
                intent.putExtra("RECEIVER_ID", receiverId)
                intent.putExtra("REQUEST_ID", this.intent.getIntExtra("REQUEST_ID", -1))
                intent.putExtra("RECEIVER_NAME", receiverName)
                intent.putExtra("BLOOD_GROUP", this.intent.getStringExtra("BLOOD_GROUP"))
                intent.putExtra("UNITS", this.intent.getIntExtra("UNITS", 1))
            }
            startActivity(intent)
            if (fromAccept) finish()
        }
    }

    private fun setupQuestion(yesId: Int, noId: Int): Pair<TextView, TextView> {
        val yes = findViewById<TextView>(yesId)
        val no = findViewById<TextView>(noId)

        yes.setOnClickListener {
            yes.isSelected = true
            no.isSelected = false
        }
        no.setOnClickListener {
            no.isSelected = true
            yes.isSelected = false
        }

        return Pair(yes, no)
    }

    private fun isAnswered(question: Pair<TextView, TextView>): Boolean {
        return question.first.isSelected || question.second.isSelected
    }
}
