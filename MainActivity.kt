package com.example.thick_of_it

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thick_of_it.ui.theme.Thick_Of_ItTheme
import kotlin.math.pow

// Kalman Filter Class
class KalmanFilter(private var q: Float, private var r: Float, private var initialEstimate: Float = 0f) {
    private var estimate: Float = initialEstimate
    private var errorCovariance: Float = 1f

    fun filter(measurement: Float): Float {
        // Prediction step
        errorCovariance += q

        // Update step
        val kalmanGain = errorCovariance / (errorCovariance + r)
        estimate += kalmanGain * (measurement - estimate)
        errorCovariance *= (1 - kalmanGain)

        return estimate
    }
}

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Kalman Filters for each axis
    private val kalmanX = KalmanFilter(q = 0.0005f, r = 0.05f)
    private val kalmanY = KalmanFilter(q = 0.0005f, r = 0.05f)
    private val kalmanZ = KalmanFilter(q = 0.0005f, r = 0.05f)

    // Mutable states for accelerometer data
    private var _accelerometerData = mutableStateOf(Triple(0f, 0f, 0f))
    val accelerometerData: State<Triple<Float, Float, Float>> get() = _accelerometerData

    // Mutable state for activity recognition
    private var _activityState = mutableStateOf("Standing Still")
    val activityState: State<String> get() = _activityState

    // Lists to store accelerometer values for variance calculation
    private val windowSize = 50 // Number of samples for a 1-second window
    private val accelX = mutableListOf<Float>()
    private val accelY = mutableListOf<Float>()
    private val accelZ = mutableListOf<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SensorManager and Accelerometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        enableEdgeToEdge()
        setContent {
            Thick_Of_ItTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        AccelerometerDisplay(accelerometerData = accelerometerData.value)
                        ActivityDisplay(activity = activityState.value)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val rawX = event.values[0]
            val rawY = event.values[1]
            val rawZ = event.values[2]

            val filteredX = kalmanX.filter(rawX)
            val filteredY = kalmanY.filter(rawY)
            val filteredZ = kalmanZ.filter(rawZ)

            _accelerometerData.value = Triple(filteredX, filteredY, filteredZ)

            if (accelX.size >= windowSize) {
                accelX.removeAt(0)
                accelY.removeAt(0)
                accelZ.removeAt(0)
            }
            accelX.add(filteredX)
            accelY.add(filteredY)
            accelZ.add(filteredZ)

            if (accelX.size == windowSize) {
                val varX = calculateVariance(accelX)
                val varY = calculateVariance(accelY)
                val varZ = calculateVariance(accelZ)
                val totalVariance = varX + varY + varZ
                val meanZ = accelZ.average().toFloat()

                _activityState.value = when {
                    totalVariance < 0.5 -> "Standing Still"
                    totalVariance in 0.5..2.0 -> when {
                        meanZ > 10.5 -> "Climbing Up"
                        meanZ < 9.0 -> "Climbing Down"
                        else -> "Walking"
                    }
                    else -> "Running"
                }
            }
        }
    }

    private fun calculateVariance(values: List<Float>): Float {
        val mean = values.average().toFloat()
        return values.sumOf { (it - mean).pow(2).toDouble() }.toFloat() / values.size
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun AccelerometerDisplay(accelerometerData: Triple<Float, Float, Float>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Smoothed Accelerometer Data:", modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "X-axis: ${"%.2f".format(accelerometerData.first)}")
        Text(text = "Y-axis: ${"%.2f".format(accelerometerData.second)}")
        Text(text = "Z-axis: ${"%.2f".format(accelerometerData.third)}")
    }
}

@Composable
fun ActivityDisplay(activity: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Current Activity:", modifier = Modifier.padding(bottom = 8.dp))
        Text(text = activity, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun AccelerometerPreview() {
    Thick_Of_ItTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AccelerometerDisplay(Triple(0f, 0f, 0f))
            ActivityDisplay("Standing Still")
        }
    }
}