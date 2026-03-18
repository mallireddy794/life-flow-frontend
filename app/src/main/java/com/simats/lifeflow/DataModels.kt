package com.simats.lifeflow

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val blood_group: String? = null,
    val role: String? = null,
    val available_to_donate: Boolean = false
)

data class LoginResponse(
    val message: String,
    val user: User
)

data class DonorProfile(
    val phone: String = "",
    val blood_group: String = "",
    val age: Int = 0,
    val city: String = ""
)

data class PatientProfile(
    val phone: String = "",
    val blood_group: String = "",
    val hospital_name: String = "",
    val city: String = ""
)

data class BloodRequest(
    val blood_group: String = "",
    val units_required: Int = 0,
    val urgency_level: String = "",
    val city: String = ""
)

data class PatientRequest(
    val request_id: Int = 0,
    val blood_group: String = "",
    val units_required: Int = 0,
    val urgency_level: String = "",
    val status: String = "",
    val city: String = ""
)

// Matches DonorDonation table with default values to fix existing code
data class Donation(
    val id: Int? = null,
    val donor_id: Int = 0,
    val donation_date: String = "",
    val units: Int = 0,
    val blood_group: String = "",
    val location: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

data class DonationHistoryResponse(
    val history: List<Donation> = emptyList()
)

// Matches ChatMessage table with default values to fix existing code
data class ChatMessage(
    val id: Int? = null,
    val chat_id: String? = null,
    val sender_id: Int = 0,
    val receiver_id: Int = 0,
    val message: String = "",
    val created_at: String? = null
)

data class InboxItem(
    val chat_id: String = "",
    val last_message: String = "",
    val last_time: String = "",
    val unread_count: Int = 0,
    val other_user: User? = null
)

data class NearbyRequest(
    val bloodGroup: String = "",
    val units: Int = 0,
    val hospitalName: String = "",
    val location: String = "",
    val urgency: String = "",
    val patientId: Int = 0
)
