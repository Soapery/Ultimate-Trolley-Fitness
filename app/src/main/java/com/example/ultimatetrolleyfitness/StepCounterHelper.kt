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
import com.example.ultimatetrolleyfitness.R
import kotlin.math.sqrt

/**
 * Step Counter Helper, provides logic for the step tracker when implemented in the main activity.
 */
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
        val progress_circular =
            activity.findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(
                R.id.progress_circular
            )

        if (tv_stepsTaken != null && progress_circular != null) {
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

                progress_circular.apply {
                    setProgressWithAnimation(step.toFloat())
                }
                // If the target device has a standard step counter
            } else {
                if (running) {
                    totalSteps = event?.values?.get(0) ?: 0f
                    val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                    tv_stepsTaken.text = currentSteps.toString()

                    progress_circular.apply {
                        setProgressWithAnimation(currentSteps.toFloat())
                    }
                }
            }
        }
    }

    private fun resetSteps() {
        val tv_stepsTaken = activity.findViewById<TextView>(R.id.tv_stepsTaken)
        tv_stepsTaken.setOnClickListener {
            Toast.makeText(activity, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        tv_stepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            tv_stepsTaken.text = 0.toString()
            saveData()

            true
        }
    }

    private fun saveData() {
        val sharedPreferences = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}


