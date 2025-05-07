package com.example.apisagro

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BeeSoundActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeeSoundScreen()
        }
    }

    @Composable
    fun BeeSoundScreen() {
        var frequency by remember { mutableFloatStateOf(200f) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Bee Sound Synthesizer", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

            Text("${frequency.toInt()} Hz", fontSize = 32.sp, color = MaterialTheme.colors.primary)
            Text("Bee-Friendly Frequency", fontSize = 16.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(24.dp))

            Slider(
                value = frequency,
                onValueChange = { frequency = it },
                valueRange = 150f..250f,
                steps = 100,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { playTone(frequency.toInt()) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
            ) {
                Text("▶️ Play Sound")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How it works:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Bees are attracted to specific sound frequencies that mimic their wing beats. Adjust the slider to play bee-friendly tones optimized for crop pollination.",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    private fun playTone(frequency: Int) {
        val duration = 2 // seconds
        val sampleRate = 44100
        val numSamples = duration * sampleRate
        val samples = DoubleArray(numSamples)
        val buffer = ShortArray(numSamples)

        for (i in samples.indices) {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequency))
            buffer[i] = (samples[i] * Short.MAX_VALUE).toInt().toShort()
        }

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffer.size * 2,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
    }
}
