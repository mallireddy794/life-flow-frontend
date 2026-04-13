package com.blooddonation.app

import android.content.Context
import android.content.res.Configuration
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val fontScaleProgress = sharedPrefs.getInt("font_scale_progress", 2)
        
        val fontScale = when (fontScaleProgress) {
            0 -> 0.85f
            1 -> 0.9f
            2 -> 1.0f 
            3 -> 1.15f
            4 -> 1.3f
            else -> 1.0f
        }

        val config = Configuration(newBase.resources.configuration)
        config.fontScale = fontScale
        val context = newBase.createConfigurationContext(config)
        
        super.attachBaseContext(context)
    }
    
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        return email.isNotEmpty() && java.util.regex.Pattern.compile(emailPattern).matcher(email).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }
}
