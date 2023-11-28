package com.example.ultimatetrolleyfitness.exercise

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseDetailSheet(name: String, type: String, muscle: String, equipment: String, difficulty: String, instructions: String, onDismiss: () -> Unit) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: $name")
            Text(text = "Type: $type")
            Text(text = "Muscle Group: $muscle")
            Text(text = "Equipment: $equipment")
            Text(text = "Difficulty: $difficulty")
            Text(text = "Instructions: $instructions")
        }
    }
}