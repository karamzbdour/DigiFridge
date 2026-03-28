package com.example.greenloop.data.repository

import com.example.greenloop.data.dao.RecipeDao
import com.example.greenloop.data.model.Recipe
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {
    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()
    val favoriteRecipes: Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()
    val wasteReducingRecipes: Flow<List<Recipe>> = recipeDao.getWasteReducingRecipes()

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }
}
