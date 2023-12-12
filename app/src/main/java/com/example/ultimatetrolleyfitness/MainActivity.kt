package com.example.ultimatetrolleyfitness

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ultimatetrolleyfitness.db.DatabaseConnection
import com.example.ultimatetrolleyfitness.exercise.Exercise
import com.example.ultimatetrolleyfitness.exercise.ExerciseDetailSheet
import com.example.ultimatetrolleyfitness.exercise.myAPI
import com.example.ultimatetrolleyfitness.home.DaysExercises
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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


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
            NavHost(navController = navController, startDestination = "com/example/ultimatetrolleyfitness/home") {

                // Navigation setup for the "home" destination
                composable("com/example/ultimatetrolleyfitness/home") {
                    BottomNav(navController = navController) {
                        HomeScreen(navController = navController)
                    }
                }

                // Navigation setup for the "nutrition" destination
                composable("nutrition") {
                    BottomNav(navController = navController) {
                        NutritionScreen(navController = navController)
                    }
                }

                // Navigation setup for the "workout" destination
                composable("workout") {
                    BottomNav(navController = navController) {
                        // Displaying the WorkoutScreen
                        WorkoutScreen(
                            apiData,
                            fetchDataFromApi = { name, type, muscle, difficulty ->
                                // Feting data form the api base on the user provided perameters
                            fetchDataFromApi(name, type, muscle, difficulty)
                            }
                        )
                    }
                }
                // Composable setup for displaying detailed information about fooditems
                composable("foodDetail/{foodName}") { backStackEntry ->
                    val foodName = backStackEntry.arguments?.getString("foodName")
                    val foodItem = NutritionData.getCSVData().firstOrNull { it[0] == foodName }

                    // If the food item is found, display the FoodDetailScreen
                    if (foodItem != null) {
                        BottomNav(navController = navController) {
                            FoodDetailScreen(foodItem, navController)
                        }
                    } else {
                        // Handle case when food item is not found. Display a message indicating so
                        Text("Food item not found")
                    }
                }

                // Composable setup for displaying exercises planned for a specific day
                composable("plan/{day}") {backStackEntry ->
                    val day = backStackEntry.arguments?.getString("day")
                    // If the day value is present, display exercises for that day
                    if (day != null) {
                        BottomNav(navController = navController) {
                            DaysExercises(day)
                        }
                   // If the day value is null, display a message indicating no exercises found for that day
                    } else {
                        Text(text = "Exercises for $day not found.")
                    }
                }
            }
        }
    }


    /**
     * Fetches workout related information from an API
     */
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

/**
 * Composable to create the Bottom Navigation bar
 */
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

/**
 * Homescreen view displaying either progress or your plan tabs
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
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
            0 -> ProgressContent()
            1 -> PlanContent(navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgressContent() {
    val dayOfWeek = LocalDate.now().dayOfWeek
    val dayOfWeekString = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

    Text(text = "Today is $dayOfWeekString")
}


@Composable
fun PlanContent(navController: NavController) {
    // List of days of the week
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(count = daysOfWeek.size) {index ->
            // For each day in the list, create a ClickableDayCard
            val day = daysOfWeek[index]
            ClickableDayCard(day = day, navController = navController)
        }
    }
}

@Composable
fun ClickableDayCard(day: String, navController: NavController) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(8.dp)
            .clickable { navController.navigate("plan/$day") },
    ) {
        Text(text = "$day") // Display the day inside the card
    }
}

@Composable
fun NutritionScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
        // TabRow displaying "Browse" and "My Food" tabs
        TabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            // "Browse" tab
            Tab(
                text = { Text("Browse") },
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 }
            )
            // "My Food" tab
            Tab(
                text = { Text("My Food") },
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 }
            )
        }

        // Display different content based on the selected tab index
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
    // State to hold the search text and filtered data
    val searchText = remember { mutableStateOf("") }
    val csvData = remember { NutritionData.getCSVData() }
    var filteredData by remember { mutableStateOf(csvData) }

    Column {
        // Search bar with text field and search button
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
                        // Filtering data when search action is triggered
                        filteredData = csvData.filter { row ->
                            row.getOrNull(0)?.contains(searchText.value, ignoreCase = true) == true
                        }
                    },
                    onDone = {
                        // Filtering data when keyboard done action is triggered
                        filteredData = csvData.filter { row ->
                            row.getOrNull(0)?.contains(searchText.value, ignoreCase = true) == true
                        }
                    }
                )
            )

            Button(
                onClick = {
                    // Filtering data when search button is clicked
                    filteredData = csvData.filter { row ->
                        row.getOrNull(0)?.contains(searchText.value, ignoreCase = true) == true
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Search")
            }
        }

        // Displaying filtered data in a LazyColumn
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(filteredData) { foodItem ->
                ClickableFoodItem(foodItem, navController)
            }
        }
    }
}

/**
 * Display the users food items saved to the database for each users food page
 */
@Composable
fun MyFoodContent() {
    // Get the current user's ID from Firebase Auth
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

    // State to hold the user's food data
    var foodsState by remember { mutableStateOf<List<Array<String>>>(emptyList()) }

    // Function to fetch user's food data from Firebase Realtime Database
    fun fetchUserFoodData() {
        if (currentUserID != null) {
            // Fetching reference to the "foods" node in Firebase Realtime Database
            val foodRef = DatabaseConnection("foods")

            // Fetch data from Firebase and update the foodsState
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

/**
 * composable to create a card that will  display the information of a users saved food items along with a button to delete them from the database
 */
@Composable
fun FoodItemCard(foodItem: Array<String>, fetchFunction: () -> Unit) {
    // Get the current user's ID from Firebase Auth
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

    // State to hold the key in Firebase Database
    var key by remember { mutableStateOf<String>("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // List of attribute names for the food item
            val attributeNames = listOf(
                "Name", "Measure", "Grams", "Calories",
                "Protein", "Fat", "Saturated Fats", "Fiber", "Carbs", "Category"
            )

            // Iterate through the food item attributes
            foodItem.forEachIndexed { index, value ->
                if (index < attributeNames.size) {
                    // Display each attribute with its corresponding value
                    FoodAttribute(attribute = attributeNames[index], value = value)
                } else {
                    // Store the key (Firebase ID) of the food item
                    key = value
                    Log.d("Key", key)
                }
            }

            // Add a button to remove the item from the database
            if (currentUserID != null) {
                Button(
                    onClick = {
                        // Remove the item from Firebase database
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

/**
 * Make food items listed clickable and send the user to the fooddetail screen of the clicked food item.
 */
@Composable
fun ClickableFoodItem(foodItem: Array<String>, navController: NavController) {
    Box(
        modifier = Modifier
            .clickable {
                // Navigate with the food name as a parameter to the "foodDetail" destination
                navController.navigate("foodDetail/${foodItem[0]}") // Navigate with the food name as a parameter
            }
            .padding(8.dp)
    ) {
        Text(text = foodItem[0]) // Display the name of the food item
    }
}

/**
 * Workout screen composable display either the favorites or browse the workout content
 */
@Composable
fun WorkoutScreen(
    apiData: List<Exercise>?,
    fetchDataFromApi: (String, String, String, String) -> Unit
) {
    // Mutable state variables to hold exercise information
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
        // TabRow to switch between "Browse" and "Favorites" tabs
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

        // Display content based on the selected tab index
        when (selectedTabIndex) {
            0 -> {
                // Display WorkoutSearchBar for browsing exercises
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
                // Display BrowseTabContent if search button is clicked
                if (searchButtonClicked) {
                    BrowseTabContent(apiData)
                }
            }
            // Display FavoritesTabContent for favorite exercises
            1 -> FavoritesTabContent()
        }
    }
}

/**
 * Browse tab, content displaying exercises from DisplayJsonData
 */
@Composable
fun BrowseTabContent(apiData: List<Exercise>?) {
    // Check if the API data is null or empty
    if (apiData.isNullOrEmpty()) {
        // Display a message if no exercises were found
        Text(text = "No exercises were found.")
    } else {
        // Display the fetched exercise data
        DisplayExerciseData(apiData)
    }
}

/**
 * Users Favorites tab displaying workouts they have selected to add to favorites
 */
@Composable
fun FavoritesTabContent() {
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    var favoritesState by remember { mutableStateOf<List<Exercise>>(emptyList()) }

    fun fetchUserFavorites() {
        if (currentUserID != null) {
            val favoritesRef = DatabaseConnection("favorite_exercises")

            favoritesRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoritesList = mutableListOf<Exercise>()
                    val children = snapshot.children
                    var id = 1
                    children.forEach() { it ->
                        val name = it.child("name").value.toString()
                        val type = it.child("type").value.toString()
                        val muscle = it.child("muscle").value.toString()
                        val equipment = it.child("equipment").value.toString()
                        val difficulty = it.child("difficulty").value.toString()
                        val instructions = it.child("instructions").value.toString()
                        val exercise = Exercise(id, name, type, muscle, equipment, difficulty, instructions)
                        favoritesList.add(exercise)
                    }
                    favoritesState = favoritesList
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that may occur while fetching data
                }
            })
        }
    }

    // Fetch user's food data initially
    LaunchedEffect(Unit) {
        fetchUserFavorites()
        Log.d("exercise data", favoritesState.toString())
    }

    DisplayExerciseData(favoritesState)
}

/**
 * Displays workout related data.
 */
@Composable
fun DisplayExerciseData(data: List<Exercise>?) {
    LazyColumn {
        items(data ?: emptyList()) { exercise ->
            if (exercise.name.isNotEmpty()) {
                val favoritesRef = DatabaseConnection("favorite_exercises")
                val buttonState = remember { mutableStateOf(false) }
                val showSheet = remember { mutableStateOf(false) }

                // Extracting exercise details
                val name = exercise.name
                val type = exercise.type
                val muscle = exercise.muscle
                val equipment = exercise.equipment
                val difficulty = exercise.difficulty
                val instructions = exercise.instructions
                var isFavorite by remember {
                    mutableStateOf(false)
                }


                favoritesRef?.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(favorite in snapshot.children) {
                            if(favorite.child("name").value == name) {
                                isFavorite = true
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("a", "a")
                    }
                })

                // Conditionally display ExerciseDetailSheet based on showSheet value
                if (showSheet.value) {
                    ExerciseDetailSheet(
                        name = name,
                        type = type,
                        muscle = muscle,
                        equipment = equipment,
                        difficulty = difficulty,
                        instructions = instructions,
                        isFavorite = isFavorite,
                    ) {
                        showSheet.value = false
                    }
                }

                // Displaying exercise details in a ListItem
                ListItem(
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
                // Display a message if no exercises were found
            } else {
                Text(text = "No exercises were found.")
            }
        }
    }
}


/**
 * Search bar for displaying exercises based on search parameters
 */
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
    // Mutable state variables for dropdown and item heights
    var typeExpanded by rememberSaveable { mutableStateOf(false) }
    var muscleExpanded by rememberSaveable { mutableStateOf(false) }
    var difficultyExpanded by rememberSaveable { mutableStateOf(false) }

    var typeItemHeight by remember { mutableStateOf(0.dp) }
    var muscleItemHeight by remember { mutableStateOf(0.dp) }
    var difficultyItemHeight by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    // Lists for dropdown menu items
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
            // TextField for exercise name input
            TextField(
                value = exerciseName,
                onValueChange = { onExerciseNameChange(it) },
                label = { Text("Exercise Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Dropdown menus for exercise type, target muscle, and difficulty level
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
            ) {
                // Dropdown for exercise type selection
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
                        // Dropdown menu items for exercise type
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

            // Button to trigger search based on selected criteria
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