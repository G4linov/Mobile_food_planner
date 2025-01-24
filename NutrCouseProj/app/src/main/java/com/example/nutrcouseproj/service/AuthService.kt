package com.example.nutrcouseproj.service

import com.example.nutrcouseproj.model.RegisterRequest
import com.example.nutrcouseproj.model.TokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface AuthService {
    @FormUrlEncoded
    @POST("/auth/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<TokenResponse>

    @POST("/auth/register")
    fun register(@Body request: RegisterRequest): Call<TokenResponse>
}