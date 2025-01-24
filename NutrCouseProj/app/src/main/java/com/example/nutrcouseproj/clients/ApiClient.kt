package com.example.nutrcouseproj.clients

import com.example.nutrcouseproj.model.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private var retrofit: Retrofit? = null

    fun getClient(token: String? = null): Retrofit {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient.Builder()
                .apply {
                    token?.let {
                        addInterceptor(AuthInterceptor(it))
                    }
                }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl("http://185.125.218.224:8000")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}