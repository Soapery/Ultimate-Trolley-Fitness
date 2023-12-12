package com.example.ultimatetrolleyfitness.exercise

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ultimatetrolleyfitness.db.DatabaseConnection
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * Composable for adding exercises along with specified sets and reps to a users profiles.
 */
@Composable
fun AddExercise(
    name: String,
    type: String,
    muscle: String,
    equipment: String,
    difficulty: String,
    instructions: String,
    showDialogState: MutableState<Boolean>, // State to control the dialog visibility
    onCloseDialog: () -> Unit // Callback function to close the dialog
) {
    // State variables to manage different parts of the exercise input
    var currentStep by remember { mutableStateOf(0) }
    var sets by remember { mutableStateOf(0) } // Number of sets for the exercise
    var reps by remember { mutableStateOf(0) } // Number of reps for the exercise
    var day by remember { mutableStateOf("") } // Selected day for the exercise
    var selectedDays by remember { mutableStateOf(emptyList<String>()) } // List of selected days for the exercise
    val exerciseRef = DatabaseConnection("exercises")

    // If the dialog state is true, display the dialog
    if (showDialogState.value) {
        Dialog(
            onDismissRequest = onCloseDialog,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(500.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(
                    topStart = 40.dp,
                    topEnd = 40.dp,
                    bottomStart = 40.dp,
                    bottomEnd = 40.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )

                // Display different content based on the current step in the process
            ) {
                when (currentStep) {
                    0 -> {
                        // Display set and reps selection UI
                        SetAndRepsSelection(
                            sets = sets,
                            onSetsChanged = { sets = it },
                            reps = reps,
                            onRepsChanged = { reps = it },
                            onNextClicked = { currentStep = 1 }
                        )
                    }

                    1 -> {
                        DaySelection(
                            day = day,
                            onDaySelected = { selectedDay, isSelected ->
                                // Update the list of selected days based on user interaction
                                val updatedList = if (isSelected) {
                                    selectedDays + selectedDay
                                } else {
                                    selectedDays - selectedDay
                                }
                                selectedDays = updatedList
                            },
                            onExercideAdded = {
                                // Store exercise details in the database when exercise is added
                                val exerciseMap: HashMap<String, Any?> = hashMapOf(
                                    "name" to name,
                                    "type" to type,
                                    "muscle" to muscle,
                                    "equipment" to equipment,
                                    "difficulty" to difficulty,
                                    "instructions" to instructions,
                                    "sets" to sets,
                                    "reps" to reps,
                                    "selectedDays" to selectedDays
                                )

                                val newExerciseRef = exerciseRef?.push()
                                newExerciseRef?.setValue(exerciseMap)

                                showDialogState.value = false
                            },
                            onCloseDialog = { showDialogState.value = false }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Function to add exercise details to a users favorites list.
 */
fun addToFavorites(
    name: String,
    type: String,
    muscle: String,
    equipment: String,
    difficulty: String,
    instructions: String
){
    // Reference to the "favorite_exercises" section in the database
    val favoritesRef = DatabaseConnection("favorite_exercises")

    // Create a map of exercise details to be stored in favorites
    val favoritesMap: HashMap<String, Any?> = hashMapOf(
        "name" to name,
        "type" to type,
        "muscle" to muscle,
        "equipment" to equipment,
        "difficulty" to difficulty,
        "instructions" to instructions
    )

    // Push a new entry to the favorites section and set exercise details in the database
    val newFavoritesRef = favoritesRef?.push()
    newFavoritesRef?.setValue(favoritesMap)
}

/**
 * Function to remove exercise details from a users favorites list.
 */
fun removeFromFavorites (name: String) {
    val favoritesRef = DatabaseConnection("favorite_exercises")
    // Adding a listener to retrieve data once from the database
    favoritesRef?.addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Loop through each child node in the "favorite_exercises" section
            for(favorite in snapshot.children) {
                // Check if the exercise name matches the provided name
                if(favorite.child("name").value == name) {
                    favorite.key?.let { favoritesRef.child(it)?.removeValue() }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("a", "a")
        }
    })
}


/**
 * Composable function to handle the day selection for users exercises
 */
@Composable
fun DaySelection(
    day: String,
    onDaySelected: (String, Boolean) -> Unit,
    onCloseDialog: () -> Unit,
    onExercideAdded: () -> Unit
) {
    // List of days of the week
    val daysOfWeek =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(text = "Select Days")
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Iterate through the days of the week
        items(count = daysOfWeek.size) { index ->
            val day = daysOfWeek[index]
            val checkedState = remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 68.dp)
            ) {
                Checkbox(
                    checked = checkedState.value,
                    onCheckedChange = { isChecked ->
                        checkedState.value = isChecked
                        onDaySelected(day, isChecked)
                    },
                )
                Text(
                    text = day,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onExercideAdded) {
                Text(text = "Add")
            }
        }
    }
}


/**
 * Composable function to allow users to select the number of sets and reps for their exercises
 */
@Composable
fun SetAndRepsSelection(
    sets: Int,
    onSetsChanged: (Int) -> Unit,
    reps: Int,
    onRepsChanged: (Int) -> Unit,
    onNextClicked: () -> Unit
) {
    val pattern = remember { Regex("^[0-9]*\$") } // Regex pattern to match numbers only
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Enter Sets and Reps")
        Spacer(modifier = Modifier.height(96.dp))

        // Row for entering sets
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Sets:") // Label for sets
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = sets.toString(),
                onValueChange = {
                    if (it.isNotEmpty() && it.toIntOrNull() != null) {
                        onSetsChanged(it.toInt()) // Invoke callback when sets change
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Row for entering reps
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Reps:")
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = reps.toString(),
                onValueChange = {
                    if (it.isNotEmpty() && it.toIntOrNull() != null) {
                        onRepsChanged(it.toInt()) // Invoke callback when reps change
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(96.dp))

        // Button to proceed to the next step
        Button(onClick = onNextClicked) {
            Text(text = "Next")
        }
    }
}
