package com.simats.lifeflow

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RequestHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_history)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup Item 1
        setupItem(
            findViewById(R.id.item_1),
            "March 18, 2024",
            "Blood Type: O+",
            "2 units",
            "Completed",
            R.drawable.bg_status_completed,
            "#166534"
        )

        // Setup Item 2
        val item2 = findViewById<LinearLayout>(R.id.item_2)
        setupItem(
            item2,
            "March 15, 2024",
            "Blood Type: A-",
            "1 unit",
            "In Progress",
            R.drawable.bg_status_in_progress,
            "#854D0E"
        )
        item2.setOnClickListener {
            val intent = android.content.Intent(this, RequestTrackingActivity::class.java)
            startActivity(intent)
        }

        // Setup Item 3
        setupItem(
            findViewById(R.id.item_3),
            "March 10, 2024",
            "Blood Type: B+",
            "3 units",
            "Completed",
            R.drawable.bg_status_completed,
            "#166534"
        )
    }

    private fun setupItem(
        view: LinearLayout,
        date: String,
        bloodType: String,
        units: String,
        status: String,
        bgRes: Int,
        textColorHex: String
    ) {
        view.findViewById<TextView>(R.id.tv_date).text = date
        view.findViewById<TextView>(R.id.tv_blood_type).text = bloodType
        view.findViewById<TextView>(R.id.tv_units).text = units
        
        val badge = view.findViewById<TextView>(R.id.tv_status_badge)
        badge.text = status
        badge.setBackgroundResource(bgRes)
        badge.setTextColor(android.graphics.Color.parseColor(textColorHex))
    }
}
