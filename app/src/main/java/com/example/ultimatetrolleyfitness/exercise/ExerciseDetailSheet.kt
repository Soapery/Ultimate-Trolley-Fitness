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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    isFavorite: Boolean,
    onDismiss: () -> Unit
) {
    // Stores state of bottom sheet for display
    val modalBottomSheetState = rememberModalBottomSheetState()
    // Initial state is determined by if the current exercise is already favoured by the user
    var userFavorite by remember { mutableStateOf(isFavorite) }
    val showDialogState = remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    // For adding a workout to the users exercise plan
                    IconButton(
                        onClick = { showDialogState.value = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to Exercise plan"
                        )
                    }
                    // For adding a workout to the users favorites
                    IconButton(
                        onClick = {
                            // If current workout isn't favoured, add it to favorites, otherwise remove.
                            if (!userFavorite) {
                                addToFavorites(
                                    name,
                                    type,
                                    muscle,
                                    equipment,
                                    difficulty,
                                    instructions
                                )
                            } else {
                                removeFromFavorites(name)
                            }

                            // Changes to this variable affect button state as well!
                            userFavorite = !userFavorite
                        }
                    ) {
                        // Shown hollow or filled in dependent on if current workout is favoured
                        Icon(
                            imageVector = if (userFavorite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = if (userFavorite) {
                                "Remove"
                            } else {
                                "Add"
                            }
                        )
                    }
                }
            }
        )
        // Displaying workout data
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Type: $type")
            Text(text = "Muscle Group: $muscle")
            Text(text = "Equipment: $equipment")
            Text(text = "Difficulty: $difficulty")
            Spacer(modifier = Modifier.padding(16.dp))
            Text(text = "Instructions: $instructions")
        }

        // Invoking function to add exercise to workout plan
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