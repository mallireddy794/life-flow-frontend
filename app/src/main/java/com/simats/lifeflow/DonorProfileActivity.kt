package com.simats.lifeflow

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DonorProfileActivity : BaseActivity() {

    private val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_profile)

        var userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            userId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        }

        val etBloodGroup = findViewById<EditText>(R.id.et_blood_group)
        val etDonationDate = findViewById<EditText>(R.id.et_donation_date)
        val btnSave = findViewById<Button>(R.id.btn_save)

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
                val profile = DonorProfile(
                    blood_group = bloodGroup,
                    phone = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("user_phone", "") ?: "",
                    city = "Update Required",
                    last_donation_date = date
                )
                
                ApiClient.instance.updateDonorProfile(userId, profile).enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@DonorProfileActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@DonorProfileActivity, DonorDashboardActivity::class.java)
                            intent.putExtra("user_id", userId)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@DonorProfileActivity, "Failed to save to server", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        Toast.makeText(this@DonorProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
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
