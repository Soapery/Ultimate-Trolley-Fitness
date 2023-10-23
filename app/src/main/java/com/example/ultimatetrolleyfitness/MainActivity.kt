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

    private var magnitudePreviousStep = 0.0
    private val ACTIVITY_RECOGNITION_REQUEST_CODE: Int = 100
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

        if(isPermissionGranted()){
            requestPermission()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

//        setContent {
//            UltimateTrolleyFitnessTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }
    }

    // Asks user for sensor data permission
    private fun requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE)
        }
    }

    // Checks if the user granted sensor data permission
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            ACTIVITY_RECOGNITION_REQUEST_CODE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    // Permission granted
                }
            }
        }
    }

    override fun onResume(){
        super.onResume()
        running = true

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
                Toast.makeText(this, "No compatible sensor detected on this device", Toast.LENGTH_SHORT).show()
            }
        }
//        if(stepSensor == null) {
//            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
//        } else {
//            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
//        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        val progress_circular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.progress_circular)

        // If the target device has an Accelerometer
        if(event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xAccel: Float = event.values[0]
            val yAccel: Float = event.values[1]
            val zAccel: Float = event.values[2]
            val magnitude: Double = sqrt((xAccel * xAccel + yAccel * yAccel + zAccel * zAccel).toDouble())

            val magnitudeDelta: Double = magnitude - magnitudePreviousStep
            magnitudePreviousStep = magnitude

            if(magnitudeDelta > 6) {
                totalSteps ++
            }

            val step: Int = totalSteps.toInt()
            tv_stepsTaken.text = step.toString()

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

    fun resetSteps(){
        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        tv_stepsTaken.setOnClickListener {
        Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        tv_stepsTaken.setOnLongClickListener{
            previousTotalSteps = totalSteps
            tv_stepsTaken.text = 0.toString()
            saveData()

            true
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData(){
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
//        Log.d("MainActivity", "$savedNumber")
        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}

//<TextView
//    android:layout_width="wrap_content"
//    android:layout_height="wrap_content"
//    android:text="Step Counter"
//    android:layout_margin="20dp"
//    android:textSize= "20sp"
//    android:textFontWeight="800"
//    android:textColor= "#3a9bdc"
//
///>
//
//<ProgressBar
//    android:layout_width= "300dp"
//    android:layout_height= "300dp"
//    android:layout_centerVertical= "true"
//    android:layout_centerHorizontal= "true"
//
///>

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