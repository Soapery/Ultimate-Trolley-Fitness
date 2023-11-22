package com.example.ultimatetrolleyfitness

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FoodDetailScreen(foodItem: Array<String>) {

    Column(modifier = Modifier.padding(16.dp)) {
        // Display each attribute of the food item
        Text(text = "Name: ${foodItem[0]}")
        Text(text = "Measure: ${foodItem[1]}")
        Text(text = "Grams: ${foodItem[2]}")
        Text(text = "Calories: ${foodItem[3]}")
        Text(text = "Protein: ${foodItem[4]}")
        Text(text = "Fat: ${foodItem[5]}")
        Text(text = "Saturated Fats: ${foodItem[6]}")
        Text(text = "Fiber: ${foodItem[7]}")
        Text(text = "Carbs: ${foodItem[8]}")
        Text(text = "Category: ${foodItem[9]}")


    }
}

//Food,Measure,Grams,Calories,Protein,Fat,Sat.Fat,Fiber,Carbs,Category