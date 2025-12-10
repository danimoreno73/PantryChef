package com.pantrychef.front.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.repository.AuthRepository
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.utils.UnitsConverter
import com.pantrychef.back.usecase.DeleteRecipeUseCase
import com.pantrychef.back.usecase.RegisterMealUseCase
import com.pantrychef.back.model.enums.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AvailabilityStatus {
    FULL,
    PARTIAL
}

data class IngredientItemUiModel(
    val id: String,
    val name: String,
    val quantity: String,
    val isAvailable: Boolean,
    val isChecked: Boolean
)

data class RecipeStepUiModel(
    val number: Int,
    val instruction: String,
    val duration: Int? = null
)

data class RecipeDetailUiState(
    val recipeId: String = "",
    val recipeTitle: String = "",
    val imageUrl: String? = null,
    val prepTime: Int = 0,
    val servings: Int = 0,
    val difficulty: String = "",
    val ingredients: List<IngredientItemUiModel> = emptyList(),
    val steps: List<RecipeStepUiModel> = emptyList(),
    val availabilityStatus: AvailabilityStatus = AvailabilityStatus.PARTIAL,
    val missingIngredients: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isUserRecipe: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showMealTypeDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface RecipeDetailEvent {
    object ToggleFavorite : RecipeDetailEvent
    data class IngredientChecked(val ingredientId: String, val isChecked: Boolean) : RecipeDetailEvent
    object CookNowClicked : RecipeDetailEvent
    object RegisterMeal : RecipeDetailEvent
    data class ConfirmMealType(val mealType: MealType) : RecipeDetailEvent
    object DismissMealTypeDialog : RecipeDetailEvent
    object AddMissingToShoppingList : RecipeDetailEvent
    object SubstituteIngredients : RecipeDetailEvent
    object EditRecipeClicked : RecipeDetailEvent
    object DeleteRecipeClicked : RecipeDetailEvent
    object ConfirmDelete : RecipeDetailEvent
    object DismissDeleteDialog : RecipeDetailEvent
}

sealed interface RecipeDetailNavigation {
    object ToMealLog : RecipeDetailNavigation
    object ToShoppingList : RecipeDetailNavigation
    data class ToEditRecipe(val recipeId: String) : RecipeDetailNavigation
    object Back : RecipeDetailNavigation
}

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val registerMealUseCase: RegisterMealUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
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
            RecipeDetailEvent.ToggleFavorite -> {
                _uiState.update { it.copy(isFavorite = !it.isFavorite) }
            }

            is RecipeDetailEvent.IngredientChecked -> {
                val updatedIngredients = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.ingredientId) {
                        ingredient.copy(isChecked = event.isChecked)
                    } else {
                        ingredient
                    }
                }
                _uiState.update { it.copy(ingredients = updatedIngredients) }
            }

            RecipeDetailEvent.CookNowClicked -> {
                _uiState.update { it.copy(showMealTypeDialog = true) }
            }

            RecipeDetailEvent.RegisterMeal -> {
                _uiState.update { it.copy(showMealTypeDialog = true) }
            }

            is RecipeDetailEvent.ConfirmMealType -> {
                registerMealWithType(event.mealType)
            }

            RecipeDetailEvent.DismissMealTypeDialog -> {
                _uiState.update { it.copy(showMealTypeDialog = false) }
            }

            RecipeDetailEvent.AddMissingToShoppingList -> {
                _navigation.value = RecipeDetailNavigation.ToShoppingList
            }

            RecipeDetailEvent.SubstituteIngredients -> {
                // TODO: Implement substitute ingredients
            }

            RecipeDetailEvent.EditRecipeClicked -> {
                _navigation.value = RecipeDetailNavigation.ToEditRecipe(recipeId)
            }

            RecipeDetailEvent.DeleteRecipeClicked -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }

            RecipeDetailEvent.ConfirmDelete -> {
                deleteRecipe()
            }

            RecipeDetailEvent.DismissDeleteDialog -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
            }
        }
    }

    private fun loadRecipeDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Obtener usuario actual
                val currentUserResult = authRepository.getCurrentUser()
                val currentUserId = currentUserResult.getOrNull()?.id

                // Cargar receta
                val recipeResult = recipeRepository.getRecipeById(recipeId)

                recipeResult.fold(
                    onSuccess = { recipe ->
                        // Verificar si es receta del usuario
                        val isUserRecipe = !recipe.isPublic || recipe.createdBy == currentUserId

                        // Cargar productos para verificar disponibilidad
                        val products = productRepository.getAllProducts().first()

                        // Mapear ingredientes con disponibilidad
                        val ingredientItems = recipe.ingredients.map { ingredient ->
                            val product = products.find {
                                it.name.trim().equals(ingredient.productName.trim(), ignoreCase = true)
                            }

                            // Usar UnitsConverter para verificar disponibilidad con conversión de unidades
                            val isAvailable = product != null && UnitsConverter.hasSufficientQuantity(
                                productQuantity = product.quantity,
                                productUnit = product.unit,
                                requiredQuantity = ingredient.quantity,
                                requiredUnit = ingredient.unit
                            )

                            IngredientItemUiModel(
                                id = ingredient.id,
                                name = ingredient.productName,
                                quantity = UnitsConverter.formatQuantityShort(ingredient.quantity, ingredient.unit),
                                isAvailable = isAvailable,
                                isChecked = false
                            )
                        }

                        // Calcular ingredientes faltantes
                        val missingIngredients = ingredientItems
                            .filter { !it.isAvailable }
                            .map { it.name }

                        // Determinar disponibilidad
                        val availabilityStatus = if (missingIngredients.isEmpty()) {
                            AvailabilityStatus.FULL
                        } else {
                            AvailabilityStatus.PARTIAL
                        }

                        // Mapear pasos
                        val steps = recipe.steps.mapIndexed { index, instruction ->
                            RecipeStepUiModel(
                                number = index + 1,
                                instruction = instruction,
                                duration = null
                            )
                        }

                        _uiState.update { it.copy(
                            recipeId = recipe.id,
                            recipeTitle = recipe.name,
                            imageUrl = recipe.imageUrl,
                            prepTime = recipe.prepTimeMinutes,
                            servings = recipe.servings,
                            difficulty = when (recipe.difficulty.name) {
                                "EASY" -> "Fácil"
                                "MEDIUM" -> "Media"
                                "HARD" -> "Difícil"
                                else -> recipe.difficulty.name
                            },
                            ingredients = ingredientItems,
                            steps = steps,
                            availabilityStatus = availabilityStatus,
                            missingIngredients = missingIngredients,
                            isFavorite = false,
                            isUserRecipe = isUserRecipe,
                            isLoading = false
                        )}
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al cargar receta"
                        )}
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar receta"
                )}
            }
        }
    }

    private fun deleteRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                showDeleteDialog = false,
                error = null
            )}

            val result = deleteRecipeUseCase(recipeId)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigation.value = RecipeDetailNavigation.Back
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al eliminar receta"
                    )}
                }
            )
        }
    }

    private fun registerMealWithType(mealType: MealType) {
        val recipeId = recipeId
        val recipeName = _uiState.value.recipeTitle
        val servings = _uiState.value.servings

        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                showMealTypeDialog = false,
                error = null
            )}

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
                    _navigation.value = RecipeDetailNavigation.ToMealLog
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

    fun reloadRecipe() {
        loadRecipeDetail()
    }
}