package com.example.greenloop.data.dao

import androidx.room.*
import com.example.greenloop.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes WHERE isFavorite = 1")
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE isWasteReducing = 1")
    fun getWasteReducingRecipes(): Flow<List<Recipe>>
}
