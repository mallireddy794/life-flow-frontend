
package com.simats.lifeflow

data class User(
    val id: Int,
    val name: String,
    val role: String,
    val email: String? = null
)

data class LoginResponse(
    val message: String,
    val user: User
)

data class DonorProfile(
    val phone: String,
    val blood_group: String,
    val age: Int,
    val city: String
)

data class PatientProfile(
    val phone: String,
    val blood_group: String,
    val hospital_name: String,
    val city: String
)

data class HospitalProfile(
    val hospital_name: String,
    val phone: String,
    val city: String,
    val address: String
)

data class BloodRequest(
    val blood_group: String,
    val units_required: Int,
    val urgency_level: String,
    val city: String
)

data class PatientRequest(
    val request_id: Int,
    val blood_group: String,
    val units_required: Int,
    val urgency_level: String,
    val status: String,
    val city: String
)

data class HospitalRequest(
    val request_id: Int,
    val blood_group: String,
    val units_required: Int,
    val urgency_level: String,
    val status: String,
    val city: String,
    val created_at: String
)

data class ChatMessage(
    val id: Int? = null,
    val chat_id: String? = null,
    val sender_id: Int,
    val receiver_id: Int,
    val message: String,
    val created_at: String? = null
)

data class InboxItem(
    val chat_id: String,
    val last_message: String,
    val last_time: String,
    val unread_count: Int,
    val other_user: User
)

data class Donation(
    val id: Int? = null,
    val donor_id: Int? = null,
    val donation_date: String,
    val units: Int,
    val blood_group: String,
    val location: String?,
    val notes: String?,
    val created_at: String? = null
)

data class DonationHistoryResponse(
    val donor_id: Int?,
    val user_id: Int?,
    val count: Int,
    val history: List<Donation>
)

data class NearbyRequest(
    val bloodGroup: String,
    val units: Int,
    val hospitalName: String,
    val location: String,
    val urgency: String,
    val patientId: Int
)
