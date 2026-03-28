package com.example.greenloop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val ingredients: String, // Comma-separated or JSON
    val steps: String, // Joined string or JSON
    val preparationTime: Int, // in minutes
    val difficulty: String, // Easy, Medium, Hard
    val co2Saved: Double, // in kg
    val isWasteReducing: Boolean = true,
    val isFavorite: Boolean = false
)
