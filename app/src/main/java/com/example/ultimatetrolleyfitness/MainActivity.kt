package com.example.ultimatetrolleyfitness

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ultimatetrolleyfitness.ui.theme.UltimateTrolleyFitnessTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var magnitudePreviousStep = 0.0
    private var sensorManager: SensorManager? = null

    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()
        resetSteps()
        setupPermissionLauncher()

        // Check if the 'ACTIVITY_RECOGNITION' permission is already granted.
        if(isPermissionGranted()){
            // If granted, launch the permission request for the 'ACTIVITY_RECOGNITION' permission.
            // If not granted, the permission request will be triggered when necessary.
            requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


    }

    // Checks if the user granted sensor data permission
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
    }

    // Sets up the permission modal
    private fun setupPermissionLauncher() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                // Empty for future handling of each result
                if (isGranted) {
                    // Permission granted
                } else {
                    // Permission denied
                }
            }
    }


    // Resume sensor data collection when the activity is in the foreground.
    override fun onResume(){
        super.onResume()

        // Set the 'running' flag to true, indicating that the app is actively monitoring sensor data.
        running = true

        // Get a reference to the system's sensor service.
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Attempt to obtain references to specific sensor types: step counter, step detector, and accelerometer.
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Determine the sensor available on the device and register a listener accordingly.
        when{
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
                // Display a message if no compatible sensor is detected on the device.
                Toast.makeText(this, "No compatible sensor detected on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Pause the sensor data updates when the activity goes into the background
    override fun onPause() {
        super.onPause()

        // Unregister the sensor event listener to conserve resources when activity is not in the foreground
        // Will still track steps while application is not open.
        sensorManager?.unregisterListener(this)
    }

    // Handle changes in sensor data, updating the step count display and circular progress bar/
    override fun onSensorChanged(event: SensorEvent?) {
        // Find the TextView element tv_stepsTaken (Main Step Count Number)
        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        // Find the circular progress bar element
        val progress_circular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.progress_circular)

        // If the target device has an Accelerometer
        if(event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Extract accelerometer data for each axis
            val xAccel: Float = event.values[0]
            val yAccel: Float = event.values[1]
            val zAccel: Float = event.values[2]
            // Calculate the magnitude (Darian Mega Brain Moment)
            val magnitude: Double = sqrt((xAccel * xAccel + yAccel * yAccel + zAccel * zAccel).toDouble())

            val magnitudeDelta: Double = magnitude - magnitudePreviousStep
            magnitudePreviousStep = magnitude

            if(magnitudeDelta > 6) {
                totalSteps ++
            }

            // Convert the total step counter to an integer for display
            val step: Int = totalSteps.toInt()

            // Update the step count TextView with the current step count
            tv_stepsTaken.text = step.toString()

            // Animate the Circular Progress Bar to reflect the current step count
            progress_circular.apply{
                setProgressWithAnimation(step.toFloat())
            }
        // If the target device has a standard step counter
        } else {
            if(running){
                totalSteps = event.values[0]
                val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                tv_stepsTaken.text = currentSteps.toString()

                progress_circular.apply{
                    setProgressWithAnimation(currentSteps.toFloat())
                }
            }
        }
    }

    // Function for resetting the step counter
    private fun resetSteps(){
        // Find the textview element tv_stepsTaken. (Main Step Counter Number)
        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        tv_stepsTaken.setOnClickListener {
        Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        // Attach the long click listener to the step count
        tv_stepsTaken.setOnLongClickListener{
            previousTotalSteps = totalSteps
            // Update the steps taken to 0 upon long click and save the data
            tv_stepsTaken.text = 0.toString()
            saveData()

            true
        }
    }


    // Save the previous total step count
    // Shared preferences are used to store simple data persistently
    private fun saveData() {
        // Access shared preferences and set to private
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        // Create an editor to modify the shared preferences
        val editor = sharedPreferences.edit()

        // Store the previous total steps using key1 and save changes
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData(){
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        previousTotalSteps = savedNumber
    }

    // Handle changes in sensor accuracy if necessary in the future. Probably not needed
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UltimateTrolleyFitnessTheme {
        Greeting("Android")
    }
}