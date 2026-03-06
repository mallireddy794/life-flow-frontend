package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class NearbyHospitalsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospitals)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val contactButtons = listOf(
            findViewById<AppCompatButton>(R.id.btn_contact1),
            findViewById<AppCompatButton>(R.id.btn_contact2),
            findViewById<AppCompatButton>(R.id.btn_contact3)
        )

        val hospitals = listOf("City General Hospital", "St. Mary Medical Center", "Regional Health Center")

        contactButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val intent = Intent(this, HospitalChatActivity::class.java)
                intent.putExtra("HOSPITAL_NAME", hospitals[index])
                startActivity(intent)
            }
        }
    }
}
