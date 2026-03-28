package com.example.greenloop.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.api.GeminiManager
import com.example.greenloop.data.model.GeneratedRecipe
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.model.Recipe
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.IngredientRepository
import com.example.greenloop.data.repository.RecipeRepository
import com.example.greenloop.data.repository.UserRepository
import com.squareup.moshi.Moshi
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

    private val _generatedRecipe = MutableStateFlow<GeneratedRecipe?>(null)
    val generatedRecipe: StateFlow<GeneratedRecipe?> = _generatedRecipe.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(GeneratedRecipe::class.java)

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
            try {
                val selectedNames = inventory.value
                    .filter { it.id in selectedIds }
                    .joinToString(", ") { it.name }

                val prompt = """
                    Generate a simple waste-reducing recipe using these ingredients: $selectedNames.
                    The recipe must be optimized to rescue these items from being wasted.
                    Response MUST be a strict JSON object with this structure:
                    {
                      "recipeName": "String",
                      "prepTimeMinutes": Int,
                      "steps": ["Step 1", "Step 2", ...]
                    }
                    Only return the JSON.
                """.trimIndent()

                val response = GeminiManager.model.generateContent(prompt)
                val responseText = response.text ?: ""
                
                // Clean the response if it contains markdown code blocks
                val jsonString = responseText.substringAfter("```json").substringBeforeLast("```").trim()
                val finalJson = if (jsonString.isEmpty()) responseText.trim() else jsonString
                
                val recipe = adapter.fromJson(finalJson)
                _generatedRecipe.value = recipe
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun completeGeneratedRecipe() {
        val recipe = _generatedRecipe.value ?: return
        val selectedIds = _selectedIngredients.value

        viewModelScope.launch {
            // Save to history
            val history = UpcycleHistory(
                recipeId = -1, // AI generated
                recipeTitle = recipe.recipeName,
                co2Saved = selectedIds.size * 0.5 // Estimated 0.5kg per ingredient rescued
            )
            historyRepository.insertHistory(history)

            // Remove used ingredients from inventory
            inventory.value.filter { it.id in selectedIds }.forEach {
                ingredientRepository.deleteIngredient(it)
            }

            // Reset state
            _generatedRecipe.value = null
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
