package com.example.apisagro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen()
        }
    }

    @Composable
    fun HomeScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to", fontSize = 20.sp)
            Text("ApisAgro", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            Text("Sustainable Farming Solutions", fontSize = 16.sp, modifier = Modifier.padding(bottom = 24.dp))

            DashboardCard(
                title = "Crop Rotation Planner",
                description = "Generate scientifically validated crop rotation plans...",
                icon = Icons.Filled.Agriculture,
                color = Color(0xFF4CAF50)
            ) { startActivity(Intent(this@MainActivity, CropRotationActivity::class.java)) }

            DashboardCard(
                title = "Bee Sound Synthesizer",
                description = "Play AI-generated, bee-friendly frequencies...",
                icon = Icons.Filled.GraphicEq,
                color = Color(0xFFFFA726)
            ) { startActivity(Intent(this@MainActivity, BeeSoundActivity::class.java)) }

            DashboardCard(
                title = "Bee Traffic Map",
                description = "Report and view local bee activity levels...",
                icon = Icons.Filled.Map,
                color = Color(0xFF42A5F5)
            ) { startActivity(Intent(this@MainActivity, BeeTrafficActivity::class.java)) }

            DashboardCard(
                title = "Virtual Assistant",
                description = "Get real-time guidance and answers to your farming queries...",
                icon = Icons.Filled.SmartToy,
                color = Color(0xFF7E57C2)
            ) { startActivity(Intent(this@MainActivity, VirtualAssistantActivity::class.java)) }
        }
    }

    @Composable
    fun DashboardCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .background(color, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = description, fontSize = 14.sp, color = Color.Gray, maxLines = 2)
                }
            }
        }
    }
}
