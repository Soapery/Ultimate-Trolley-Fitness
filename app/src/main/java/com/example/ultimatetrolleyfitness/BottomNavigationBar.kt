package com.example.ultimatetrolleyfitness

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        BottomNavItem("Nutrition"),
        BottomNavItem("Home"),
        BottomNavItem("Workout")
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        modifier = modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = currentRoute == getRouteForIndex(index)

            NavigationBarItem(
                icon = {
                    when (index) {
                        0 -> Icon(Icons.Filled.Restaurant, contentDescription = "Nutrition")
                        1 -> Icon(Icons.Filled.Tram, contentDescription = "Home")
                        2 -> Icon(Icons.Filled.FitnessCenter, contentDescription = "Workout")
                        else -> Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
                    }
                },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(getRouteForIndex(index))
                }
            )
        }
    }
}

private fun getRouteForIndex(index: Int): String {
    return when (index) {
        0 -> "nutrition"
        1 -> "home"
        2 -> "workout"
        else -> ""
    }
}