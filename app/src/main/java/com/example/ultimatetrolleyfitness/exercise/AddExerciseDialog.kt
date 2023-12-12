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
import com.example.ultimatetrolleyfitness.nutrition.foodRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@Composable
fun AddExercise(
    name: String,
    type: String,
    muscle: String,
    equipment: String,
    difficulty: String,
    instructions: String,
    showDialogState: MutableState<Boolean>,
    onCloseDialog: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var sets by remember { mutableStateOf(0) }
    var reps by remember { mutableStateOf(0) }
    var day by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(emptyList<String>()) }
    val exerciseRef = DatabaseConnection("exercises")


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
            ) {
                when (currentStep) {
                    0 -> {
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
                                val updatedList = if (isSelected) {
                                    selectedDays + selectedDay
                                } else {
                                    selectedDays - selectedDay
                                }
                                selectedDays = updatedList
                            },
                            onExercideAdded = {
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

fun addToFavorites(
    name: String,
    type: String,
    muscle: String,
    equipment: String,
    difficulty: String,
    instructions: String
){
    val favoritesRef = DatabaseConnection("favorite_exercises")

    val favoritesMap: HashMap<String, Any?> = hashMapOf(
        "name" to name,
        "type" to type,
        "muscle" to muscle,
        "equipment" to equipment,
        "difficulty" to difficulty,
        "instructions" to instructions
    )

    val newFavoritesRef = favoritesRef?.push()
    newFavoritesRef?.setValue(favoritesMap)
}

fun removeFromFavorites (name: String) {
    val favoritesRef = DatabaseConnection("favorite_exercises")
    favoritesRef?.addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for(favorite in snapshot.children) {
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



@Composable
fun DaySelection(
    day: String,
    onDaySelected: (String, Boolean) -> Unit,
    onCloseDialog: () -> Unit,
    onExercideAdded: () -> Unit
) {
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

@Composable
fun SetAndRepsSelection(
    sets: Int,
    onSetsChanged: (Int) -> Unit,
    reps: Int,
    onRepsChanged: (Int) -> Unit,
    onNextClicked: () -> Unit
) {
    val pattern = remember { Regex("^[0-9]*\$") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Enter Sets and Reps")
        Spacer(modifier = Modifier.height(96.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Sets:")
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = sets.toString(),
                onValueChange = {
                    if (it.isNotEmpty() && it.toIntOrNull() != null) {
                        onSetsChanged(it.toInt())
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Reps:")
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = reps.toString(),
                onValueChange = {
                    if (it.isNotEmpty() && it.toIntOrNull() != null) {
                        onRepsChanged(it.toInt())
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(96.dp))

        Button(onClick = onNextClicked) {
            Text(text = "Next")
        }
    }
}
