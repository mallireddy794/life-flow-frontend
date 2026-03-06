package com.simats.lifeflow

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val phone: String,
    val blood_group: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val user: UserData? = null
)

data class UserData(
    val id: Int,
    val full_name: String,
    val email: String,
    val blood_group: String
)
