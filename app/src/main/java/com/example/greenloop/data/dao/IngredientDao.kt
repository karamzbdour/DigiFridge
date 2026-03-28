package com.example.greenloop.data.dao

import androidx.room.*
import com.example.greenloop.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY expiryDate ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients WHERE expiryDate <= :threshold")
    fun getExpiringSoon(threshold: Long): Flow<List<Ingredient>>
}
