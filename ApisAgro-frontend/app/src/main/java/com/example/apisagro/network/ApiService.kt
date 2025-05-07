package com.example.apisagro.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Data classes
data class BeeTrafficRequest(
    val level: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)



// API interface
interface ApiService {

    @POST("bee-traffic")
    fun saveBeeTraffic(@Body request: BeeTrafficRequest): Call<Map<String, String>>

    @GET("bee-traffic")
    fun getBeeTraffic(): Call<List<BeeTrafficRequest>>

}
