package com.pantrychef.front.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.front.components.BadgeSeverity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeCardData(
    val id: String,
    val title: String,
    val prepTime: String,
    val servings: String,
    val imageUrl: String?,
    val badge: String?,
    val badgeSeverity: BadgeSeverity
)

data class RecipesUiState(
    val cookableNow: List<RecipeCardData> = emptyList(),
    val almostCookable: List<RecipeCardData> = emptyList(),
    val yourRecipes: List<RecipeCardData> = emptyList(),
    val searchQuery: String = "",
    val activeTab: RecipeTab = RecipeTab.COOKABLE,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class RecipeTab {
    COOKABLE,      // Cocinables
    ALMOST,        // Casi
    UNDER_30MIN,   // < 30 min (filtro adicional)
    YOUR_RECIPES   // Tus recetas
}

sealed interface RecipesEvent {
    data class SearchQueryChanged(val query: String) : RecipesEvent
    data class TabChanged(val tab: RecipeTab) : RecipesEvent
    data class RecipeClicked(val recipeId: String) : RecipesEvent
    object CreateRecipeClicked : RecipesEvent
    object Refresh : RecipesEvent
}

sealed interface RecipesNavigation {
    data class ToRecipeDetail(val recipeId: String) : RecipesNavigation
    object ToCreateRecipe : RecipesNavigation
}

@HiltViewModel
class RecipesViewModel @Inject constructor(
    // TODO: Inject RecipeRepository, GetCookableRecipesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<RecipesNavigation?>(null)
    val navigation: StateFlow<RecipesNavigation?> = _navigation.asStateFlow()

    init {
        loadRecipes()
    }

    fun onEvent(event: RecipesEvent) {
        when (event) {
            is RecipesEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                // TODO: Filtrar recetas
            }

            is RecipesEvent.TabChanged -> {
                _uiState.update { it.copy(activeTab = event.tab) }
            }

            is RecipesEvent.RecipeClicked -> {
                _navigation.value = RecipesNavigation.ToRecipeDetail(event.recipeId)
            }

            RecipesEvent.CreateRecipeClicked -> {
                _navigation.value = RecipesNavigation.ToCreateRecipe
            }

            RecipesEvent.Refresh -> {
                loadRecipes()
            }
        }
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Call real repositories
            kotlinx.coroutines.delay(300)

            _uiState.update { it.copy(
                cookableNow = getMockCookableRecipes(),
                almostCookable = getMockAlmostCookableRecipes(),
                yourRecipes = getMockYourRecipes(),
                isLoading = false
            )}
        }
    }

    // Mock data
    private fun getMockCookableRecipes() = listOf(
        RecipeCardData(
            id = "1",
            title = "Pasta con verduras",
            prepTime = "25 min",
            servings = "2 porciones",
            imageUrl = "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=300",
            badge = "Todo listo",
            badgeSeverity = BadgeSeverity.SUCCESS
        ),
        RecipeCardData(
            id = "2",
            title = "Curry de garbanzos",
            prepTime = "30 min",
            servings = "3 porciones",
            imageUrl = "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=300",
            badge = "Todo listo",
            badgeSeverity = BadgeSeverity.SUCCESS
        )
    )

    private fun getMockAlmostCookableRecipes() = listOf(
        RecipeCardData(
            id = "3",
            title = "Hamburguesa endentro colesterol",
            prepTime = "30 min",
            servings = "3 porciones",
            imageUrl = "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=300",
            badge = "2/4",
            badgeSeverity = BadgeSeverity.WARNING
        ),
        RecipeCardData(
            id = "4",
            title = "Risotto de setas",
            prepTime = "35 min",
            servings = "2 porciones",
            imageUrl = "https://plus.unsplash.com/premium_photo-1694850980302-f568e6de0f6d?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            badge = "5/6",
            badgeSeverity = BadgeSeverity.WARNING
        )
    )

    private fun getMockYourRecipes() = listOf(
        RecipeCardData(
            id = "5",
            title = "Pizza casera",
            prepTime = "45 min",
            servings = "4 porciones",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=300",
            badge = "Guardada",
            badgeSeverity = BadgeSeverity.INFO
        ),
        RecipeCardData(
            id = "6",
            title = "Sopa de lentejas",
            prepTime = "45 min",
            servings = "3 porciones",
            imageUrl = "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=300",
            badge = "Guardada",
            badgeSeverity = BadgeSeverity.INFO
        )
    )

    fun clearNavigation() {
        _navigation.value = null
    }
}