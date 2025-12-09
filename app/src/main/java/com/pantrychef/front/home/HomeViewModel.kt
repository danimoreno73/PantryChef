package com.pantrychef.front.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.usecase.GetAlmostCookableRecipesUseCase
import com.pantrychef.back.usecase.GetCookableRecipesUseCase
import com.pantrychef.back.usecase.GetLowStockProductsUseCase
import com.pantrychef.back.usecase.BuildSuggestedShoppingListUseCase
import com.pantrychef.back.utils.UnitsConverter
import com.pantrychef.front.components.BadgeSeverity
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
    val severity: BadgeSeverity,
    val actionLabel: String
)

data class RecipeItem(
    val id: String,
    val title: String,
    val prepTime: String,
    val servings: String,
    val imageUrl: String?,
    val badge: String?,
    val badgeSeverity: BadgeSeverity
)

data class ShoppingPreviewItem(
    val id: String,
    val name: String,
    val quantity: String,
    val source: String
)

data class HomeUiState(
    val userName: String = "Usuario",
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
    private val getLowStockProductsUseCase: GetLowStockProductsUseCase,
    private val getCookableRecipesUseCase: GetCookableRecipesUseCase,
    private val getAlmostCookableRecipesUseCase: GetAlmostCookableRecipesUseCase,
    private val buildSuggestedShoppingListUseCase: BuildSuggestedShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<HomeNavigation?>(null)
    val navigation: StateFlow<HomeNavigation?> = _navigation.asStateFlow()

    init {
        loadHomeData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> {
                loadHomeData()
            }

            is HomeEvent.AlertClicked -> {
                _navigation.value = HomeNavigation.ToProductDetail(event.alertId)
            }

            is HomeEvent.RecipeClicked -> {
                _navigation.value = HomeNavigation.ToRecipeDetail(event.recipeId)
            }

            HomeEvent.AddToPantryClicked -> {
                _navigation.value = HomeNavigation.ToPantry
            }

            HomeEvent.ScanClicked -> {
                // TODO: Implement scan functionality
            }

            HomeEvent.ViewAllAlertsClicked -> {
                _navigation.value = HomeNavigation.ToPantry
            }

            HomeEvent.ViewAllRecipesClicked -> {
                _navigation.value = HomeNavigation.ToRecipes
            }

            HomeEvent.GoToShoppingListClicked -> {
                generateShoppingListAndNavigate()
            }

            HomeEvent.SettingsClicked -> {
                _navigation.value = HomeNavigation.ToSettings
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Cargar productos con bajo stock
                launch {
                    getLowStockProductsUseCase().collect { lowStockProducts ->
                        updateWithLowStockProducts(lowStockProducts)
                    }
                }

                // Cargar recetas 100% cocinables
                launch {
                    getCookableRecipesUseCase().collect { cookableRecipes ->
                        updateWithCookableRecipes(cookableRecipes)
                    }
                }

                // Cargar recetas casi cocinables (80-99%)
                launch {
                    getAlmostCookableRecipesUseCase().collect { almostCookableRecipes ->
                        updateWithAlmostCookableRecipes(almostCookableRecipes)
                    }
                }

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar datos"
                )}
            }
        }
    }

    private fun updateWithLowStockProducts(products: List<Product>) {
        // Generar alertas de productos con bajo stock
        val alerts = products.map { product ->
            AlertItem(
                id = product.id,
                productName = product.name,
                message = when {
                    product.quantity == 0f -> "Sin stock"
                    product.quantity < product.lowStockThreshold * 0.5f -> "Stock crítico"
                    else -> "Stock bajo"
                },
                categoryInfo = "${product.category.name} • ${product.location ?: "Sin ubicación"}",
                severity = when {
                    product.quantity == 0f -> BadgeSeverity.CRITICAL
                    product.quantity < product.lowStockThreshold * 0.5f -> BadgeSeverity.URGENT
                    else -> BadgeSeverity.WARNING
                },
                actionLabel = "Reponer"
            )
        }.take(3)

        val shoppingPreview = products.map { product ->
            val quantityNeeded = (product.lowStockThreshold - product.quantity).coerceAtLeast(0f)
            ShoppingPreviewItem(
                id = product.id,
                name = product.name,
                quantity = UnitsConverter.formatQuantityShort(quantityNeeded, product.unit),
                source = "Sugerido"
            )
        }.take(2)

        _uiState.update { it.copy(
            alerts = alerts,
            shoppingPreview = shoppingPreview
        )}
    }

    private fun updateWithCookableRecipes(recipes: List<Recipe>) {
        val cookableRecipes = recipes.map { recipe ->
            RecipeItem(
                id = recipe.id,
                title = recipe.name,
                prepTime = "${recipe.prepTimeMinutes} min",
                servings = "${recipe.servings} porciones",
                imageUrl = recipe.imageUrl,
                badge = "Todo listo",
                badgeSeverity = BadgeSeverity.SUCCESS
            )
        }.take(2)

        _uiState.update { it.copy(
            cookableCount = recipes.size,
            cookableRecipes = cookableRecipes
        )}
    }

    private fun updateWithAlmostCookableRecipes(almostCookableList: List<GetAlmostCookableRecipesUseCase.AlmostCookableRecipe>) {
        val almostCookableRecipes = almostCookableList.map { almostCookableRecipe ->
            val recipe = almostCookableRecipe.recipe
            val missingCount = almostCookableRecipe.missingIngredients.size
            val totalCount = recipe.ingredients.size

            RecipeItem(
                id = recipe.id,
                title = recipe.name,
                prepTime = "${recipe.prepTimeMinutes} min",
                servings = "${recipe.servings} porciones",
                imageUrl = recipe.imageUrl,
                badge = "${totalCount - missingCount}/$totalCount",
                badgeSeverity = BadgeSeverity.WARNING
            )
        }.take(2)

        _uiState.update { it.copy(
            almostCookableCount = almostCookableList.size,
            almostCookableRecipes = almostCookableRecipes
        )}
    }

    fun clearNavigation() {
        _navigation.value = null
    }

    private fun generateShoppingListAndNavigate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = buildSuggestedShoppingListUseCase()

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigation.value = HomeNavigation.ToShoppingList
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al generar lista"
                    )}
                    // Navegar de todas formas aunque falle
                    _navigation.value = HomeNavigation.ToShoppingList
                }
            )
        }
    }
}