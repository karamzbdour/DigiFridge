package com.example.greenloop.data.repository

import com.example.greenloop.data.dao.IngredientDao
import com.example.greenloop.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val ingredientDao: IngredientDao) {
    val allIngredients: Flow<List<Ingredient>> = ingredientDao.getAllIngredients()

    fun getExpiringSoon(threshold: Long): Flow<List<Ingredient>> {
        return ingredientDao.getExpiringSoon(threshold)
    }

    suspend fun insertIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient)
    }

    suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient)
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient)
    }

    suspend fun getIngredientByNameAndCategory(name: String, category: String): Ingredient? {
        return ingredientDao.getIngredientByNameAndCategory(name, category)
    }
}
