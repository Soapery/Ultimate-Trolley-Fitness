package com.example.ultimatetrolleyfitness

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.NavigationBarItem

data class BottomNavItem(val label: String)

@Composable
fun RowScope.BottomNavItem(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = { onClick() },
        icon = { /* Set your icons here */ },
        label = { Text(item.label, color = if (selected) Color.Blue else Color.Gray) },
        modifier = Modifier.fillMaxHeight()
    )
}

