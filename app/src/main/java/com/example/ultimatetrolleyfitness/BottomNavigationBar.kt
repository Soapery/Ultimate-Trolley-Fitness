package com.example.ultimatetrolleyfitness

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tram
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        BottomNavItem("Nutrition"),
        BottomNavItem("Main"),
        BottomNavItem("Workout")
    )

    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar(
        modifier = modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    when (index) {
                        0 -> Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = "Nutrition"
                        ) // Replace with the icon for "Nutrition"
                        1 -> Icon(
                            Icons.Filled.Tram,
                            contentDescription = "Home"
                        ) // Replace with the icon for "Main"
                        2 -> Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = "Workout"
                        ) // Replace with the icon for "Workout"
                        else -> Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
                    }
                },
                label = { Text(item.label) },
                selected = selectedItem == index,
                onClick = { selectedItem = index }
            )
        }
    }
}