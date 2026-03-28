package com.example.greenloop.ui.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.api.GeminiManager
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.repository.IngredientRepository
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    val inventoryItems: StateFlow<List<Ingredient>> = repository.allIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scanImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val prompt = "Identify the food items on this receipt or in this image. For each item, estimate its category (Dairy, Vegetables, Fruits, Meat, Cupboard) and likely expiry date (in days from now). Format the response as a JSON list: [{\"name\": \"Milk\", \"category\": \"Dairy\", \"expiryDays\": 7, \"quantity\": \"1L\"}]"
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }
                val response = GeminiManager.model.generateContent(inputContent)
                val resultText = response.text
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
        
        // Use regex for robust extraction of JSON objects from the AI response
        val regex = Regex("""\{\s*\"name\":\s*\"([^\"]+)\",\s*\"category\":\s*\"([^\"]+)\",\s*\"expiryDays\":\s*(\d+)(?:,\s*\"quantity\":\s*\"([^\"]+)\")?\s*\}""")
        val matches = regex.findAll(resultText)
        
        matches.forEach { match ->
            val name = match.groups[1]?.value ?: "Unknown Item"
            val category = match.groups[2]?.value ?: "Cupboard"
            val expiryDays = match.groups[3]?.value?.toIntOrNull() ?: 7
            val quantity = match.groups[4]?.value
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, expiryDays)
            
            val newItem = Ingredient(
                name = name,
                category = category,
                expiryDate = calendar.timeInMillis,
                quantity = quantity
            )
            repository.insertIngredient(newItem)
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
