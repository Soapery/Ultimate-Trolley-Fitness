package com.example.ultimatetrolleyfitness

import StepCounterHelper
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.ultimatetrolleyfitness.ui.theme.UltimateTrolleyFitnessTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    private lateinit var stepTrackerPermissionManager: StepCounterHelper

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Commented out below for testing login form.
        setContentView(R.layout.activity_main)

        setContent {
            // Set up your navigation controller
            val navController = rememberNavController()

            // Set up your navigation host with destinations
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    // Your main content goes here
                    ScaffoldWithBottomNav(navController)
                }
                // Add more composable functions for other destinations as needed
            }
        }
    }
}

@Composable
fun ScaffoldWithBottomNav(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Put some content here, composable functions hopefully??
        Text(text = "Hello, Compose!", modifier = Modifier.weight(1f))

        // Bottom navigation bar
        BottomNavigationBar(navController = navController)
    }
}





//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    UltimateTrolleyFitnessTheme {
//        Greeting("Android")
//    }
//}

// Initialize StepTrackerPermissionManager
//        stepTrackerPermissionManager = StepCounterHelper(this) { stepCount ->
//            // Update UI or perform actions based on step count
//            updateUI(stepCount)
//        }

// Update UI based on the step count
//    private fun updateUI(stepCount: Int) {
//        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
//        val progress_circular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.progress_circular)
//
//        // Update UI elements based on step count
//        tv_stepsTaken.text = stepCount.toString()
//        progress_circular.setProgressWithAnimation(stepCount.toFloat())
//    }
//}



