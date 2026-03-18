package com.simats.lifeflow

import android.os.Bundle
import android.widget.ImageView

class NearbyHospitalsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospitals)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // List is now static in XML for the 5km radius display as requested
    }
}
