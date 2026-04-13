package com.blooddonation.app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class AccessibilityActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accessibility)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSave = findViewById<Button>(R.id.btn_save_accessibility)
        val seekFontSize = findViewById<SeekBar>(R.id.seek_font_size)
        val tvFontSizeVal = findViewById<TextView>(R.id.tv_font_size_val)

        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedProgress = sharedPrefs.getInt("font_scale_progress", 2)
        seekFontSize.progress = savedProgress
        tvFontSizeVal.text = "${12 + (savedProgress * 2)}px"

        btnBack.setOnClickListener {
            onBackPressed()
        }

        seekFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = 12 + (progress * 2)
                tvFontSizeVal.text = "${size}px"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            val progress = seekFontSize.progress
            sharedPrefs.edit().putInt("font_scale_progress", progress).apply()
            
            Toast.makeText(this, "Accessibility changes saved", Toast.LENGTH_SHORT).show()
            
            recreate()
            finish()
        }
    }
}
