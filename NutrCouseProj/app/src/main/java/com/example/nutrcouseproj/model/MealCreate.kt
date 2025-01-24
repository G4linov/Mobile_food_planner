package com.example.nutrcouseproj.model

data class MealCreate(
    val date: String, // Дата приема пищи в формате "yyyy-MM-dd"
    val food_name: String,
    val meal_type: String,// Название или ID продукта
    val amount: Int // Количество
)