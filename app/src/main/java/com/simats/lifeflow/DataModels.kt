package com.simats.lifeflow

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val blood_group: String? = null,
    val role: String? = null,
    val available_to_donate: Boolean = false,
    @SerializedName("is_profile_complete") val isProfileComplete: Boolean = false
)

data class LoginResponse(
    val message: String,
    val user: User
)

data class DonorProfile(
    val phone: String = "",
    val blood_group: String = "",
    val age: Int = 0,
    val city: String = "",
    val last_donation_date: String = ""
)

data class PatientProfile(
    val phone: String = "",
    val blood_group: String = "",
    val hospital_name: String = "",
    val city: String = ""
)

data class BloodRequest(
    @SerializedName("patient_name") val patient_name: String? = null,
    @SerializedName("hospital_name") val hospital_name: String? = null,
    @SerializedName("contact_number") val contact_number: String? = null,
    @SerializedName("blood_group") val blood_group: String = "",
    @SerializedName("units_required") val units_required: Int = 0,
    @SerializedName("urgency_level") val urgency_level: String = "",
    @SerializedName("city") val city: String = ""
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
    val id: Int = 0,
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("donor_id") val donorId: Int = 0,
    @SerializedName("blood_group") val bloodGroup: String = "",
    @SerializedName("units_needed") val units: Int = 0,
    @SerializedName("urgency") val urgency: String = "",
    val message: String? = null,
    val status: String = "",
    @SerializedName("created_at") val createdAt: String? = null,
    
    // UI mapping helpers (Backwards compatibility with existing Adapter)
    @SerializedName("hospital_name") val hospitalName: String = "LifeFlow Center",
    @SerializedName("city") val location: String = "Nearby"
)

data class NearbyDonor(
    @SerializedName("donor_user_id") val donorUserId: Int? = null,
    val name: String? = null,
    val phone: String? = null,
    @SerializedName("blood_group") val bloodGroup: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("distance_km") val distanceKm: Double? = null,
    val pastAcceptanceRate: Double? = null,
    val responseTimeAvg: Int? = null,
    @SerializedName("avg_rating") val avgRating: Float = 0.0f,
    @SerializedName("sentiment_score") val sentimentScore: Float = 0.0f,
    @SerializedName("total_reviews") val totalReviews: Int = 0
)

data class NearbyPatient(
    val id: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val name: String? = null,
    @SerializedName("blood_group") val blood_group: String? = null,
    @SerializedName("hospital_name") val hospital_name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class LocationUpdate(
    @SerializedName("user_id") val userId: Int,
    val latitude: Double,
    val longitude: Double
)

data class RequestStatusUpdate(
    @SerializedName("request_id") val requestId: Int,
    val status: String
)

data class SendRequestToDonor(
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("donor_id") val donorId: Int,
    @SerializedName("blood_group") val bloodGroup: String,
    @SerializedName("units_needed") val unitsNeeded: Int,
    @SerializedName("urgency") val urgency: String,
    @SerializedName("message") val message: String
)

data class NearbyRequestsResponse(
    val message: String,
    val count: Int,
    val requests: List<NearbyRequest>
)

data class EmergencySearchRequest(
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("blood_group") val bloodGroup: String,
    val lat: Double,
    val lng: Double,
    @SerializedName("units_required") val unitsRequired: Int = 1,
    @SerializedName("radius_km") val radiusKm: Double = 5.0
)

data class RankedDonor(
    @SerializedName("donor_id") val donorId: Int,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("blood_group") val bloodGroup: String? = null,
    val age: Int? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("distance_km") val distanceKm: Double? = null,
    @SerializedName("blood_match_score") val bloodMatchScore: Double? = null,
    @SerializedName("is_available") val isAvailable: Int? = null,
    @SerializedName("is_eligible") val isEligible: Int? = null,
    @SerializedName("past_acceptance_rate") val pastAcceptanceRate: Double? = null,
    @SerializedName("response_time_avg") val responseTimeAvg: Int? = null,
    @SerializedName("ai_score") val aiScore: Double? = null,
    @SerializedName("avg_rating") val avgRating: Float = 0.0f,
    @SerializedName("sentiment_score") val sentimentScore: Float = 0.0f,
    @SerializedName("total_reviews") val totalReviews: Int = 0
)

data class EmergencySearchResponse(
    val message: String,
    @SerializedName("best_donor") val bestDonor: RankedDonor?,
    @SerializedName("nearby_donors") val nearbyDonors: List<RankedDonor>
)

data class DonorRatingRequest(
    @SerializedName("donor_id") val donorId: Int,
    @SerializedName("patient_id") val patientId: Int,
    val rating: Int,
    @SerializedName("review_text") val reviewText: String
)

data class DonorReview(
    val rating: Int = 0,
    @SerializedName("review_text") val reviewText: String? = null,
    @SerializedName("sentiment_score") val sentimentScore: Float = 0.0f,
    @SerializedName("created_at") val createdAt: String? = null
)

data class DonorReviewsResponse(
    @SerializedName("donor_id") val donorId: Int = 0,
    val reviews: List<DonorReview> = emptyList(),
    @SerializedName("average_rating") val averageRating: Float = 0.0f,
    @SerializedName("total_reviews") val totalReviews: Int = 0
)

