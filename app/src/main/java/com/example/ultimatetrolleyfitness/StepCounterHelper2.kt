package com.example.ultimatetrolleyfitness

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.lang.Math.sqrt

@RequiresApi(Build.VERSION_CODES.Q)
class StepCounterHelper(
    private val activity: ComponentActivity,
    private val onStepCountChangeListener: (Int) -> Unit
) : SensorEventListener {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var magnitudePreviousStep = 0.0
    private var sensorManager: SensorManager? = null

    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    init {
        setupPermissionLauncher()
        if (isPermissionGranted()) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
        }
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        onResume()
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Permission granted
                } else {
                    // Permission denied
                }
            }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun onResume() {
        running = true

        val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        when {
            stepSensor != null -> {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            }

            detectorSensor != null -> {
                sensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_UI)
            }

            accelerometer != null -> {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }

            else -> {
                Toast.makeText(
                    activity,
                    "No compatible sensor detected on this device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun onPause() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val tv_stepsTaken = activity.findViewById<TextView>(R.id.tv_stepsTaken)

        if (tv_stepsTaken != null) {
            // If the target device has an Accelerometer
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val xAccel: Float = event.values[0]
                val yAccel: Float = event.values[1]
                val zAccel: Float = event.values[2]
                val magnitude: Double =
                    sqrt((xAccel * xAccel + yAccel * yAccel + zAccel * zAccel).toDouble())

                val magnitudeDelta: Double = magnitude - magnitudePreviousStep
                magnitudePreviousStep = magnitude

                if (magnitudeDelta > 6) {
                    totalSteps++
                }

                val step: Int = totalSteps.toInt()
                tv_stepsTaken.text = step.toString()

                // If the target device has a standard step counter
            } else {
                if (running) {
                    totalSteps = event?.values?.get(0) ?: 0f
                    val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                    tv_stepsTaken.text = currentSteps.toString()
                }
            }
        }
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}