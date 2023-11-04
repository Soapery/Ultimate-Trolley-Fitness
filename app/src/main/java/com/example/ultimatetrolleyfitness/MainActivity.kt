package com.example.ultimatetrolleyfitness

import StepCounterHelper
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ultimatetrolleyfitness.ui.theme.UltimateTrolleyFitnessTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var stepTrackerPermissionManager: StepCounterHelper

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Commented out below for testing login form.
        setContentView(R.layout.activity_main)

        // Initialize StepTrackerPermissionManager
        stepTrackerPermissionManager = StepCounterHelper(this) { stepCount ->
            // Update UI or perform actions based on step count
            updateUI(stepCount)
        }
    }

    // Update UI based on the step count
    private fun updateUI(stepCount: Int) {
        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        val progress_circular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.progress_circular)

        // Update UI elements based on step count
        tv_stepsTaken.text = stepCount.toString()
        progress_circular.setProgressWithAnimation(stepCount.toFloat())
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