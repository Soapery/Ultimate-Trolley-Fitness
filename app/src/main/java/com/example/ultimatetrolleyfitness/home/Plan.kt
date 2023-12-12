package com.example.ultimatetrolleyfitness.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ultimatetrolleyfitness.db.DatabaseConnection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

@Composable
fun DaysExercises(day: String) {
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    val exercisesForDay = remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    val exerciseRef = DatabaseConnection("exercises")

    LaunchedEffect(currentUserID) {
        exerciseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedExercises = mutableMapOf<String, Map<String, Any>>()

                for (exerciseSnapshot in snapshot.children) {
                    val selectedDaysSnapshot = exerciseSnapshot.child("selectedDays")
                    val type = object : GenericTypeIndicator<List<String>>() {}
                    val selectedDays = selectedDaysSnapshot.getValue(type)

                    if (selectedDays != null && day in selectedDays) {
                        val exerciseMap = mutableMapOf<String, Any>()

                        for (propertySnapshot in exerciseSnapshot.children) {
                            exerciseMap[propertySnapshot.key ?: ""] = propertySnapshot.value ?: ""
                        }
                        fetchedExercises[exerciseSnapshot.key ?: ""] = exerciseMap
                    }
                }
                exercisesForDay.value = fetchedExercises.toMutableMap()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that may occur while fetching data
            }
        })
    }

    LazyColumn() {
        items(exercisesForDay.value.entries.toList()) { (exerciseKey, exercise) ->
            ExerciseCard(exercise, day, exerciseKey) { key ->
                removeExercise(key, day) {
                    exercisesForDay.value = exercisesForDay.value.filterKeys { it != key }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Composable to create a card to display the exercise data
 */
@Composable
fun ExerciseCard(exercise: Map<String, Any>, day: String, exerciseKey: String, onRemoveClicked: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Name: ${exercise["name"]}")
            Text("Sets: ${exercise["sets"]}")
            Text("Reps: ${exercise["reps"]}")
            Spacer(modifier = Modifier.padding(16.dp))
            Text("Type: ${exercise["type"]}")
            Text("Muscle group: ${exercise["muscle"]}")
            Text("Equipment: ${exercise["equipment"]}")
            Text("Difficulty: ${exercise["difficulty"]}")
            Spacer(modifier = Modifier.padding(16.dp))
            Text("Instructions: ${exercise["instructions"]}")

            IconButton(
                onClick = { onRemoveClicked(exerciseKey) },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.End)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}

fun removeExercise(exerciseKey: String, day: String, onExerciseRemoved: () -> Unit) {
    val exerciseRef = DatabaseConnection("exercises")?.child(exerciseKey)?.child("selectedDays")

    Log.d("Key", exerciseKey ?: "Key is null")

    exerciseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val selectedDays = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
            selectedDays?.let {
                val updatedSelectedDays = it.toMutableList()
                updatedSelectedDays.remove(day) // Remove the specified day

                // Update the selectedDays field in the database
                exerciseRef.setValue(updatedSelectedDays)
                    .addOnSuccessListener {
                        Log.d("RemoveDay", "Day removed successfully")
                        onExerciseRemoved()
                    }
                    .addOnFailureListener { e ->
                        Log.w("RemoveDay", "Error removing day", e)
                    }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("RemoveDay", "Cancelled", error.toException())
        }
    })
}
