package com.pantrychef.front.meallog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.MealLog
import com.pantrychef.back.model.enums.MealType
import com.pantrychef.back.repository.MealLogRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.usecase.RegisterMealUseCase
import com.pantrychef.front.components.MealStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ViewMode {
    WEEK,
    MONTH
}

data class MealUiModel(
    val id: String,
    val mealName: String,
    val mealType: String,
    val time: String,
    val calories: Int,
    val imageUrl: String?,
    val status: MealStatus
)

data class MealLogUiState(
    val mealsByDate: Map<String, List<MealUiModel>> = emptyMap(),
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.WEEK,
    val weeklyMealsCount: Int = 0,
    val homemadeMealsCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface MealLogEvent {
    data class SearchQueryChanged(val query: String) : MealLogEvent
    data class ViewModeChanged(val mode: ViewMode) : MealLogEvent
    object CalendarClicked : MealLogEvent
    object RegisterMealClicked : MealLogEvent
    data class MealClicked(val mealId: String) : MealLogEvent
    object ViewStatsClicked : MealLogEvent
}

sealed interface MealLogNavigation {
    object ToRegisterMeal : MealLogNavigation
    object ToStats : MealLogNavigation
}

@HiltViewModel
class MealLogViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val recipeRepository: RecipeRepository,
    private val registerMealUseCase: RegisterMealUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealLogUiState())
    val uiState: StateFlow<MealLogUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<MealLogNavigation?>(null)
    val navigation: StateFlow<MealLogNavigation?> = _navigation.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var allMealLogs: List<MealLog> = emptyList()

    init {
        loadMealLogs()
    }

    fun onEvent(event: MealLogEvent) {
        when (event) {
            is MealLogEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterAndGroupMeals()
            }

            is MealLogEvent.ViewModeChanged -> {
                _uiState.update { it.copy(viewMode = event.mode) }
                loadMealLogs()
            }

            MealLogEvent.CalendarClicked -> {
                // TODO: Show calendar picker
            }

            MealLogEvent.RegisterMealClicked -> {
                _navigation.value = MealLogNavigation.ToRegisterMeal
            }

            is MealLogEvent.MealClicked -> {
                // TODO: Navigate to meal detail or show edit dialog
            }

            MealLogEvent.ViewStatsClicked -> {
                _navigation.value = MealLogNavigation.ToStats
            }
        }
    }

    private fun loadMealLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val (startDate, endDate) = getDateRange()

                mealLogRepository.getMealHistory(startDate, endDate).collect { logs ->
                    allMealLogs = logs

                    val weeklyCount = logs.size
                    val homemadeCount = logs.size // TODO: Filter by isHomemade flag when added

                    _uiState.update { it.copy(
                        weeklyMealsCount = weeklyCount,
                        homemadeMealsCount = homemadeCount,
                        isLoading = false
                    )}

                    filterAndGroupMeals()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar historial"
                )}
            }
        }
    }

    private fun filterAndGroupMeals() {
        val query = _uiState.value.searchQuery

        // Filtrar por búsqueda
        val filteredLogs = if (query.isBlank()) {
            allMealLogs
        } else {
            allMealLogs.filter {
                it.recipeName.contains(query, ignoreCase = true)
            }
        }

        // Agrupar por fecha
        val grouped = filteredLogs
            .sortedByDescending { it.timestamp }
            .groupBy { dateFormatter.format(Date(it.timestamp)) }
            .mapValues { (_, meals) ->
                meals.map { mapToUiModel(it) }
            }

        _uiState.update { it.copy(mealsByDate = grouped) }
    }

    private fun mapToUiModel(mealLog: MealLog): MealUiModel {
        return MealUiModel(
            id = mealLog.id,
            mealName = mealLog.recipeName,
            mealType = when (mealLog.mealType) {
                MealType.BREAKFAST -> "Desayuno"
                MealType.LUNCH -> "Comida"
                MealType.DINNER -> "Cena"
                MealType.SNACK -> "Merienda"
            },
            time = timeFormatter.format(Date(mealLog.timestamp)),
            calories = mealLog.caloriesEstimate ?: 0,
            imageUrl = null, // TODO: Get from recipe
            status = MealStatus.COOKED  // ✅ COOKED en lugar de LOGGED
        )
    }

    private fun getDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startDate = when (_uiState.value.viewMode) {
            ViewMode.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            ViewMode.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
        }

        return startDate to endDate
    }

    fun logMealFromRecipe(
        recipeId: String,
        recipeName: String,
        mealType: MealType,
        servings: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = registerMealUseCase(
                recipeId = recipeId,
                recipeName = recipeName,
                mealType = mealType,
                servings = servings,
                caloriesEstimate = null
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    // El Flow de loadMealLogs() se actualizará automáticamente
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al registrar comida"
                    )}
                }
            )
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}