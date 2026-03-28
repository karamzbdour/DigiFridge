package com.example.greenloop.data

import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.model.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object DatabaseInitializer {
    fun populateDatabase(db: AppDatabase) {
        val recipeDao = db.recipeDao()
        val ingredientDao = db.ingredientDao()
        
        CoroutineScope(Dispatchers.IO).launch {
            // Sample Ingredients
            val cal = Calendar.getInstance()
            val ingredients = listOf(
                Ingredient(name = "Milk", category = "Dairy", expiryDate = cal.apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis, quantity = "1L"),
                Ingredient(name = "Spinach", category = "Vegetables", expiryDate = cal.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis, quantity = "200g"),
                Ingredient(name = "Greek Yogurt", category = "Dairy", expiryDate = cal.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis, quantity = "500g")
            )
            ingredients.forEach { ingredientDao.insertIngredient(it) }

            // Sample Recipes
            val recipes = listOf(
                Recipe(
                    title = "Expiring Milk Pancakes",
                    description = "A great way to use up milk that's about to expire.",
                    ingredients = "Milk, Flour, Eggs, Sugar",
                    steps = "1. Mix dry ingredients. 2. Whisk in milk and eggs. 3. Cook on a griddle until golden.",
                    preparationTime = 20,
                    difficulty = "Easy",
                    co2Saved = 1.2,
                    isWasteReducing = true
                ),
                Recipe(
                    title = "Wilting Spinach Pesto",
                    description = "Turn slightly wilted spinach into a delicious pasta sauce.",
                    ingredients = "Spinach, Garlic, Olive Oil, Parmesan, Nuts",
                    steps = "1. Blend all ingredients until smooth. 2. Toss with your favorite pasta.",
                    preparationTime = 10,
                    difficulty = "Easy",
                    co2Saved = 0.8,
                    isWasteReducing = true
                )
            )
            recipes.forEach { recipeDao.insertRecipe(it) }
        }
    }
}
