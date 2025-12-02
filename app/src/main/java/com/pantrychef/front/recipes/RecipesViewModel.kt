package com.pantrychef.front.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.usecase.GetAlmostCookableRecipesUseCase
import com.pantrychef.back.usecase.GetCookableRecipesUseCase
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
    val badgeSeverity: BadgeSeverity?
)

enum class RecipeTab {
    COOKABLE,
    ALMOST,
    UNDER_30MIN,
    YOUR_RECIPES
}

data class RecipesUiState(
    val cookableNow: List<RecipeCardData> = emptyList(),
    val almostCookable: List<RecipeCardData> = emptyList(),
    val under30Min: List<RecipeCardData> = emptyList(),
    val yourRecipes: List<RecipeCardData> = emptyList(),
    val searchQuery: String = "",
    val activeTab: RecipeTab = RecipeTab.COOKABLE,
    val isLoading: Boolean = false,
    val error: String? = null
)

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
    private val recipeRepository: RecipeRepository,
    private val getCookableRecipesUseCase: GetCookableRecipesUseCase,
    private val getAlmostCookableRecipesUseCase: GetAlmostCookableRecipesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<RecipesNavigation?>(null)
    val navigation: StateFlow<RecipesNavigation?> = _navigation.asStateFlow()

    // Guardar listas originales para búsqueda local
    private var originalCookable: List<RecipeCardData> = emptyList()
    private var originalAlmost: List<RecipeCardData> = emptyList()
    private var originalUnder30: List<RecipeCardData> = emptyList()
    private var originalYourRecipes: List<RecipeCardData> = emptyList()

    init {
        loadRecipes()
    }

    fun onEvent(event: RecipesEvent) {
        when (event) {
            is RecipesEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                applySearch(event.query)
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

            try {
                // Cargar recetas 100% cocinables
                launch {
                    getCookableRecipesUseCase().collect { cookableRecipes ->
                        val cookableCards = cookableRecipes.map { recipe ->
                            RecipeCardData(
                                id = recipe.id,
                                title = recipe.name,
                                prepTime = "${recipe.prepTimeMinutes} min",
                                servings = "${recipe.servings} porciones",
                                imageUrl = recipe.imageUrl,
                                badge = "Todo listo",
                                badgeSeverity = BadgeSeverity.SUCCESS
                            )
                        }

                        originalCookable = cookableCards
                        _uiState.update { it.copy(cookableNow = cookableCards) }
                    }
                }

                // Cargar recetas casi cocinables (80-99%)
                launch {
                    getAlmostCookableRecipesUseCase().collect { almostCookableRecipes ->
                        val almostCards = almostCookableRecipes.map { almostCookableRecipe ->
                            val recipe = almostCookableRecipe.recipe
                            val missingCount = almostCookableRecipe.missingIngredients.size
                            val totalCount = recipe.ingredients.size

                            RecipeCardData(
                                id = recipe.id,
                                title = recipe.name,
                                prepTime = "${recipe.prepTimeMinutes} min",
                                servings = "${recipe.servings} porciones",
                                imageUrl = recipe.imageUrl,
                                badge = "${totalCount - missingCount}/$totalCount",
                                badgeSeverity = BadgeSeverity.WARNING
                            )
                        }

                        originalAlmost = almostCards
                        _uiState.update { it.copy(almostCookable = almostCards) }
                    }
                }

                // Cargar todas las recetas para filtros adicionales
                launch {
                    recipeRepository.getAllRecipes().collect { allRecipes ->
                        val under30 = allRecipes.filter { it.prepTimeMinutes <= 30 }
                        val under30Cards = under30.map { recipe ->
                            RecipeCardData(
                                id = recipe.id,
                                title = recipe.name,
                                prepTime = "${recipe.prepTimeMinutes} min",
                                servings = "${recipe.servings} porciones",
                                imageUrl = recipe.imageUrl,
                                badge = "Rápida",
                                badgeSeverity = BadgeSeverity.INFO
                            )
                        }

                        // TODO: Filtrar "Tus recetas" por userId cuando tengamos auth
                        val yourRecipesCards = allRecipes.filter { !it.isPublic }.map { recipe ->
                            RecipeCardData(
                                id = recipe.id,
                                title = recipe.name,
                                prepTime = "${recipe.prepTimeMinutes} min",
                                servings = "${recipe.servings} porciones",
                                imageUrl = recipe.imageUrl,
                                badge = null,
                                badgeSeverity = null
                            )
                        }

                        originalUnder30 = under30Cards
                        originalYourRecipes = yourRecipesCards

                        _uiState.update { it.copy(
                            under30Min = under30Cards,
                            yourRecipes = yourRecipesCards
                        )}
                    }
                }

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar recetas"
                )}
            }
        }
    }

    private fun applySearch(query: String) {
        if (query.isBlank()) {
            // Restaurar listas originales
            _uiState.update { it.copy(
                cookableNow = originalCookable,
                almostCookable = originalAlmost,
                under30Min = originalUnder30,
                yourRecipes = originalYourRecipes
            )}
            return
        }

        // Filtrar en memoria
        val filteredCookable = originalCookable.filter {
            it.title.contains(query, ignoreCase = true)
        }
        val filteredAlmost = originalAlmost.filter {
            it.title.contains(query, ignoreCase = true)
        }
        val filteredUnder30 = originalUnder30.filter {
            it.title.contains(query, ignoreCase = true)
        }
        val filteredYourRecipes = originalYourRecipes.filter {
            it.title.contains(query, ignoreCase = true)
        }

        _uiState.update { it.copy(
            cookableNow = filteredCookable,
            almostCookable = filteredAlmost,
            under30Min = filteredUnder30,
            yourRecipes = filteredYourRecipes
        )}
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}