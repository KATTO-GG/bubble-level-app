package com.example.bubblelevel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.setContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawCircle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.toDegrees

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private var roll = 0f
    private var pitch = 0f
    private val rollState = mutableStateOf(0f)
    private val pitchState = mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BubbleLevelScreen(
                        roll = rollState.value,
                        pitch = pitchState.value
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.let { values ->
            val x = values[0]
            val y = values[1]
            val z = values[2]

            val newRoll = atan2(y, z).toDegrees()
            val newPitch = atan2(-x, sqrt(y * y + z * z)).toDegrees()

            rollState.value = newRoll
            pitchState.value = newPitch
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun BubbleLevelScreen(roll: Float, pitch: Float) {
    val bubbleX = remember { derivedStateOf { pitch } }
    val bubbleY = remember { derivedStateOf { roll } }

    val maxOffset = 120f
    val offsetX = (bubbleX.value / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)
    val offsetY = (bubbleY.value / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)

    val isLevel = abs(roll) < 2f && abs(pitch) < 2f

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.text.Text(
            text = if (isLevel) "LEVEL" else "Tilt: ${abs(roll).toInt()}° / ${abs(pitch).toInt()}°",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLevel) Color.Green else Color.White,
            textAlign = TextAlign.Center
        )

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(16.dp))

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(300.dp)) {
                val center = size.width / 2
                val radius = 130f

                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    center = androidx.compose.ui.geometry.Offset(center, center),
                    radius = radius
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    center = androidx.compose.ui.geometry.Offset(center, center),
                    radius = radius * 0.66f
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    center = androidx.compose.ui.geometry.Offset(center, center),
                    radius = radius * 0.33f
                )

                drawRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(center - 1, center - radius),
                    size = androidx.compose.ui.geometry.Size(2f, radius * 2)
                )
                drawRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(center - radius, center - 1),
                    size = androidx.compose.ui.geometry.Size(radius * 2, 2f)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        translationX = offsetX
                        translationY = offsetY
                    }
            ) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    drawCircle(
                        color = if (isLevel) Color.Green else Color.Red,
                        center = androidx.compose.ui.geometry.Offset(20f, 20f),
                        radius = 18f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        center = androidx.compose.ui.geometry.Offset(20f, 20f),
                        radius = 18f,
                        style = androidx.compose.ui.graphics.Stroke(width = 3f)
                    )
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.text.Text(
                text = "Roll: ${roll.toInt()}°",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            androidx.compose.foundation.text.Text(
                text = "Pitch: ${pitch.toInt()}°",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}