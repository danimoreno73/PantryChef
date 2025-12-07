package com.pantrychef.front.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Unit
import com.pantrychef.back.repository.AuthRepository
import com.pantrychef.back.usecase.AddRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class IngredientInput(
    val id: String = UUID.randomUUID().toString(),
    val productName: String = "",
    val quantity: String = "",
    val unit: String = "g",
    val isOptional: Boolean = false
)

data class StepInput(
    val id: String = UUID.randomUUID().toString(),
    val instruction: String = ""
)

data class AddRecipeUiState(
    val name: String = "",
    val prepTimeMinutes: String = "",
    val servings: String = "",
    val difficulty: String = "Fácil",
    val ingredients: List<IngredientInput> = listOf(IngredientInput()),
    val steps: List<StepInput> = listOf(StepInput()),
    val unitOptions: List<String> = emptyList(),
    val difficultyOptions: List<String> = emptyList(),
    val nameError: String? = null,
    val prepTimeError: String? = null,
    val servingsError: String? = null,
    val ingredientsError: String? = null,
    val stepsError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface AddRecipeEvent {
    data class NameChanged(val name: String) : AddRecipeEvent
    data class PrepTimeChanged(val time: String) : AddRecipeEvent
    data class ServingsChanged(val servings: String) : AddRecipeEvent
    data class DifficultyChanged(val difficulty: String) : AddRecipeEvent

    data class IngredientNameChanged(val id: String, val name: String) : AddRecipeEvent
    data class IngredientQuantityChanged(val id: String, val quantity: String) : AddRecipeEvent
    data class IngredientUnitChanged(val id: String, val unit: String) : AddRecipeEvent
    data class IngredientOptionalToggled(val id: String, val isOptional: Boolean) : AddRecipeEvent
    data class RemoveIngredient(val id: String) : AddRecipeEvent
    object AddIngredient : AddRecipeEvent

    data class StepChanged(val id: String, val instruction: String) : AddRecipeEvent
    data class RemoveStep(val id: String) : AddRecipeEvent
    data class AddIngredientAfter(val id: String) : AddRecipeEvent
    data class AddStepAfter(val id: String) : AddRecipeEvent
    object AddStep : AddRecipeEvent

    object SaveClicked : AddRecipeEvent
}

sealed interface AddRecipeNavigation {
    object Back : AddRecipeNavigation
}

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val addRecipeUseCase: AddRecipeUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddRecipeUiState(
            unitOptions = Unit.values().map { unitToSpanish(it) },
            difficultyOptions = Difficulty.values().map { difficultyToSpanish(it) }
        )
    )
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<AddRecipeNavigation?>(null)
    val navigation: StateFlow<AddRecipeNavigation?> = _navigation.asStateFlow()

    fun onEvent(event: AddRecipeEvent) {
        when (event) {
            is AddRecipeEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, nameError = null) }
            }

            is AddRecipeEvent.PrepTimeChanged -> {
                _uiState.update { it.copy(prepTimeMinutes = event.time, prepTimeError = null) }
            }

            is AddRecipeEvent.ServingsChanged -> {
                _uiState.update { it.copy(servings = event.servings, servingsError = null) }
            }

            is AddRecipeEvent.DifficultyChanged -> {
                _uiState.update { it.copy(difficulty = event.difficulty) }
            }

            is AddRecipeEvent.IngredientNameChanged -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(productName = event.name)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated, ingredientsError = null) }
            }

            is AddRecipeEvent.IngredientQuantityChanged -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(quantity = event.quantity)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated, ingredientsError = null) }
            }

            is AddRecipeEvent.IngredientUnitChanged -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(unit = event.unit)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated) }
            }

            is AddRecipeEvent.IngredientOptionalToggled -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(isOptional = event.isOptional)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated) }
            }

            is AddRecipeEvent.RemoveIngredient -> {
                val updated = _uiState.value.ingredients.filter { it.id != event.id }
                if (updated.isNotEmpty()) {
                    _uiState.update { it.copy(ingredients = updated) }
                }
            }

            AddRecipeEvent.AddIngredient -> {
                val updated = _uiState.value.ingredients + IngredientInput()
                _uiState.update { it.copy(ingredients = updated) }
            }

            is AddRecipeEvent.StepChanged -> {
                val updated = _uiState.value.steps.map { step ->
                    if (step.id == event.id) {
                        step.copy(instruction = event.instruction)
                    } else step
                }
                _uiState.update { it.copy(steps = updated, stepsError = null) }
            }

            is AddRecipeEvent.RemoveStep -> {
                val updated = _uiState.value.steps.filter { it.id != event.id }
                if (updated.isNotEmpty()) {
                    _uiState.update { it.copy(steps = updated) }
                }
            }

            AddRecipeEvent.AddStep -> {
                val updated = _uiState.value.steps + StepInput()
                _uiState.update { it.copy(steps = updated) }
            }

            is AddRecipeEvent.AddIngredientAfter -> {
                val currentList = _uiState.value.ingredients
                val index = currentList.indexOfFirst { it.id == event.id }
                if (index != -1) {
                    val updated = currentList.toMutableList().apply {
                        add(index + 1, IngredientInput())
                    }
                    _uiState.update { it.copy(ingredients = updated) }
                }
            }

            is AddRecipeEvent.AddStepAfter -> {
                val currentList = _uiState.value.steps
                val index = currentList.indexOfFirst { it.id == event.id }
                if (index != -1) {
                    val updated = currentList.toMutableList().apply {
                        add(index + 1, StepInput())
                    }
                    _uiState.update { it.copy(steps = updated) }
                }
            }

            AddRecipeEvent.SaveClicked -> {
                saveRecipe()
            }
        }
    }

    private fun saveRecipe() {
        // Validación
        val nameError = if (_uiState.value.name.isBlank()) "El nombre es requerido" else null
        val prepTimeError = if (_uiState.value.prepTimeMinutes.isBlank()) "El tiempo es requerido" else null
        val servingsError = if (_uiState.value.servings.isBlank()) "Las porciones son requeridas" else null

        val validIngredients = _uiState.value.ingredients.filter {
            it.productName.isNotBlank() && it.quantity.isNotBlank()
        }
        val ingredientsError = if (validIngredients.isEmpty()) "Añade al menos un ingrediente" else null

        val validSteps = _uiState.value.steps.filter { it.instruction.isNotBlank() }
        val stepsError = if (validSteps.isEmpty()) "Añade al menos un paso" else null

        if (nameError != null || prepTimeError != null || servingsError != null ||
            ingredientsError != null || stepsError != null) {
            _uiState.update { it.copy(
                nameError = nameError,
                prepTimeError = prepTimeError,
                servingsError = servingsError,
                ingredientsError = ingredientsError,
                stepsError = stepsError
            )}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Obtener usuario actual
                val userResult = authRepository.getCurrentUser()
                val userId = userResult.getOrNull()?.id ?: "unknown"

                val recipeId = UUID.randomUUID().toString()

                // Mapear ingredientes
                val ingredients = validIngredients.map { input ->
                    Ingredient(
                        id = UUID.randomUUID().toString(),
                        recipeId = recipeId,
                        productName = input.productName,
                        quantity = input.quantity.toFloatOrNull() ?: 0f,
                        unit = spanishToUnit(input.unit),
                        isOptional = input.isOptional
                    )
                }

                // Mapear pasos
                val steps = validSteps.map { it.instruction }

                // Mapear dificultad
                val difficulty = spanishToDifficulty(_uiState.value.difficulty)

                val result = addRecipeUseCase(
                    id = recipeId,
                    name = _uiState.value.name,
                    imageUrl = null,
                    prepTimeMinutes = _uiState.value.prepTimeMinutes.toIntOrNull() ?: 0,
                    servings = _uiState.value.servings.toIntOrNull() ?: 0,
                    difficulty = difficulty,
                    steps = steps,
                    ingredients = ingredients,
                    createdBy = userId,
                    isPublic = false,
                    createdAt = System.currentTimeMillis()
                )

                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false) }
                        _navigation.value = AddRecipeNavigation.Back
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al crear receta"
                        )}
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al crear receta"
                )}
            }
        }
    }

    // ========== MAPEO DE UNIT ==========

    private fun unitToSpanish(unit: Unit): String {
        return when (unit) {
            Unit.GRAMS -> "g"
            Unit.KILOGRAMS -> "kg"
            Unit.LITERS -> "L"
            Unit.MILLILITERS -> "ml"
            Unit.UNITS -> "uds"
            Unit.TABLESPOONS -> "cdas"
            Unit.CUPS -> "tazas"
            Unit.PACKAGES -> "paquetes"
            Unit.DOZEN -> "docena"
        }
    }

    private fun spanishToUnit(spanish: String): Unit {
        return when (spanish) {
            "g" -> Unit.GRAMS
            "kg" -> Unit.KILOGRAMS
            "L" -> Unit.LITERS
            "ml" -> Unit.MILLILITERS
            "uds" -> Unit.UNITS
            "cdas" -> Unit.TABLESPOONS
            "tazas" -> Unit.CUPS
            "paquetes" -> Unit.PACKAGES
            "docena" -> Unit.DOZEN
            else -> Unit.GRAMS
        }
    }

    // ========== MAPEO DE DIFFICULTY ==========

    private fun difficultyToSpanish(difficulty: Difficulty): String {
        return when (difficulty) {
            Difficulty.EASY -> "Fácil"
            Difficulty.MEDIUM -> "Media"
            Difficulty.HARD -> "Difícil"
        }
    }

    private fun spanishToDifficulty(spanish: String): Difficulty {
        return when (spanish) {
            "Fácil" -> Difficulty.EASY
            "Media" -> Difficulty.MEDIUM
            "Difícil" -> Difficulty.HARD
            else -> Difficulty.EASY
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}