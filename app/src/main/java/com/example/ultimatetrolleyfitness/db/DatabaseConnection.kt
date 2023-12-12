package com.example.ultimatetrolleyfitness.db

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

/**
 * Function to establish a connection to the Firebase Realtime Database.
 * @param pathString The path within the database to retrieve the reference.
 * @return DatabaseReference? A reference to the specified path in the database.
 */
fun DatabaseConnection(pathString: String): DatabaseReference? {
    val database = Firebase.database // Get the Firebase database instance
    val currentUser = FirebaseAuth.getInstance().currentUser // Get the current user's authentication instance
    val currentUserID = currentUser?.uid // Get the unique user ID if available
    val ref = currentUserID?.let { database.getReference("users").child(it).child(pathString) } // Create a reference to the specified path in the database if the user ID is available

    return ref
}