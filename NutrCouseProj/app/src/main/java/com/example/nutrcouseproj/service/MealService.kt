package com.example.nutrcouseproj.service

import com.example.nutrcouseproj.model.DBProduct
import com.example.nutrcouseproj.model.Meal
import com.example.nutrcouseproj.model.MealCreate
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface MealService {
    @GET("/meals/")
    fun getMealsForDay(@Header("Authorization") token: String, @Query("date") date: String): Call<List<Meal>>

    @POST("/meals/")
    fun addMeal(@Header("Authorization") token: String, @Body meal: MealCreate): Call<Meal>

    @GET("/products/search")
    fun searchProducts(@Header("Authorization") token: String, @Query("query") query: String): Call<List<DBProduct>>

}