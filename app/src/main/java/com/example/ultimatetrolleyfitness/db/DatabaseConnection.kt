package com.example.ultimatetrolleyfitness.db

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

fun DatabaseConnection(pathString: String): DatabaseReference? {
    val database = Firebase.database

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserID = currentUser?.uid // Get the unique user ID
    val foodRef = currentUserID?.let { database.getReference("users").child(it).child("foods") }

    return foodRef
}