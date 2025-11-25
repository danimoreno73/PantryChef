package com.pantrychef.front.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertItem(
    val id: String,
    val productName: String,
    val message: String,
    val categoryInfo: String,
    val severity: com.pantrychef.front.components.BadgeSeverity,
    val actionLabel: String
)

data class RecipeItem(
    val id: String,
    val title: String,
    val prepTime: String,
    val servings: String,
    val imageUrl: String?,
    val badge: String?,
    val badgeSeverity: com.pantrychef.front.components.BadgeSeverity
)

data class ShoppingPreviewItem(
    val id: String,
    val name: String,
    val quantity: String,
    val source: String
)

data class HomeUiState(
    val userName: String = "",
    val cookableCount: Int = 0,
    val almostCookableCount: Int = 0,
    val alerts: List<AlertItem> = emptyList(),
    val cookableRecipes: List<RecipeItem> = emptyList(),
    val almostCookableRecipes: List<RecipeItem> = emptyList(),
    val shoppingPreview: List<ShoppingPreviewItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface HomeEvent {
    object Refresh : HomeEvent
    data class AlertClicked(val alertId: String) : HomeEvent
    data class RecipeClicked(val recipeId: String) : HomeEvent
    object AddToPantryClicked : HomeEvent
    object ScanClicked : HomeEvent
    object ViewAllAlertsClicked : HomeEvent
    object ViewAllRecipesClicked : HomeEvent
    object GoToShoppingListClicked : HomeEvent
    object SettingsClicked : HomeEvent
}

sealed interface HomeNavigation {
    data class ToProductDetail(val productId: String) : HomeNavigation
    data class ToRecipeDetail(val recipeId: String) : HomeNavigation
    object ToPantry : HomeNavigation
    object ToRecipes : HomeNavigation
    object ToShoppingList : HomeNavigation
    object ToSettings : HomeNavigation
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Inject repositories and use cases
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<HomeNavigation?>(null)
    val navigation: StateFlow<HomeNavigation?> = _navigation.asStateFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> loadDashboardData()

            is HomeEvent.AlertClicked -> {
                // TODO: Get productId from alert
                _navigation.value = HomeNavigation.ToProductDetail("product-123")
            }

            is HomeEvent.RecipeClicked -> {
                _navigation.value = HomeNavigation.ToRecipeDetail(event.recipeId)
            }

            HomeEvent.AddToPantryClicked -> {
                _navigation.value = HomeNavigation.ToPantry
            }

            HomeEvent.ScanClicked -> {
                // TODO: Implement scanner
            }

            HomeEvent.ViewAllAlertsClicked -> {
                // TODO: Navigate to alerts screen
            }

            HomeEvent.ViewAllRecipesClicked -> {
                _navigation.value = HomeNavigation.ToRecipes
            }

            HomeEvent.GoToShoppingListClicked -> {
                _navigation.value = HomeNavigation.ToShoppingList
            }

            HomeEvent.SettingsClicked -> {
                _navigation.value = HomeNavigation.ToSettings
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Call real use cases
            // Simulación con datos mock
            kotlinx.coroutines.delay(500)

            _uiState.update { it.copy(
                userName = "Usuario",
                cookableCount = 12,
                almostCookableCount = 7,
                alerts = getMockAlerts(),
                cookableRecipes = getMockCookableRecipes(),
                almostCookableRecipes = getMockAlmostCookableRecipes(),
                shoppingPreview = getMockShoppingPreview(),
                isLoading = false
            )}
        }
    }

    // Mock data
    private fun getMockAlerts() = listOf(
        AlertItem(
            id = "1",
            productName = "Leche",
            message = "Baja en 2 días",
            categoryInfo = "Lácteos",
            severity = com.pantrychef.front.components.BadgeSeverity.WARNING,
            actionLabel = "Reponer"
        ),
        AlertItem(
            id = "2",
            productName = "Huevos",
            message = "Quedan 3",
            categoryInfo = "Proteínas",
            severity = com.pantrychef.front.components.BadgeSeverity.LOW,
            actionLabel = "Bajo"
        ),
        AlertItem(
            id = "3",
            productName = "Espinaca",
            message = "Vence en 1 día",
            categoryInfo = "Verduras",
            severity = com.pantrychef.front.components.BadgeSeverity.URGENT,
            actionLabel = "Urgente"
        )
    )

    private fun getMockCookableRecipes() = listOf(
        RecipeItem(
            id = "1",
            title = "Pasta con verduras",
            prepTime = "25 min",
            servings = "2 porciones",
            imageUrl = null,
            badge = "Todo listo",
            badgeSeverity = com.pantrychef.front.components.BadgeSeverity.SUCCESS
        ),
        RecipeItem(
            id = "2",
            title = "Curry de garbanzos",
            prepTime = "30 min",
            servings = "3 porciones",
            imageUrl = null,
            badge = null,
            badgeSeverity = com.pantrychef.front.components.BadgeSeverity.SUCCESS
        )
    )

    private fun getMockAlmostCookableRecipes() = listOf(
        RecipeItem(
            id = "3",
            title = "Tacos de pollo",
            prepTime = "30 min",
            servings = "3 porciones",
            imageUrl = null,
            badge = "2/4",
            badgeSeverity = com.pantrychef.front.components.BadgeSeverity.WARNING
        ),
        RecipeItem(
            id = "4",
            title = "Risotto de setas",
            prepTime = "35 min",
            servings = "2 porciones",
            imageUrl = null,
            badge = "5/6",
            badgeSeverity = com.pantrychef.front.components.BadgeSeverity.WARNING
        )
    )

    private fun getMockShoppingPreview() = listOf(
        ShoppingPreviewItem(
            id = "1",
            name = "Tortillas",
            quantity = "12 uds",
            source = "Para Tacos de pollo"
        ),
        ShoppingPreviewItem(
            id = "2",
            name = "Leche",
            quantity = "2 L",
            source = "Bajo stock"
        )
    )

    fun clearNavigation() {
        _navigation.value = null
    }
}