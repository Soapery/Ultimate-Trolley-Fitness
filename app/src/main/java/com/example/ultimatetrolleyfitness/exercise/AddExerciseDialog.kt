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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
){
    var currentStep by remember { mutableStateOf(0) }
    var sets by remember { mutableStateOf(0) }
    var reps by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(emptyList<String>()) }

//  Checking via Logcat if variables retain changed values
    DisposableEffect(showDialogState.value) {
        val logTag = "AddExercise"

        onDispose {
            Log.d(
                logTag,
                "Sets: $sets, Reps: $reps, Selected Days: $selectedDays"
            )
        }
    }

    if (showDialogState.value) {
        Dialog(
            onDismissRequest = onCloseDialog,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp) // Adjust width as needed
                    .height(500.dp) // Adjust height as needed
                    .padding(16.dp),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 40.dp, bottomEnd = 40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                when (currentStep) {
                    0 -> {
                        SetAndRepsSelection {
                            currentStep = 1
                            sets =sets
                            reps = reps
                        }
                    }
                    1 -> {
                        DaySelection {
                            selectedDays = selectedDays
                            onCloseDialog()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaySelection(
    onCloseDialog: () -> Unit
) {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val selectedDays = remember { mutableStateOf(emptyList<String>()) }

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
        items(count = daysOfWeek.size) {index ->
            val day = daysOfWeek[index]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 68.dp)
            ) {
                DaySelectionItem(
                    day = day,
                    onDaySelected = { selectedDay, isSelected ->
                        val updatedList = if (isSelected) {
                            selectedDays.value + selectedDay
                        } else {
                            selectedDays.value - selectedDay
                        }
                        selectedDays.value = updatedList
                    }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCloseDialog) {
                Text(text = "Add")
            }
        }
    }
}

@Composable
fun DaySelectionItem(
    day: String,
    onDaySelected: (String, Boolean) -> Unit
) {
    val checkedState = remember { mutableStateOf(false) }

    Row() {
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

@Composable
fun SetAndRepsSelection(
    onNextClicked: () -> Unit
) {
    var sets by remember { mutableStateOf(0) }
    var reps by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Enter Sets and Reps")
        Spacer(modifier = Modifier.height(96.dp))

        SetAndRepsItem(
            initialSets = sets,
            initialReps = reps,
            onSetsChanged = { sets = it },
            onRepsChanged = { reps = it }
        )

        Spacer(modifier = Modifier.height(96.dp))

        Button(onClick = onNextClicked) {
            Text(text = "Next")
        }
    }
}

@Composable
fun SetAndRepsItem(
    initialSets: Int,
    initialReps: Int,
    onSetsChanged: (Int) -> Unit,
    onRepsChanged: (Int) -> Unit
) {
    var sets by remember { mutableStateOf(initialSets) }
    var reps by remember { mutableStateOf(initialReps) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Sets:")
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = sets.toString(),
            onValueChange = { input ->
                sets = input.toIntOrNull() ?: 0
                onSetsChanged(sets)
            },
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
            onValueChange = { input ->
                reps = input.toIntOrNull() ?: 0
                onRepsChanged(reps)
            },
            modifier = Modifier.width(50.dp)
        )
    }
}
