package com.example.greenloop.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.IngredientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class SpendingTimelinePoint(
    val dateLabel: String,
    val totalAmount: Double,
    val timestamp: Long
)

class ProgressViewModel(
    private val historyRepository: HistoryRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    val history: StateFlow<List<UpcycleHistory>> = historyRepository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<Ingredient>> = ingredientRepository.allIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCo2Saved: StateFlow<Double> = historyRepository.totalCo2Saved
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPrice: StateFlow<Double> = inventoryItems.map { items ->
        items.sumOf { it.price ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val spendingTimeline: StateFlow<List<SpendingTimelinePoint>> = inventoryItems.map { items ->
        items.groupBy { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.addedDate
            "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
        }.map { (label, dayItems) ->
            SpendingTimelinePoint(
                dateLabel = label,
                totalAmount = dayItems.sumOf { it.price ?: 0.0 },
                timestamp = dayItems.first().addedDate
            )
        }.sortedBy { it.timestamp }.takeLast(7)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyGoal = 5.0 // kg of CO2

    class Factory(
        private val historyRepository: HistoryRepository,
        private val ingredientRepository: IngredientRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProgressViewModel(historyRepository, ingredientRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
