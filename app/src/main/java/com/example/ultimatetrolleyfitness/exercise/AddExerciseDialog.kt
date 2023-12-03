package com.example.ultimatetrolleyfitness.exercise

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

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
                            DaySelectionItem(day = day)
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
        }
    }
}

@Composable
fun DaySelectionItem(day: String) {
    val checkedState = remember { mutableStateOf(false) }

    Row() {
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = { isChecked ->
                checkedState.value = isChecked
            },
        )
        Text(
            text = day,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}