package com.example.nutrcouseproj.service

import com.example.nutrcouseproj.model.DBProduct
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ProductService {
    @GET("/products/") // Замените путь на ваш реальный эндпоинт
    fun getProducts(@Header("Authorization") token: String): Call<List<DBProduct>>
}