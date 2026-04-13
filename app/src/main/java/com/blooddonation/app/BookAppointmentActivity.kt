package com.blooddonation.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.*

class BookAppointmentActivity : BaseActivity() {

    private var selectedTimeSlot: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val etDate = findViewById<EditText>(R.id.et_appointment_date)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_appointment)

        // Time Slots
        val slots = listOf<TextView>(
            findViewById(R.id.slot_09am),
            findViewById(R.id.slot_10am),
            findViewById(R.id.slot_11am),
            findViewById(R.id.slot_02pm),
            findViewById(R.id.slot_03pm),
            findViewById(R.id.slot_04pm)
        )

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        etDate.setOnClickListener {
            showDatePickerDialog(etDate)
        }

        slots.forEach { slot ->
            slot.setOnClickListener {
                selectedTimeSlot?.isSelected = false
                slot.isSelected = true
                selectedTimeSlot = slot
            }
        }

        val fromAccept = intent.getBooleanExtra("FROM_ACCEPT", false)
        val receiverId = intent.getIntExtra("RECEIVER_ID", -1)
        val receiverName = intent.getStringExtra("RECEIVER_NAME")

        btnConfirm.setOnClickListener {
            val date = etDate.text.toString()
            if (date.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedTimeSlot == null) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Navigate to Appointment Confirmation Screen
            val intent = Intent(this, AppointmentConfirmationActivity::class.java)
            intent.putExtra("DATE", date)
            intent.putExtra("TIME", selectedTimeSlot?.text.toString())
            intent.putExtra("FROM_ACCEPT", fromAccept)
            intent.putExtra("RECEIVER_ID", receiverId)
            intent.putExtra("REQUEST_ID", this.intent.getIntExtra("REQUEST_ID", -1))
            intent.putExtra("RECEIVER_NAME", receiverName)
            intent.putExtra("BLOOD_GROUP", this.intent.getStringExtra("BLOOD_GROUP"))
            intent.putExtra("UNITS", this.intent.getIntExtra("UNITS", 1))
            startActivity(intent)
            finish()
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dateStr = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editText.setText(dateStr)
        }, year, month, day)

        // Don't allow past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }
}
