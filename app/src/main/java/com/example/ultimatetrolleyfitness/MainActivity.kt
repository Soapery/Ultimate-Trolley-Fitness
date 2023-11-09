package com.example.ultimatetrolleyfitness

import StepCounterHelper
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    private lateinit var stepTrackerPermissionManager: StepCounterHelper

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Commented out below for testing login form.
        // setContentView(R.layout.activity_main)

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    ScaffoldWithBottomNav(navController)
                }
            }
        }
    }
}

@Composable
fun ScaffoldWithBottomNav(navController: NavController) {
    BottomNavigationBar(navController)
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



