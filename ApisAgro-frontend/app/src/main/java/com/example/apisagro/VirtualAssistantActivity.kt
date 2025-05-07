package com.example.apisagro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apisagro.network.ChatRequest
import com.example.apisagro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VirtualAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VirtualAssistantScreen()
        }
    }

    @Composable
    fun VirtualAssistantScreen() {
        var userInput by remember { mutableStateOf("") }
        var chatMessages by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
        var loading by remember { mutableStateOf(false) }

        fun sendMessage(message: String) {
            loading = true
            chatMessages = chatMessages + (message to true)

            RetrofitClient.apiService.saveChat(ChatRequest(message, true))
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(
                        call: Call<Map<String, String>>,
                        response: Response<Map<String, String>>
                    ) {
                        loading = false
                        if (response.isSuccessful) {
                            val botText = response.body()?.get("message") ?: "No response from server"
                            chatMessages = chatMessages + (botText to false)
                        } else {
                            Toast
                                .makeText(
                                    this@VirtualAssistantActivity,
                                    "Server error: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        loading = false
                        Toast
                            .makeText(
                                this@VirtualAssistantActivity,
                                "Network error: ${t.localizedMessage}",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }

                })
        }

        fun fetchChatHistory() {
            loading = true
            val api = RetrofitClient.apiService
            api.getChats().enqueue(object : Callback<List<ChatRequest>> {
                override fun onResponse(call: Call<List<ChatRequest>>, response: Response<List<ChatRequest>>) {
                    loading = false
                    if (response.isSuccessful) {
                        response.body()?.let {
                            chatMessages = it.map { msg -> msg.message to msg.is_user }
                        }
                    }
                }

                override fun onFailure(call: Call<List<ChatRequest>>, t: Throwable) {
                    loading = false
                    Toast.makeText(this@VirtualAssistantActivity, "Error fetching chats", Toast.LENGTH_SHORT).show()
                }
            })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Virtual Assistant", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(chatMessages.reversed()) { (message, isUser) ->
                    ChatBubble(message, isUser)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Type your question...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            sendMessage(userInput)
                            userInput = ""
                        }
                    },
                    enabled = !loading
                ) {
                    Text("Send")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { fetchChatHistory() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("📖 Fetch Chat History")
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }


    }

    @Composable
    fun ChatBubble(message: String, isUser: Boolean) {
        Card(
            backgroundColor = if (isUser) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                message,
                modifier = Modifier.padding(16.dp),
                color = if (isUser) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
                fontSize = 16.sp
            )
        }
    }
}
