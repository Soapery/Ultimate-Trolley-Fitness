package com.example.ultimatetrolleyfitness.exercise

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Detailed view for Workout information supplied by workout API
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseDetailSheet(
    name: String,
    type: String,
    muscle: String,
    equipment: String,
    difficulty: String,
    instructions: String,
    onDismiss: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState()
    val buttonState = remember {mutableStateOf(false) }
    val showDialogState= remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        ListItem(
            headlineContent = {
                Text(text = name)
            },
            trailingContent = {
                Row (
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    IconButton(
                        onClick = { showDialogState.value = true }
                    ) { // Should add to users workout plan
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (buttonState.value) {
                                "Remove"
                            } else {
                                "Add"
                            }
                        )
                    }
                    IconButton(
                        onClick = {buttonState.value = !buttonState.value }
                    ) { // Should add to users favorites tab
                        Icon(
                            imageVector = if (buttonState.value) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = if (buttonState.value) {
                                "Remove"
                            } else {
                                "Add"
                            }
                        )
                    }
                }
            }
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Type: $type")
            Text(text = "Muscle Group: $muscle")
            Text(text = "Equipment: $equipment")
            Text(text = "Difficulty: $difficulty")
            Spacer(modifier = Modifier.padding(16.dp))
            Text(text = "Instructions: $instructions")
        }

        if (showDialogState.value) {
            AddExercise(
                name,
                type,
                muscle,
                equipment,
                difficulty,
                instructions,
                showDialogState = showDialogState,
                onCloseDialog = { showDialogState.value = false }
            )
        }
    }
}