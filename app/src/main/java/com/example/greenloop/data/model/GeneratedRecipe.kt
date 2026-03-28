package com.example.greenloop.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeneratedRecipe(
    val recipeName: String,
    val prepTimeMinutes: Int,
    val steps: List<String>
)
