package com.example.nutrcouseproj.model

import com.example.nutrcouseproj.clients.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .apply {
                token?.let {
                    header("Authorization", "Bearer $it")
                }
            }
            .build()

        return chain.proceed(newRequest)
    }
}