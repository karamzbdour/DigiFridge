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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    val inventoryItems: StateFlow<List<Ingredient>> = repository.allIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPrice: StateFlow<Double> = inventoryItems.map { items ->
        items.sumOf { (it.price ?: 0.0) * (extractQuantity(it.quantity)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private fun extractQuantity(quantityStr: String?): Int {
        if (quantityStr == null) return 1
        return try {
            quantityStr.removePrefix("x").toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    fun scanImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val resultText = OpenRouterManager.analyzeImage(bitmap)
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
            val root = JSONObject(cleanJson)
            val metadata = root.optJSONObject("metadata")
            val itemsArray = root.getJSONArray("items")
            
            // Parse shop date if available, otherwise use current time
            val shopDateStr = metadata?.optString("shop_date", "")
            val shopDateLong = if (!shopDateStr.isNullOrEmpty()) {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(shopDateStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) { System.currentTimeMillis() }
            } else {
                System.currentTimeMillis()
            }
            
            for (i in 0 until itemsArray.length()) {
                val obj = itemsArray.getJSONObject(i)
                val name = obj.optString("human_readable_name", "Unknown Item")
                val price = obj.optDouble("price", 0.0)
                val category = obj.optString("category", "Uncategorized")
                val daysToExpiry = if (obj.isNull("days_to_expiry")) 7 else obj.getInt("days_to_expiry")
                
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = shopDateLong
                calendar.add(Calendar.DAY_OF_YEAR, daysToExpiry)
                
                val existingIngredient = repository.getIngredientByNameAndCategory(name, category)
                if (existingIngredient != null) {
                    val currentQty = extractQuantity(existingIngredient.quantity)
                    val updatedIngredient = existingIngredient.copy(
                        quantity = "x${currentQty + 1}",
                        expiryDate = calendar.timeInMillis // Update to latest expiry if needed, or keep oldest? Usually latest is safer for fresh stock.
                    )
                    repository.updateIngredient(updatedIngredient)
                } else {
                    val newItem = Ingredient(
                        name = name,
                        category = category,
                        expiryDate = calendar.timeInMillis,
                        addedDate = shopDateLong,
                        price = price,
                        quantity = "x1"
                    )
                    repository.insertIngredient(newItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addIngredient(name: String, category: String, expiryDays: Int, price: Double) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, expiryDays)
            
            val existingIngredient = repository.getIngredientByNameAndCategory(name, category)
            if (existingIngredient != null) {
                val currentQty = extractQuantity(existingIngredient.quantity)
                val updatedIngredient = existingIngredient.copy(
                    quantity = "x${currentQty + 1}",
                    expiryDate = calendar.timeInMillis
                )
                repository.updateIngredient(updatedIngredient)
            } else {
                val newItem = Ingredient(
                    name = name,
                    category = category,
                    expiryDate = calendar.timeInMillis,
                    price = price,
                    quantity = "x1"
                )
                repository.insertIngredient(newItem)
            }
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
