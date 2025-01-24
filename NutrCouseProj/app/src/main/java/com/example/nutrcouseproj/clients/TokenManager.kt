package com.example.nutrcouseproj.clients

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_TOKEN = "access_token"

    // Сохранить токен
    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    // Получить токен
    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }
}