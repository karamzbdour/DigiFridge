package com.example.greenloop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upcycle_history")
data class UpcycleHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val recipeTitle: String,
    val completedDate: Long = System.currentTimeMillis(),
    val co2Saved: Double,
    val moneySaved: Double = 0.0
)
