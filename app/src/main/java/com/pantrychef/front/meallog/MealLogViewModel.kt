package com.pantrychef.front.meallog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.front.components.MealStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealLogItemUiModel(
    val id: String,
    val mealName: String,
    val mealType: String,
    val time: String,
    val calories: Int,
    val imageUrl: String?,
    val status: MealStatus,
    val date: String // Para agrupación
)

enum class ViewMode {
    WEEK,
    MONTH
}

data class MealLogUiState(
    val weeklyMealsCount: Int = 0,
    val homemadeMealsCount: Int = 0,
    val mealsByDate: Map<String, List<MealLogItemUiModel>> = emptyMap(),
    val viewMode: ViewMode = ViewMode.WEEK,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface MealLogEvent {
    data class MealClicked(val mealId: String) : MealLogEvent
    data class ViewModeChanged(val mode: ViewMode) : MealLogEvent
    data class SearchQueryChanged(val query: String) : MealLogEvent
    object RegisterMealClicked : MealLogEvent
    object ViewStatsClicked : MealLogEvent
    object CalendarClicked : MealLogEvent
    object Refresh : MealLogEvent
}

sealed interface MealLogNavigation {
    object ToRegisterMeal : MealLogNavigation
    object ToStats : MealLogNavigation
}

@HiltViewModel
class MealLogViewModel @Inject constructor(
    // TODO: Inject MealLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealLogUiState())
    val uiState: StateFlow<MealLogUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<MealLogNavigation?>(null)
    val navigation: StateFlow<MealLogNavigation?> = _navigation.asStateFlow()

    init {
        loadMealLog()
    }

    fun onEvent(event: MealLogEvent) {
        when (event) {
            is MealLogEvent.MealClicked -> {
                // TODO: Navigate to meal detail
            }

            is MealLogEvent.ViewModeChanged -> {
                _uiState.update { it.copy(viewMode = event.mode) }
                // TODO: Reload data for different view mode
            }

            is MealLogEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterMeals(event.query)
            }

            MealLogEvent.RegisterMealClicked -> {
                _navigation.value = MealLogNavigation.ToRegisterMeal
            }

            MealLogEvent.ViewStatsClicked -> {
                _navigation.value = MealLogNavigation.ToStats
            }

            MealLogEvent.CalendarClicked -> {
                // TODO: Show calendar picker
            }

            MealLogEvent.Refresh -> {
                loadMealLog()
            }
        }
    }

    private fun filterMeals(query: String) {
        if (query.isBlank()) {
            // Si no hay búsqueda, mostrar todos
            loadMealLog()
            return
        }

        // Filtrar comidas por nombre o tipo
        val allMeals = _uiState.value.mealsByDate.values.flatten()
        val filtered = allMeals.filter { meal ->
            meal.mealName.contains(query, ignoreCase = true) ||
                    meal.mealType.contains(query, ignoreCase = true)
        }

        val groupedFiltered = filtered.groupBy { it.date }
        _uiState.update { it.copy(mealsByDate = groupedFiltered) }
    }

    private fun loadMealLog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Call repository
            kotlinx.coroutines.delay(300)

            val mockMeals = getMockMealLog()
            val groupedMeals = mockMeals.groupBy { it.date }

            _uiState.update { it.copy(
                weeklyMealsCount = mockMeals.size,
                homemadeMealsCount = mockMeals.count { it.status == MealStatus.COOKED || it.status == MealStatus.RECIPE },
                mealsByDate = groupedMeals,
                isLoading = false
            )}
        }
    }

    private fun getMockMealLog() = listOf(
        // Hoy
        MealLogItemUiModel(
            id = "1",
            mealName = "Avena con fruta",
            mealType = "Desayuno",
            time = "07:45",
            calories = 320,
            imageUrl = null,
            status = MealStatus.PLAN,
            date = "Hoy • Jue 14"
        ),
        MealLogItemUiModel(
            id = "2",
            mealName = "Tacos de pollo",
            mealType = "Almuerzo",
            time = "13:20",
            calories = 540,
            imageUrl = "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=200",
            status = MealStatus.RECIPE,
            date = "Hoy • Jue 14"
        ),

        // Ayer
        MealLogItemUiModel(
            id = "3",
            mealName = "Pasta con verduras",
            mealType = "Cena",
            time = "20:10",
            calories = 610,
            imageUrl = "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=200",
            status = MealStatus.COOKED,
            date = "Ayer • Mié 13"
        ),
        MealLogItemUiModel(
            id = "4",
            mealName = "Sopa de lentejas",
            mealType = "Comida",
            time = "14:05",
            calories = 430,
            imageUrl = "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=200",
            status = MealStatus.LEFTOVER,
            date = "Ayer • Mié 13"
        ),

        // Lun 11
        MealLogItemUiModel(
            id = "5",
            mealName = "Tortilla francesa",
            mealType = "Cena",
            time = "20:30",
            calories = 720,
            imageUrl = null,
            status = MealStatus.COOKED,
            date = "Lun 11"
        )
    )

    fun clearNavigation() {
        _navigation.value = null
    }
}