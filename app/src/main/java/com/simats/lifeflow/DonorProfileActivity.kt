package com.simats.lifeflow

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class DonorProfileActivity : AppCompatActivity() {

    private val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_profile)

        val userId = intent.getIntExtra("user_id", -1)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val etBloodGroup = findViewById<EditText>(R.id.et_blood_group)
        val etDonationDate = findViewById<EditText>(R.id.et_donation_date)
        val btnSave = findViewById<Button>(R.id.btn_save)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configure blood group selection dialog
        etBloodGroup.isFocusable = false
        etBloodGroup.isClickable = true
        etBloodGroup.setOnClickListener {
            showBloodGroupDialog(etBloodGroup)
        }

        // Configure date picker
        etDonationDate.isFocusable = false
        etDonationDate.isClickable = true
        etDonationDate.setOnClickListener {
            showDatePicker(etDonationDate)
        }

        btnSave.setOnClickListener {
            val bloodGroup = etBloodGroup.text.toString().trim()
            val date = etDonationDate.text.toString().trim()

            if (bloodGroup.isNotEmpty() && date.isNotEmpty()) {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DonorDashboardActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBloodGroupDialog(etBlood: EditText) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select blood group")
        builder.setItems(bloodGroups) { _, which ->
            etBlood.setText(bloodGroups[which])
        }
        builder.show()
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }
}
