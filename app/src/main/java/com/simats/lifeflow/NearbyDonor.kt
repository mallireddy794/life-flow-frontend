package com.simats.lifeflow

data class NearbyDonor(
    val donor_user_id: Int,
    val name: String,
    val phone: String?,
    val blood_group: String,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?,
    val distance_km: Double?
)
