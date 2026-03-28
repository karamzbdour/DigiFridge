package com.example.greenloop.ui.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.api.OpenRouterManager
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Calendar

class DashboardViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    val inventoryItems: StateFlow<List<Ingredient>> = repository.allIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPrice: StateFlow<Double> = inventoryItems.map { items ->
        items.sumOf { it.price ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun scanImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val prompt = """
                    Identify the food items on this receipt or in this image. 
                    For each item, provide:
                    1. Name
                    2. Category (Dairy, Vegetables, Fruits, Meat, Cupboard)
                    3. Likely expiry date (in days from now)
                    4. Estimated price (as a number, e.g., 2.50)
                    5. Estimated calories (per typical serving or package)
                    6. Estimated protein in grams (per typical serving or package)
                    
                    Format the response ONLY as a JSON list: 
                    [{"name": "Milk", "category": "Dairy", "expiryDays": 7, "quantity": "1L", "price": 2.50, "calories": 150, "protein": 8.0}]
                """.trimIndent()
                
                val resultText = OpenRouterManager.analyzeImage(bitmap, prompt)
                _scanResult.value = resultText
                
                parseAndSaveResult(resultText)
            } catch (e: Exception) {
                _scanResult.value = "Error: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    private suspend fun parseAndSaveResult(resultText: String?) {
        if (resultText == null) return
        
        try {
            val cleanJson = resultText.trim().removeSurrounding("```json", "```").trim()
            val jsonArray = JSONArray(cleanJson)
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.optString("name", "Unknown Item")
                val category = obj.optString("category", "Cupboard")
                val expiryDays = obj.optInt("expiryDays", 7)
                val quantity = if (obj.isNull("quantity")) null else obj.getString("quantity")
                val price = obj.optDouble("price", 0.0)
                val calories = obj.optInt("calories", 0)
                val protein = obj.optDouble("protein", 0.0)
                
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, expiryDays)
                
                val newItem = Ingredient(
                    name = name,
                    category = category,
                    expiryDate = calendar.timeInMillis,
                    quantity = quantity,
                    price = price,
                    calories = calories,
                    protein = protein
                )
                repository.insertIngredient(newItem)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackParse(resultText)
        }
    }

    private suspend fun fallbackParse(resultText: String) {
        val regex = Regex("""\{\s*\"name\":\s*\"([^\"]+)\",\s*\"category\":\s*\"([^\"]+)\",\s*\"expiryDays\":\s*(\d+)(?:,\s*\"quantity\":\s*\"([^\"]+)\")?(?:,\s*\"price\":\s*([\d\.]+))?(?:,\s*\"calories\":\s*(\d+))?(?:,\s*\"protein\":\s*([\d\.]+))?\s*\}""")
        val matches = regex.findAll(resultText)
        
        matches.forEach { match ->
            val name = match.groups[1]?.value ?: "Unknown Item"
            val category = match.groups[2]?.value ?: "Cupboard"
            val expiryDays = match.groups[3]?.value?.toIntOrNull() ?: 7
            val quantity = match.groups[4]?.value
            val price = match.groups[5]?.value?.toDoubleOrNull()
            val calories = match.groups[6]?.value?.toIntOrNull()
            val protein = match.groups[7]?.value?.toDoubleOrNull()
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, expiryDays)
            
            val newItem = Ingredient(
                name = name,
                category = category,
                expiryDate = calendar.timeInMillis,
                quantity = quantity,
                price = price,
                calories = calories,
                protein = protein
            )
            repository.insertIngredient(newItem)
        }
    }

    fun addIngredient(name: String, category: String, expiryDays: Int, price: Double) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, expiryDays)
            val newItem = Ingredient(
                name = name,
                category = category,
                expiryDate = calendar.timeInMillis,
                price = price
            )
            repository.insertIngredient(newItem)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredient)
        }
    }

    fun markAsExpired(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.insertIngredient(ingredient.copy(isExpired = true))
        }
    }

    class Factory(private val repository: IngredientRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
