package com.example.ultimatetrolleyfitness

import NutritionData
import StepCounterHelper
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ultimatetrolleyfitness.ui.theme.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : ComponentActivity() {
    private var apiData by mutableStateOf<List<Exercise>?>(null)
    private lateinit var stepTrackerPermissionManager: StepCounterHelper
    lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        fetchDataFromApi("", "", "", "")

        // Commented out below for testing login form.
        setContentView(R.layout.activity_main)

        // Confirmation of user authentication
        auth.currentUser?.email?.let { Log.i("User:", it) }

        setContent {
            // Set up your navigation controller
            val navController = rememberNavController()

            // Read CSV data before setting content
            NutritionData.readNutrientsCSV(this@MainActivity)

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
                        NutritionScreen(navController = navController)
                    }
                }
                composable("workout") {
                    BottomNav(navController = navController) {
                        WorkoutScreen(apiData) { name, type, muscle, difficulty ->
                            fetchDataFromApi(name, type, muscle, difficulty)
                        }
                    }
                }
                composable("foodDetail/{foodName}") { backStackEntry ->
                    val foodName = backStackEntry.arguments?.getString("foodName")
                    val foodItem = NutritionData.getCSVData().firstOrNull { it[0] == foodName }

                    if (foodItem != null) {
                        BottomNav(navController = navController) {
                            FoodDetailScreen(foodItem)
                        }
                    } else {
                        // Handle case when food item is not found
                        Text("Food item not found")
                    }
                }
            }
        }
    }

    private fun fetchDataFromApi(name: String, type: String, muscle: String, difficulty: String) {
        val apiService = RetrofitInstance.retrofit.create(myAPI::class.java)

        val call = apiService.getExercises(name, type, muscle, difficulty)

        // Asynchronous callback for a successful API response
        call.enqueue(object : Callback<List<Exercise>> {
            override fun onResponse(
                call: Call<List<Exercise>>,
                response: Response<List<Exercise>>
            ) {
                if (response.isSuccessful) {
                    // Extract the response body (products data) from the API response
                    val myData = response.body()

                    // Update the jsonData state variable with the fetched data
                    apiData = myData
                    Log.i("Success", "Data was received")
                } else {
                    // Assign error to exercises for feedback to user
                    Log.e("Failure", response.toString())
                }
            }

            override fun onFailure(call: Call<List<Exercise>>, t: Throwable) {
                // Handle network error
                t.message?.let { Log.e("Failure", it) }
            }
        })
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
fun HomeScreen() {
    Text("Welcome to the Home Screen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(navController: NavController) {
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
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        filteredData = csvData.filter { row ->
                            row.getOrNull(0)?.contains(searchText.value, ignoreCase = true) == true
                        }
                    },
                    onDone = {
                        // You can handle the Enter key press action here if needed
                        // For example, triggering the search action similarly to onSearch
                        filteredData = csvData.filter { row ->
                            row.getOrNull(0)?.contains(searchText.value, ignoreCase = true) == true
                        }
                    }
                )
            )

            Button(
                onClick = {
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
            ClickableFoodItem(row, navController)
        }
    }
}

@Composable
fun ClickableFoodItem(foodItem: Array<String>, navController: NavController) {
    Box(
        modifier = Modifier
            .clickable {
                navController.navigate("foodDetail/${foodItem[0]}") // Navigate with the food name as a parameter
            }
            .padding(8.dp)
    ) {
        Text(text = foodItem[0]) // Display the name of the food item
    }
}

@Composable
fun WorkoutScreen(
    apiData: List<Exercise>?,
    fetchDataFromApi: (String, String, String, String) -> Unit
) {
    var exerciseName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("") }

    Column {
        WorkoutSearchBar(
            exerciseName = exerciseName,
            onExerciseNameChange = { exerciseName = it },
            selectedType = selectedType,
            onSelectedTypeChange = { selectedType = it },
            selectedMuscle = selectedMuscle,
            onSelectedMuscleChange = { selectedMuscle = it },
            selectedDifficulty = selectedDifficulty,
            onSelectedDifficultyChange = { selectedDifficulty = it },
            onSearch = { searchExerciseName, searchType, searchMuscle, searchDifficulty ->
                fetchDataFromApi(searchExerciseName, searchType, searchMuscle, searchDifficulty)
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        DisplayJsonData(apiData)
    }

}

@Composable
fun DisplayJsonData(data: List<Exercise>?) {
    LazyColumn {
        items(data ?: emptyList()) { exercise ->
            if (exercise.name.isNotEmpty()) {
                val name = exercise.name
                val type = exercise.type
                val difficulty = exercise.difficulty

                Text(
                    text = "$name: $type. Difficulty: $difficulty.",
                    modifier = Modifier.padding(16.dp) // Adjust padding as needed
                )
            } else {
                Text(text = "No products were found.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSearchBar(
    exerciseName: String,
    onExerciseNameChange: (String) -> Unit,
    selectedType: String,
    onSelectedTypeChange: (String) -> Unit,
    selectedMuscle: String,
    onSelectedMuscleChange: (String) -> Unit,
    selectedDifficulty: String,
    onSelectedDifficultyChange: (String) -> Unit,
    onSearch: (String, String, String, String) -> Unit
) {
    var typeExpanded by rememberSaveable { mutableStateOf(false) }
    var muscleExpanded by rememberSaveable { mutableStateOf(false) }
    var difficultyExpanded by rememberSaveable { mutableStateOf(false) }

    var typeItemHeight by remember { mutableStateOf(0.dp) }
    var muscleItemHeight by remember { mutableStateOf(0.dp) }
    var difficultyItemHeight by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    val types = listOf(
        "cardio", "olympic_weightlifting", "plyometrics",
        "powerlifting", "strength", "stretching", "strongman"
    )

    val muscles = listOf(
        "abdominals", "abductors", "adductors", "biceps", "calves",
        "chest", "forearms", "glutes", "hamstrings", "lats", "lower_back",
        "middle_back", "neck", "quadriceps", "traps", "triceps"
    )

    val difficulties = listOf(
        "beginner", "intermediate", "expert"
    )

    Card(
        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
        colors = CardDefaults.cardColors(),
        modifier = Modifier
            .fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = exerciseName,
                onValueChange = { onExerciseNameChange(it) },
                label = { Text("Exercise Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
            ) {
                TextField(
                    value = ( if (selectedType !== "") selectedType else "Exercise Type" ),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = {
                        typeExpanded = false
                    }
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(text = type) },
                            onClick = {
                                onSelectedTypeChange(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = muscleExpanded,
                onExpandedChange = { muscleExpanded = it },
            ) {
                TextField(
                    value = ( if (selectedMuscle !== "") selectedMuscle else "Target Muscle" ),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleExpanded)
                    },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = muscleExpanded,
                    onDismissRequest = {
                        muscleExpanded = false
                    }
                ) {
                    muscles.forEach { muscle ->
                        DropdownMenuItem(
                            text = { Text(text = muscle) },
                            onClick = {
                                onSelectedMuscleChange(muscle)
                                muscleExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = difficultyExpanded,
                onExpandedChange = { difficultyExpanded = it },
            ) {
                TextField(
                    value = ( if (selectedDifficulty !== "") selectedDifficulty else "Difficulty" ),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded)
                    },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = difficultyExpanded,
                    onDismissRequest = {
                        difficultyExpanded = false
                    }
                ) {
                    difficulties.forEach { difficulty ->
                        DropdownMenuItem(
                            text = { Text(text = difficulty) },
                            onClick = {
                                onSelectedDifficultyChange(difficulty)
                                difficultyExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onSearch(exerciseName, selectedType, selectedMuscle, selectedDifficulty)
                },
                modifier = Modifier
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text("Search")
            }
        }
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



