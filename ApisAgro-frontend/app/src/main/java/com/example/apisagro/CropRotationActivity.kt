package com.example.apisagro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apisagro.network.CropRotationRequest
import com.example.apisagro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.apisagro.network.ApiResponse

class CropRotationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CropRotationScreen()
        }
    }

    @Composable
    fun CropRotationScreen() {
        var crop by remember { mutableStateOf("") }
        var soil by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("") }
        var plan by remember { mutableStateOf("") }
        var savedPlans by remember { mutableStateOf(listOf<CropRotationRequest>()) }
        var loading by remember { mutableStateOf(false) }

        val savePlan: (String, String, String, String) -> Unit = { c, s, d, p ->
            loading = true
            val req = CropRotationRequest(c, s, d, p)
            RetrofitClient.apiService.saveCropRotation(req).enqueue(object : Callback<ApiResponse<CropRotationRequest>> {
                override fun onResponse( call: Call<ApiResponse<CropRotationRequest>>, resp: Response<ApiResponse<CropRotationRequest>> ) {
                    loading = false
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        if (body?.message == "Crop rotation plan saved successfully." && body.data != null) {
                            savedPlans = listOf(body.data)
                        } else {
                            Toast.makeText(this@CropRotationActivity, "Save failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFailure(call: Call<ApiResponse<CropRotationRequest>>, t: Throwable) {
                    loading = false
                }
            })
        }

        val fetchSavedPlans: () -> Unit = {
            loading = true
            RetrofitClient.apiService.getCropRotations().enqueue(object : Callback<List<CropRotationRequest>> {
                override fun onResponse( call: Call<List<CropRotationRequest>>, resp: Response<List<CropRotationRequest>> ) {
                    loading = false
                    if (resp.isSuccessful) {
                        savedPlans = resp.body() ?: emptyList()
                    }
                }
                override fun onFailure(call: Call<List<CropRotationRequest>>, t: Throwable) {
                    loading = false
                }
            })
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Crop Rotation Planner", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

            OutlinedTextField(
                value = crop,
                onValueChange = { crop = it },
                label = { Text("Current Crop") },
                placeholder = { Text("e.g., Corn") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = soil,
                onValueChange = { soil = it },
                label = { Text("Soil Type") },
                placeholder = { Text("e.g., Clay Loam") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Cultivation Duration (months)") },
                placeholder = { Text("e.g., 4") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    savePlan(crop, soil, duration, plan)
                    crop=""; soil=""; duration=""; plan = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("🔄 Generate Plan")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { fetchSavedPlans() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("📖 Fetch Saved Plans")
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }

            LazyColumn {
                items(savedPlans) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Crop: ${item.crop}")
                            Text("Soil: ${item.soil}")
                            Text("Duration: ${item.duration} months")
                            Text("Plan: ${item.plan}")
                        }
                    }
                }
            }
        }
    }
}
