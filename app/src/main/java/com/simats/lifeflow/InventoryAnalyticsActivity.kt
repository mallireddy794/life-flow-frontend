package com.simats.lifeflow

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class InventoryAnalyticsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_analytics)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupBars()
    }

    private fun setupBars() {
        // Labels matching the provided design image
        val types = listOf("A+", "A-", "B+", "B-", "AB-", "O+", "O-", "AB+")
        // Values roughly matching the heights in the image (proportional to 200 max)
        val values = listOf(120, 85, 95, 70, 60, 45, 205, 110) 
        
        val itemIds = listOf(
            R.id.bar_1, R.id.bar_2, R.id.bar_3, R.id.bar_4, 
            R.id.bar_5, R.id.bar_6, R.id.bar_7, R.id.bar_8
        )

        for (i in itemIds.indices) {
            val itemView = findViewById<LinearLayout>(itemIds[i])
            val bar = itemView.findViewById<View>(R.id.bar_view)
            val label = itemView.findViewById<TextView>(R.id.tv_label)

            label.text = types[i]
            
            // Set bar height dynamically based on value
            val params = bar.layoutParams
            params.height = dpToPx(values[i])
            bar.layoutParams = params
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
