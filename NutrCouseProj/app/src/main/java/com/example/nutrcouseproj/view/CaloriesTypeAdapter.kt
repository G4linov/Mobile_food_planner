//package com.example.nutrcouseproj.view
//
//import com.google.gson.JsonDeserializationContext
//import com.google.gson.JsonDeserializer
//import com.google.gson.JsonElement
//
//class CaloriesTypeAdapter : JsonDeserializer<Int> {
//    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Int {
//        return json.asDouble.toInt() // Преобразование в Int
//    }
//}