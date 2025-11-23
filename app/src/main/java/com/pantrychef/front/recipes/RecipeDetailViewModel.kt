package com.pantrychef.front.recipes

import androidx.lifecycle.SavedStateHandle
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

data class IngredientItemUiModel(
    val id: String,
    val name: String,
    val quantity: String,
    val isAvailable: Boolean,
    val isChecked: Boolean = false
)

data class RecipeStepUiModel(
    val number: Int,
    val instruction: String,
    val duration: Int? = null
)

data class RecipeDetailUiState(
    val recipeTitle: String = "",
    val imageUrl: String? = null,
    val prepTime: Int = 0,
    val servings: Int = 2,
    val difficulty: String = "",
    val availabilityStatus: AvailabilityStatus = AvailabilityStatus.FULL,
    val ingredients: List<IngredientItemUiModel> = emptyList(),
    val steps: List<RecipeStepUiModel> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class AvailabilityStatus {
    FULL,
    PARTIAL
}

sealed interface RecipeDetailEvent {
    data class IngredientChecked(val ingredientId: String, val checked: Boolean) : RecipeDetailEvent
    data class ServingsChanged(val servings: Int) : RecipeDetailEvent
    object CookNowClicked : RecipeDetailEvent
    object AddMissingToShoppingList : RecipeDetailEvent
    object SubstituteIngredients : RecipeDetailEvent
    object ToggleFavorite : RecipeDetailEvent
    object RegisterMeal : RecipeDetailEvent
}

sealed interface RecipeDetailNavigation {
    object ToShoppingList : RecipeDetailNavigation
    object ToMealLog : RecipeDetailNavigation
    object Back : RecipeDetailNavigation
}

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
    // TODO: Inject RecipeRepository
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<RecipeDetailNavigation?>(null)
    val navigation: StateFlow<RecipeDetailNavigation?> = _navigation.asStateFlow()

    init {
        loadRecipeDetail()
    }

    fun onEvent(event: RecipeDetailEvent) {
        when (event) {
            is RecipeDetailEvent.IngredientChecked -> {
                val updatedIngredients = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.ingredientId) {
                        ingredient.copy(isChecked = event.checked)
                    } else {
                        ingredient
                    }
                }
                _uiState.update { it.copy(ingredients = updatedIngredients) }
            }

            is RecipeDetailEvent.ServingsChanged -> {
                _uiState.update { it.copy(servings = event.servings) }
                // TODO: Recalcular cantidades de ingredientes
            }

            RecipeDetailEvent.CookNowClicked -> {
                // TODO: Implement cook logic - reduce pantry stock
                _navigation.value = RecipeDetailNavigation.ToMealLog
            }

            RecipeDetailEvent.AddMissingToShoppingList -> {
                // TODO: Add missing ingredients to shopping list
                _navigation.value = RecipeDetailNavigation.ToShoppingList
            }

            RecipeDetailEvent.SubstituteIngredients -> {
                // TODO: Show substitute suggestions
            }

            RecipeDetailEvent.ToggleFavorite -> {
                _uiState.update { it.copy(isFavorite = !it.isFavorite) }
                // TODO: Persist to repository
            }

            RecipeDetailEvent.RegisterMeal -> {
                // TODO: Register meal in log
                _navigation.value = RecipeDetailNavigation.ToMealLog
            }
        }
    }

    private fun loadRecipeDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Call repository
            kotlinx.coroutines.delay(300)

            // Mock data - Simula receta según ID
            val isFullyAvailable = recipeId == "1" || recipeId == "2"

            _uiState.update { it.copy(
                recipeTitle = if (isFullyAvailable) "Pasta con verduras" else "Tacos de pollo",
                imageUrl = if (isFullyAvailable)
                    "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=600"
                else
                    "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=600",
                prepTime = if (isFullyAvailable) 25 else 30,
                servings = if (isFullyAvailable) 2 else 3,
                difficulty = if (isFullyAvailable) "Fácil" else "Media",
                availabilityStatus = if (isFullyAvailable)
                    AvailabilityStatus.FULL
                else
                    AvailabilityStatus.PARTIAL,
                ingredients = if (isFullyAvailable) getMockIngredientsFullyAvailable() else getMockIngredientsPartial(),
                steps = if (isFullyAvailable) getMockStepsPasta() else getMockStepsTacos(),
                missingIngredients = if (isFullyAvailable) emptyList() else listOf("Tortillas", "Lima"),
                isFavorite = false,
                isLoading = false
            )}
        }
    }

    private fun getMockIngredientsFullyAvailable() = listOf(
        IngredientItemUiModel(
            id = "1",
            name = "Pasta corta",
            quantity = "180 g",
            isAvailable = true
        ),
        IngredientItemUiModel(
            id = "2",
            name = "Calabacín",
            quantity = "1 mediano",
            isAvailable = true
        ),
        IngredientItemUiModel(
            id = "3",
            name = "Pimiento",
            quantity = "1 pequeño",
            isAvailable = true
        ),
        IngredientItemUiModel(
            id = "4",
            name = "Aceite de oliva",
            quantity = "1 cda",
            isAvailable = true
        )
    )

    private fun getMockIngredientsPartial() = listOf(
        IngredientItemUiModel(
            id = "1",
            name = "Pollo desmenuzado",
            quantity = "300 g",
            isAvailable = true
        ),
        IngredientItemUiModel(
            id = "2",
            name = "Cebolla",
            quantity = "1 mediana",
            isAvailable = true
        ),
        IngredientItemUiModel(
            id = "3",
            name = "Tortillas",
            quantity = "12 uds",
            isAvailable = false
        ),
        IngredientItemUiModel(
            id = "4",
            name = "Lima",
            quantity = "1 unidad",
            isAvailable = false
        )
    )

    private fun getMockStepsPasta() = listOf(
        RecipeStepUiModel(
            number = 1,
            instruction = "Hervir la pasta en agua con sal hasta al dente. Reserva 1/4 taza del agua.",
            duration = 10
        ),
        RecipeStepUiModel(
            number = 2,
            instruction = "Saltear verduras (calabacín y pimiento) en aceite de oliva 4-5 min, sal y pimienta.",
            duration = 5
        ),
        RecipeStepUiModel(
            number = 3,
            instruction = "Mezclar pasta escurrida con verduras. Ajustar condimentos."
        ),
        RecipeStepUiModel(
            number = 4,
            instruction = "Servir y terminar con aceite de oliva o queso si tienes."
        )
    )

    private fun getMockStepsTacos() = listOf(
        RecipeStepUiModel(
            number = 1,
            instruction = "Preparar relleno: saltear cebolla y agregar pollo; condimentar al gusto.",
            duration = 15
        ),
        RecipeStepUiModel(
            number = 2,
            instruction = "Calentar tortillas en plancha o sartén 30-60 s por lado.",
            duration = 5
        ),
        RecipeStepUiModel(
            number = 3,
            instruction = "Montar tacos: rellenar con pollo; terminar con jugo de lima."
        )
    )

    fun clearNavigation() {
        _navigation.value = null
    }
}