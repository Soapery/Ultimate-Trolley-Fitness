package com.example.ultimatetrolleyfitness.exercise
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface myAPI {
    @Headers(value = ["X-Api-Key: A8GQrx7TOwo+sTuPYChX7w==HZgBaaH3NzRWnOSq"])
    @GET("v1/exercises")
    fun getExercises(
        @Query("name") name: String,
        @Query("type") type: String,
        @Query("muscle") muscle: String,
        @Query("difficulty") difficulty: String
    ): Call<List<Exercise>>
}