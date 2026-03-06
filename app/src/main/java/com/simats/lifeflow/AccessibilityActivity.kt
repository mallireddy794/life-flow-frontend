package com.simats.lifeflow

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AccessibilityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accessibility)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSave = findViewById<Button>(R.id.btn_save_accessibility)
        val seekFontSize = findViewById<SeekBar>(R.id.seek_font_size)
        val tvFontSizeVal = findViewById<TextView>(R.id.tv_font_size_val)

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
            Toast.makeText(this, "Accessibility changes saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
