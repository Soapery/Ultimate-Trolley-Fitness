package com.example.ultimatetrolleyfitness

data class Exercise(
    val id: Int,
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
)