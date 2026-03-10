package com.simats.lifeflow

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("signup")
    fun signup(@Body data: Map<String, String>): Call<Map<String, String>>

    @POST("login")
    fun login(@Body data: Map<String, String>): Call<LoginResponse>

    @POST("send-otp")
    fun sendOtp(@Body data: Map<String, String>): Call<Map<String, String>>

    @POST("verify-otp")
    fun verifyOtp(@Body data: Map<String, String>): Call<Map<String, String>>

    @PUT("reset-password")
    fun resetPassword(@Body data: Map<String, String>): Call<Map<String, String>>

    @PUT("donor/profile/{user_id}")
    fun updateDonorProfile(@Path("user_id") userId: Int, @Body data: DonorProfile): Call<Map<String, String>>

    @PUT("donor/availability/{user_id}")
    fun toggleAvailability(@Path("user_id") userId: Int, @Body data: Map<String, Boolean>): Call<Map<String, String>>

    @GET("donor/availability/{user_id}")
    fun getDonorAvailability(@Path("user_id") userId: Int): Call<Map<String, Any>>

    @PUT("patient/profile/{user_id}")
    fun updatePatientProfile(@Path("user_id") userId: Int, @Body data: PatientProfile): Call<Map<String, String>>

    @POST("patient/request/{user_id}")
    fun createRequest(@Path("user_id") userId: Int, @Body data: BloodRequest): Call<Map<String, String>>

    @GET("patient/requests/{user_id}")
    fun viewRequests(@Path("user_id") userId: Int): Call<List<PatientRequest>>

    @PUT("admin/approve/{request_id}")
    fun approveRequest(@Path("request_id") requestId: Int): Call<Map<String, String>>

    @PUT("admin/reject/{request_id}")
    fun rejectRequest(@Path("request_id") requestId: Int): Call<Map<String, String>>

    @POST("chat/send")
    fun sendMessage(@Body data: ChatMessage): Call<Map<String, String>>

    @GET("chat/history")
    fun getChatHistory(@Query("user1") user1: Int, @Query("user2") user2: Int): Call<List<ChatMessage>>

    @POST("forgot-password")
    fun forgotPassword(@Body data: Map<String, String>): Call<Map<String, String>>

    @GET("chat/inbox")
    fun getChatInbox(@Query("user_id") userId: Int): Call<List<InboxItem>>

    @POST("chat/mark_read")
    fun markAsRead(@Body data: Map<String, Int>): Call<Map<String, Any>>

    @GET("users/donors")
    fun getDonors(): Call<List<User>>

    @GET("users/patients")
    fun getPatients(): Call<List<User>>

    @GET("donor/donations/history")
    fun getDonationHistory(
        @Query("donor_id") userId: Int
    ): Call<DonationHistoryResponse>

    @POST("donor/donations/add")
    fun addDonation(@Body donation: Donation): Call<Map<String, Any>>

    @GET("donors/nearby")
    fun getNearbyDonors(
        @Query("blood_group") bloodGroup: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_km") radiusKm: Double = 5.0
    ): Call<List<NearbyDonor>>

    @POST("patient/send_request")
    fun sendRequestToDonor(@Body data: Map<String, Any>): Call<Map<String, String>>
}
