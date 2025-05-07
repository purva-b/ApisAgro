package com.example.apisagro.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Data classes
data class CropRotationRequest(
    val crop: String,
    val soil: String,
    val duration: String,
    val plan: String
)

data class BeeTrafficRequest(
    val level: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class ChatRequest(
    val message: String,
    val is_user: Boolean
)

data class ApiResponse<T>(
    val message: String,
    val data: T?
)


// API interface
interface ApiService {

    @POST("bee-traffic")
    fun saveBeeTraffic(@Body request: BeeTrafficRequest): Call<Map<String, String>>

    @GET("bee-traffic")
    fun getBeeTraffic(): Call<List<BeeTrafficRequest>>

    @POST("crop-rotation")
    fun saveCropRotation(@Body request: CropRotationRequest): Call<ApiResponse<CropRotationRequest>>

    @GET("crop-rotation")
    fun getCropRotations(): Call<List<CropRotationRequest>>

    @POST("chat")
    fun saveChat(@Body request: ChatRequest): Call<Map<String, String>>

    @GET("chat")
    fun getChats(): Call<List<ChatRequest>>
}
