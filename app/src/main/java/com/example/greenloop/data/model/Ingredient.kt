package com.example.greenloop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., Dairy, Vegetable, Fruit, Cupboard
    val expiryDate: Long, // Timestamp
    val quantity: String? = null,
    val addedDate: Long = System.currentTimeMillis()
)
