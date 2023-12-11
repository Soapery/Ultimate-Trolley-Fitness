package com.example.ultimatetrolleyfitness.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val exercisesForDay = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val exerciseRef = DatabaseConnection("exercises")

    LaunchedEffect(currentUserID) {
        exerciseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedExercises = mutableListOf<Map<String, Any>>()

                for (exerciseSnapshot in snapshot.children) {
                    val selectedDaysSnapshot = exerciseSnapshot.child("selectedDays")
                    val type = object : GenericTypeIndicator<List<String>>() {}
                    val selectedDays = selectedDaysSnapshot.getValue(type)

                    if (selectedDays != null && day in selectedDays) {
                        val exerciseMap = mutableMapOf<String, Any>()

                        for (propertySnapshot in exerciseSnapshot.children) {
                            exerciseMap[propertySnapshot.key ?: ""] = propertySnapshot.value ?: ""
                        }
                        fetchedExercises.add(exerciseMap)
                    }
                }
                exercisesForDay.value = fetchedExercises
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that may occur while fetching data
            }
        })
    }

    LazyColumn() {
        items(exercisesForDay.value) { exercise ->
            ExerciseCard(exercise)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ExerciseCard(exercise: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
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
        }
    }
}