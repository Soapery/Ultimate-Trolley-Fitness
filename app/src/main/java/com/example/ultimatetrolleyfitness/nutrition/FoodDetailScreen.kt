package com.example.ultimatetrolleyfitness.nutrition

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ultimatetrolleyfitness.db.DatabaseConnection

/**
 * Detailed views for each food item displayed by the CSV.
 */
// Firebase database reference
val foodRef = DatabaseConnection("foods")
// Detailed food screen displaying information of the selected clickable food item
@Composable
fun FoodDetailScreen(foodItem: Array<String>, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display food details using FoodAttribute

            FoodAttribute("Name", foodItem[0])
            FoodAttribute("Measure", foodItem[1])
            FoodAttribute("Grams", foodItem[2])
            FoodAttribute("Calories", foodItem[3])
            FoodAttribute("Protein", foodItem[4])
            FoodAttribute("Fat", foodItem[5])
            FoodAttribute("Saturated Fats", foodItem[6])
            FoodAttribute("Fiber", foodItem[7])
            FoodAttribute("Carbs", foodItem[8])
            FoodAttribute("Category", foodItem[9])

            // Back Button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                Text("Back")
            }

            val context = LocalContext.current

            // Add a button to add the item to the database with confirmation
            Button(
                onClick = {
                        // Show a confirmation toast before adding the item
                        Toast.makeText(context, "Adding food, please wait...", Toast.LENGTH_SHORT)
                            .show()


                        val newFoodRef = foodRef?.push()
                        newFoodRef?.setValue(foodItem.toMutableList()) // Push the food item associated with the user to the database

                        // Show a toast message confirming the addition
                        Toast.makeText(context, "Food added successfully!", Toast.LENGTH_SHORT)
                            .show()

                        // Navigate back after successful addition
                        navController.popBackStack()
                    // }
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text("Add")
            }
        }
    }
}


/**
 * Food attributes displayed in a dolumn on the page
 */
@Composable
fun FoodAttribute(attribute: String, value: String) {
    // Column to display food attribute and its value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = attribute,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
        )
    }
}

//Food,Measure,Grams,Calories,Protein,Fat,Sat.Fat,Fiber,Carbs,Category