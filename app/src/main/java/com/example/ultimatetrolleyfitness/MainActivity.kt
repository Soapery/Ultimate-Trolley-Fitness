package com.example.ultimatetrolleyfitness

import StepCounterHelper
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
        setContentView(R.layout.activity_main)

        setContent {
            // Set up your navigation controller
            val navController = rememberNavController()

            // Set up your navigation host with destinations
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    // Your main content goes here
                    BottomNav(navController = navController) {
                        HomeScreen()
                    }
                }
                composable("home") {
                    BottomNav(navController = navController) {
                        HomeScreen()
                    }
                }
                composable("nutrition") {
                    BottomNav(navController = navController) {
                        NutritionScreen()
                    }
                }
                composable("workout") {
                    BottomNav(navController = navController) {
                        WorkoutScreen()
                    }
                }
                // Add more composable functions for other destinations as needed
            }


        }
    }

}

@Composable
fun BottomNav(navController: NavController, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Content takes remaining space
        Box(
            modifier = Modifier.weight(1f)
        ) {
            content()
        }

        // Bottom navigation bar
        BottomNavigationBar(navController = navController)
    }
}

@Composable
fun HomeScreen(){
    Text("Welcome to the Home Screen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen() {
    val context = LocalContext.current
    val searchText = remember { mutableStateOf("") }
    val csvData = remember { NutritionData.getCSVData() }
    var filteredData by remember { mutableStateOf(csvData) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                label = { Text("Search") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search // Set the action for the TextField
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // Filter data based on search text when Enter key is pressed
                        filteredData = csvData.filter { row ->
                            row.getOrNull(0)?.contains(searchText.value, ignoreCase = true) == true
                        }
                    }
                )
            )

            Button(
                onClick = {
                    // Filter data based on search text when the button is clicked
                    filteredData = csvData.filter { row ->
                        row.getOrNull(0)?.contains(searchText.value, ignoreCase = true) == true
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Search")
            }
        }

        // Display filtered data based on search text
        filteredData.forEach { row ->
            Text(row.getOrNull(0) ?: "Name not found")
            // Display the Name attribute of each filtered row in a Text composable
            // If Name is not present in the row, display a default message
        }
    }
}

@Composable
fun WorkoutScreen(){
    Text("This is the Workout Screen")
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



