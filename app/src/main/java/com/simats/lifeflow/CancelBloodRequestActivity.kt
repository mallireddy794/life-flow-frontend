package com.simats.lifeflow

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class CancelBloodRequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_blood_request)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val rgReasons = findViewById<RadioGroup>(R.id.rg_reasons)
        val etOtherReason = findViewById<EditText>(R.id.et_other_reason)
        val btnConfirmCancel = findViewById<AppCompatButton>(R.id.btn_confirm_cancel)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        rgReasons.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_reason5) {
                etOtherReason.visibility = View.VISIBLE
            } else {
                etOtherReason.visibility = View.GONE
            }
        }

        btnConfirmCancel.setOnClickListener {
            val selectedId = rgReasons.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a reason for cancellation", Toast.LENGTH_SHORT).show()
            } else {
                // Here you would typically call an API to cancel the request
                Toast.makeText(this, "Blood request cancelled successfully", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
