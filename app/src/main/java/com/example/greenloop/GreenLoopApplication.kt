package com.example.greenloop

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.greenloop.data.AppDatabase
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.IngredientRepository
import com.example.greenloop.data.repository.RecipeRepository
import com.example.greenloop.data.repository.UserRepository

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class GreenLoopApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val ingredientRepository by lazy { IngredientRepository(database.ingredientDao()) }
    val recipeRepository by lazy { RecipeRepository(database.recipeDao()) }
    val historyRepository by lazy { HistoryRepository(database.historyDao()) }
    val userRepository by lazy { UserRepository(dataStore) }
}
