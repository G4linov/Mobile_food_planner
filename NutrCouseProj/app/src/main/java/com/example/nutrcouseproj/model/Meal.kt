package com.example.nutrcouseproj.model

data class Meal(
    val title: String,
    val calories: Double,
    val details: List<UserProduct> // Список подробностей для меню
)

