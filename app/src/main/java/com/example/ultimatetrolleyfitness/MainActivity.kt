package com.example.ultimatetrolleyfitness

import StepCounterHelper
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ultimatetrolleyfitness.db.DatabaseConnection
import com.example.ultimatetrolleyfitness.exercise.Exercise
import com.example.ultimatetrolleyfitness.exercise.ExerciseDetailSheet
import com.example.ultimatetrolleyfitness.exercise.myAPI
import com.example.ultimatetrolleyfitness.navigation.BottomNavigationBar
import com.example.ultimatetrolleyfitness.nutrition.FoodAttribute
import com.example.ultimatetrolleyfitness.nutrition.FoodDetailScreen
import com.example.ultimatetrolleyfitness.nutrition.NutritionData
import com.example.ultimatetrolleyfitness.ui.theme.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
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
                        WorkoutScreen(
                            apiData,
                            fetchDataFromApi = { name, type, muscle, difficulty ->
                            fetchDataFromApi(name, type, muscle, difficulty)
                            }
                        )
                    }
                }
                composable("foodDetail/{foodName}") { backStackEntry ->
                    val foodName = backStackEntry.arguments?.getString("foodName")
                    val foodItem = NutritionData.getCSVData().firstOrNull { it[0] == foodName }

                    if (foodItem != null) {
                        BottomNav(navController = navController) {
                            FoodDetailScreen(foodItem, navController)
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
    var selectedTabIndex by remember { mutableStateOf(0)}
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        TabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            Tab(
                text = { Text("Progress") },
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 }
            )
            Tab(
                text = { Text("Your Plan") },
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 }
            ) 
        }
        
        when (selectedTabIndex) {
            0 -> Text(text = "Progress Content")
            1 -> Text(text= "Plans")
        }
    }
}



@Composable
fun NutritionScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            Tab(
                text = { Text("Browse") },
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 }
            )
            Tab(
                text = { Text("My Food") },
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 }
            )
        }

        when (selectedTabIndex) {
            0 -> {
                BrowseNutritionContent(navController)
            }
            1 -> {
                MyFoodContent()
            }
        }
    }
}

@Composable
fun BrowseNutritionContent(navController: NavController) {
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

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(filteredData) { foodItem ->
                ClickableFoodItem(foodItem, navController)
            }
        }
    }
}

@Composable
fun MyFoodContent() {
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    var foodsState by remember { mutableStateOf<List<Array<String>>>(emptyList()) }

    // Function to fetch user's food data from Firebase Realtime Database
    fun fetchUserFoodData() {
        if (currentUserID != null) {
            val foodRef = DatabaseConnection("foods")

            foodRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val foodsList = mutableListOf<Array<String>>()
                    val children = snapshot.children

                    children.forEach() { it ->
                        val foodDetails = it.value as? MutableList<String>
                        if (foodDetails != null) {
                            it.key?.let { it1 -> foodDetails.add(it1) }
                        }
                        foodDetails?.let {
                            foodsList.add(it.toTypedArray())
                        }
                    }
                    foodsState = foodsList
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that may occur while fetching data
                }
            })
        }
    }

    // Fetch user's food data initially
    LaunchedEffect(Unit) {
        fetchUserFoodData()
    }

    // Display user's food data
    Column {
        LazyColumn {
            items(foodsState) { foodItem ->
                FoodItemCard(foodItem, ::fetchUserFoodData)
                Log.d("Food data", foodItem.contentToString())
            }
        }
    }
}

@Composable
fun FoodItemCard(foodItem: Array<String>, fetchFunction: () -> Unit) {
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    var key by remember { mutableStateOf<String>("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val attributeNames = listOf(
                "Name", "Measure", "Grams", "Calories",
                "Protein", "Fat", "Saturated Fats", "Fiber", "Carbs", "Category"
            )

            foodItem.forEachIndexed { index, value ->
                if (index < attributeNames.size) {
                    FoodAttribute(attribute = attributeNames[index], value = value)
                } else {
                    key = value
                    Log.d("Key", key)
                }
            }

            // Add a button to remove the item from the database
            if (currentUserID != null) {
                Button(
                    onClick = {
                        DatabaseConnection("foods")?.child(key)?.removeValue()
                        fetchFunction() // Call the fetch function after removal
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Remove")
                }
            }
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
    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchButtonClicked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            Tab(
                text = { Text("Browse") },
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 }
            )
            Tab(
                text = { Text("Favorites") },
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 }
            )
        }
        
        when (selectedTabIndex) {
            0 -> {
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
                        searchButtonClicked = true
                        fetchDataFromApi(searchExerciseName, searchType, searchMuscle, searchDifficulty)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                if (searchButtonClicked) {
                    BrowseTabContent(apiData)
                }
            }
            1 -> FavoritesTabContent()
        }
    }
//    Column {
//        WorkoutSearchBar(
//            exerciseName = exerciseName,
//            onExerciseNameChange = { exerciseName = it },
//            selectedType = selectedType,
//            onSelectedTypeChange = { selectedType = it },
//            selectedMuscle = selectedMuscle,
//            onSelectedMuscleChange = { selectedMuscle = it },
//            selectedDifficulty = selectedDifficulty,
//            onSelectedDifficultyChange = { selectedDifficulty = it },
//            onSearch = { searchExerciseName, searchType, searchMuscle, searchDifficulty ->
//                fetchDataFromApi(searchExerciseName, searchType, searchMuscle, searchDifficulty)
//            }
//        )
//        Spacer(modifier = Modifier.weight(1f))
//        DisplayJsonData(apiData)
//    }

}

@Composable
fun BrowseTabContent(apiData: List<Exercise>?) {
    if (apiData.isNullOrEmpty()) {
        Text(text = "No exercises were found.")
    } else {
        DisplayJsonData(apiData)
    }
}

@Composable
fun FavoritesTabContent() {
    Text(text = "Favorites Tab Content")
}

@Composable
fun DisplayJsonData(data: List<Exercise>?) {
    LazyColumn {
        items(data ?: emptyList()) { exercise ->
            if (exercise.name.isNotEmpty()) {
                val buttonState = remember { mutableStateOf(false) }
                val showSheet = remember { mutableStateOf(false) }
                val name = exercise.name
                val type = exercise.type
                val muscle = exercise.muscle
                val equipment = exercise.equipment
                val difficulty = exercise.difficulty
                val instructions = exercise.instructions

                if (showSheet.value) {
                    ExerciseDetailSheet(
                        name = name,
                        type = type,
                        muscle = muscle,
                        equipment = equipment,
                        difficulty = difficulty,
                        instructions = instructions
                    ) {
                        showSheet.value = false
                    }
                }

                ListItem(
//                    leadingContent = {
//                        IconButton(onClick = {buttonState.value = !buttonState.value }) { // Should add to users workout plan
//                            Icon(
//                                imageVector = if (buttonState.value) {
//                                    Icons.Default.Favorite
//                                } else {
//                                    Icons.Default.FavoriteBorder
//                                },
//                                contentDescription = if (buttonState.value) {
//                                    "Remove from Favorites button"
//                                } else {
//                                    "Add to favorites button"
//                                }
//                            )
//                        }
//                    },
                    headlineContent = {
                        Text(text = name.replaceFirstChar { it.uppercase() })
                    },
                    overlineContent = {
                        Text(text = "${type.replaceFirstChar { it.uppercase() }} - ${muscle.replaceFirstChar { it.uppercase() }}")
                    },
                    supportingContent = {
                        Text(text = "Difficulty: ${difficulty.replaceFirstChar { it.uppercase() }}")
                    },
                    trailingContent = {
                        IconButton(onClick = {showSheet.value = true}) { // Should launch detailed workout info screen
                            Icon(
                                imageVector = (Icons.Outlined.Info),
                                contentDescription = "Info Button"
                            )
                        }
                    }
                )
            } else {
                Text(text = "No exercises were found.")
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
            .fillMaxWidth()
    ) {
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
                    value = (if (selectedType !== "") selectedType else "Exercise Type"),
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
                    value = (if (selectedMuscle !== "") selectedMuscle else "Target Muscle"),
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
                    value = (if (selectedDifficulty !== "") selectedDifficulty else "Difficulty"),
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

// Function to update UI with step count


//
//    private fun updateUI(stepCount: Int) {
//        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
//        val progress_circular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.progress_circular)
//
//        // Update UI elements based on step count
//        tv_stepsTaken.text = stepCount.toString()
//        progress_circular.setProgressWithAnimation(stepCount.toFloat())
//    }
//}



