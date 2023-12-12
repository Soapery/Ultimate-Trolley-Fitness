package com.example.ultimatetrolleyfitness.ui.theme

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object for creating and configuring a Retrofit instance for API communication.
 */
object RetrofitInstance {

    /** The base URL of the API. */
    private const val BASE_URL = "https://api.api-ninjas.com/"

    /**
     * The Retrofit instance configured with the base URL and Gson converter factory.
     */
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) //Set the base URL for the API
        // Use Gson for JSON serialization and deserialization
        // GSON Converts JSON Data into Java Objects
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    // Build the Retrofit instance
}