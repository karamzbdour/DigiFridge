package com.example.greenloop.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.api.OpenRouterManager
import com.example.greenloop.data.model.GeneratedRecipe
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.model.Recipe
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.IngredientRepository
import com.example.greenloop.data.repository.RecipeRepository
import com.example.greenloop.data.repository.UserRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val historyRepository: HistoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val inventory: StateFlow<List<Ingredient>> = ingredientRepository.allIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedIngredients = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIngredients: StateFlow<Set<Int>> = _selectedIngredients.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedRecipes = MutableStateFlow<List<GeneratedRecipe>>(emptyList())
    val generatedRecipes: StateFlow<List<GeneratedRecipe>> = _generatedRecipes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, GeneratedRecipe::class.java)
    private val adapter = moshi.adapter<List<GeneratedRecipe>>(listType)

    fun toggleIngredientSelection(id: Int) {
        _selectedIngredients.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun generateAiRecipe() {
        val selectedIds = _selectedIngredients.value
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            try {
                val selectedNames = inventory.value
                    .filter { it.id in selectedIds }
                    .joinToString(", ") { it.name }

                val prompt = """
                    Generate at least 3 simple waste-reducing recipes using these ingredients: ${selectedNames}.
                    The recipes must be optimized to rescue these items from being wasted.
                    Response MUST be a strict JSON array of objects with this structure:
                    [
                      {
                        "recipeName": "String",
                        "prepTimeMinutes": Int,
                        "difficulty": "Easy/Medium/Hard",
                        "steps": ["Step 1", "Step 2", ...]
                      }
                    ]
                    Only return the JSON array.
                """.trimIndent()

                val responseText = OpenRouterManager.generateContent(prompt) ?: ""
                
                // Clean the response if it contains markdown code blocks
                val jsonString = responseText.substringAfter("```json").substringBeforeLast("```").trim()
                val finalJson = if (jsonString.isEmpty()) responseText.trim() else jsonString
                
                val recipes = adapter.fromJson(finalJson) ?: emptyList()
                if (recipes.isEmpty()) {
                    _errorMessage.value = "No recipes could be generated. Please try again."
                } else {
                    _generatedRecipes.value = recipes
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error generating recipes: ${e.localizedMessage}. Please check your OPENROUTER_API_KEY in local.properties."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun extractQuantity(quantityStr: String?): Int {
        if (quantityStr == null) return 1
        return try {
            quantityStr.removePrefix("x").toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    fun completeGeneratedRecipe(recipe: GeneratedRecipe) {
        val selectedIds = _selectedIngredients.value

        viewModelScope.launch {
            val selectedIngredients = inventory.value.filter { it.id in selectedIds }
            val moneySaved = selectedIngredients.sumOf { (it.price ?: 0.0) * extractQuantity(it.quantity) }

            // Save to history
            val history = UpcycleHistory(
                recipeId = -1, // AI generated
                recipeTitle = recipe.recipeName,
                co2Saved = selectedIds.size * 0.5, // Estimated 0.5kg per ingredient rescued
                moneySaved = moneySaved
            )
            historyRepository.insertHistory(history)

            // Remove used ingredients from inventory
            selectedIngredients.forEach {
                ingredientRepository.deleteIngredient(it)
            }

            // Reset state
            _generatedRecipes.value = emptyList()
            _selectedIngredients.value = emptySet()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    class Factory(
        private val recipeRepository: RecipeRepository,
        private val ingredientRepository: IngredientRepository,
        private val historyRepository: HistoryRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(recipeRepository, ingredientRepository, historyRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
