package com.example.ultimatetrolleyfitness
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart

interface myAPI {

    @Headers(value = ["X-Api-Key: A8GQrx7TOwo+sTuPYChX7w==HZgBaaH3NzRWnOSq"])
    @GET("v1/exercises")
    fun getExercises(): Call<List<Exercise>>

}