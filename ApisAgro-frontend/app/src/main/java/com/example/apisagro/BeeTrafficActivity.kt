package com.example.apisagro

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.apisagro.network.RetrofitClient
import com.example.apisagro.network.BeeTrafficRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BeeTrafficActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var userLocation: LatLng? = null

    // Define addMarkerToMap here, at class level
    private fun addMarkerToMap(location: LatLng, level: String) {
        val markerColor = when (level) {
            "Low Activity" -> BitmapDescriptorFactory.HUE_RED
            "Medium Activity" -> BitmapDescriptorFactory.HUE_ORANGE
            "High Activity" -> BitmapDescriptorFactory.HUE_GREEN
            else -> BitmapDescriptorFactory.HUE_AZURE
        }

        googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title(level)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
        )
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getUserLocation()
        } else {
            Toast.makeText(this, "Location permission is required to show your position", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check location permission
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getUserLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        setContent {
            BeeTrafficScreen(savedInstanceState)
        }
    }

    private fun getUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                    googleMap?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
                    )
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Simple data class to track bee activity reports that includes location data
    data class BeeActivityReport(
        val level: String,
        val latitude: Double,
        val longitude: Double
    )

    @Composable
    fun BeeTrafficScreen(savedInstanceState: Bundle?) {
        val context = LocalContext.current
        var activityReports by remember { mutableStateOf(listOf<BeeActivityReport>()) }
        var loading by remember { mutableStateOf(false) }
        var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
        val coroutineScope = rememberCoroutineScope()

        fun reportBeeTraffic(level: String) {
            val location = selectedLocation ?: userLocation
            if (location == null) {
                Toast.makeText(context, "Please select a location first", Toast.LENGTH_SHORT).show()
                return
            }

            loading = true
            val api = RetrofitClient.apiService
            // Use the network version of BeeTrafficRequest
            val request = BeeTrafficRequest(level)

            api.saveBeeTraffic(request).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    loading = false
                    if (response.isSuccessful) {
                        // Add to local list with location data
                        activityReports = activityReports + BeeActivityReport(
                            level = level,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        addMarkerToMap(location, level)
                        selectedLocation = null // Reset selection
                    } else {
                        Toast.makeText(context, "Failed to report!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    loading = false
                    Toast.makeText(context, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        fun fetchSavedBeeReports() {
            loading = true
            val api = RetrofitClient.apiService

            // Add debug logs
            //Log.d("BeeTraffic", "Fetching saved bee reports")

            api.getBeeTraffic().enqueue(object : Callback<List<BeeTrafficRequest>> {
                override fun onResponse(call: Call<List<BeeTrafficRequest>>, response: Response<List<BeeTrafficRequest>>) {
                    loading = false

                    // Log the response code
                   // Log.d("BeeTraffic", "Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val reports = response.body()
                       // Log.d("BeeTraffic", "Reports received: ${reports?.size ?: 0}")

                        if (reports != null) {
                            // Clear the map
                            googleMap?.clear()

                            // Process reports from API
                            // Use your existing activityReports to show on the map for now
                            activityReports.forEach { report ->
                                val position = LatLng(report.latitude, report.longitude)
                                addMarkerToMap(position, report.level)
                            }

                            Toast.makeText(context, "Reports refreshed", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No reports found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        //Log.e("BeeTraffic", "Error response: ${response.errorBody()?.string()}")
                        Toast.makeText(context, "Error fetching reports: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<BeeTrafficRequest>>, t: Throwable) {
                    loading = false
                   // Log.e("BeeTraffic", "Network error", t)
                    Toast.makeText(context, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bee Traffic Map", fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))

            // Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapView = this
                            this.onCreate(savedInstanceState)
                            this.getMapAsync { map ->
                                googleMap = map

                                // Configure map
                                map.uiSettings.isZoomControlsEnabled = true

                                // Set user location if available
                                userLocation?.let {
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                                }

                                // Set click listener to select location
                                map.setOnMapClickListener { latLng ->
                                    map.clear()
                                    // Redraw existing markers
                                    activityReports.forEach { report ->
                                        addMarkerToMap(
                                            LatLng(report.latitude, report.longitude),
                                            report.level
                                        )
                                    }
                                    // Add temporary selection marker
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(latLng)
                                            .title("Selected Location")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                    )
                                    selectedLocation = latLng
                                }
                            }
                        }
                    },
                    update = { view ->
                        mapView = view
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Map overlay instructions
                if (selectedLocation == null) {
                    Text(
                        "Tap on the map to select a location",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selection info
            selectedLocation?.let {
                Text(
                    "Selected: Lat: ${it.latitude.format(4)}, Lng: ${it.longitude.format(4)}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Text("Report Bee Activity", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { reportBeeTraffic("Low Activity") },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    enabled = !loading && selectedLocation != null
                ) {
                    Text("Low")
                }
                Button(
                    onClick = { reportBeeTraffic("Medium Activity") },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA726)),
                    enabled = !loading && selectedLocation != null
                ) {
                    Text("Medium")
                }
                Button(
                    onClick = { reportBeeTraffic("High Activity") },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF66BB6A)),
                    enabled = !loading && selectedLocation != null
                ) {
                    Text("High")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { fetchSavedBeeReports() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("📖 Fetch Saved Reports")
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

            Text("Recent Reports", fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(activityReports) { report ->
                    val backgroundColor = when (report.level) {
                        "Low Activity" -> Color.Red.copy(alpha = 0.3f)
                        "Medium Activity" -> Color(0xFFFFA726).copy(alpha = 0.3f)
                        "High Activity" -> Color(0xFF66BB6A).copy(alpha = 0.3f)
                        else -> Color.LightGray
                    }

                    Card(
                        elevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        backgroundColor = backgroundColor
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                report.level,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                "Location: ${report.latitude.format(4)}, ${report.longitude.format(4)}",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
}

// Extension function to format Double to specific decimal places
fun Double.format(digits: Int) = "%.${digits}f".format(this)