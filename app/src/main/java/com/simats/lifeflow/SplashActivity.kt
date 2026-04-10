package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for 2 seconds then transition to SubscriptionActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, SubscriptionActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
