package com.pantrychef.front.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Unit
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.usecase.UpdateRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditRecipeUiState(
    val recipeId: String = "",
    val name: String = "",
    val prepTimeMinutes: String = "",
    val servings: String = "",
    val difficulty: String = "Fácil",
    val ingredients: List<IngredientInput> = listOf(IngredientInput()),
    val steps: List<StepInput> = listOf(StepInput()),
    val unitOptions: List<String> = emptyList(),
    val difficultyOptions: List<String> = emptyList(),
    val imageUrl: String? = null,
    val createdBy: String = "",
    val isPublic: Boolean = false,
    val createdAt: Long = 0L,
    val nameError: String? = null,
    val prepTimeError: String? = null,
    val servingsError: String? = null,
    val ingredientsError: String? = null,
    val stepsError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface EditRecipeEvent {
    data class NameChanged(val name: String) : EditRecipeEvent
    data class PrepTimeChanged(val time: String) : EditRecipeEvent
    data class ServingsChanged(val servings: String) : EditRecipeEvent
    data class DifficultyChanged(val difficulty: String) : EditRecipeEvent

    data class IngredientNameChanged(val id: String, val name: String) : EditRecipeEvent
    data class IngredientQuantityChanged(val id: String, val quantity: String) : EditRecipeEvent
    data class IngredientUnitChanged(val id: String, val unit: String) : EditRecipeEvent
    data class IngredientOptionalToggled(val id: String, val isOptional: Boolean) : EditRecipeEvent
    data class RemoveIngredient(val id: String) : EditRecipeEvent
    data class AddIngredientAfter(val id: String) : EditRecipeEvent
    object AddIngredient : EditRecipeEvent

    data class StepChanged(val id: String, val instruction: String) : EditRecipeEvent
    data class RemoveStep(val id: String) : EditRecipeEvent
    data class AddStepAfter(val id: String) : EditRecipeEvent
    object AddStep : EditRecipeEvent

    object SaveClicked : EditRecipeEvent
}

sealed interface EditRecipeNavigation {
    object Back : EditRecipeNavigation
}

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val updateRecipeUseCase: UpdateRecipeUseCase
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow(
        EditRecipeUiState(
            unitOptions = Unit.values().map { unitToSpanish(it) },
            difficultyOptions = Difficulty.values().map { difficultyToSpanish(it) }
        )
    )
    val uiState: StateFlow<EditRecipeUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<EditRecipeNavigation?>(null)
    val navigation: StateFlow<EditRecipeNavigation?> = _navigation.asStateFlow()

    init {
        loadRecipe()
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = recipeRepository.getRecipeById(recipeId)

                result.fold(
                    onSuccess = { recipe ->
                        // Mapear ingredientes
                        val ingredientInputs = recipe.ingredients.map { ingredient ->
                            IngredientInput(
                                id = ingredient.id,
                                productName = ingredient.productName,
                                quantity = ingredient.quantity.toString(),
                                unit = unitToSpanish(ingredient.unit),
                                isOptional = ingredient.isOptional
                            )
                        }

                        // Mapear pasos
                        val stepInputs = recipe.steps.mapIndexed { index, instruction ->
                            StepInput(
                                id = "step_$index",
                                instruction = instruction
                            )
                        }

                        _uiState.update { it.copy(
                            recipeId = recipe.id,
                            name = recipe.name,
                            prepTimeMinutes = recipe.prepTimeMinutes.toString(),
                            servings = recipe.servings.toString(),
                            difficulty = difficultyToSpanish(recipe.difficulty),
                            ingredients = ingredientInputs,
                            steps = stepInputs,
                            imageUrl = recipe.imageUrl,
                            createdBy = recipe.createdBy,
                            isPublic = recipe.isPublic,
                            createdAt = recipe.createdAt,
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

    fun onEvent(event: EditRecipeEvent) {
        when (event) {
            is EditRecipeEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, nameError = null) }
            }

            is EditRecipeEvent.PrepTimeChanged -> {
                _uiState.update { it.copy(prepTimeMinutes = event.time, prepTimeError = null) }
            }

            is EditRecipeEvent.ServingsChanged -> {
                _uiState.update { it.copy(servings = event.servings, servingsError = null) }
            }

            is EditRecipeEvent.DifficultyChanged -> {
                _uiState.update { it.copy(difficulty = event.difficulty) }
            }

            is EditRecipeEvent.IngredientNameChanged -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(productName = event.name)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated, ingredientsError = null) }
            }

            is EditRecipeEvent.IngredientQuantityChanged -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(quantity = event.quantity)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated, ingredientsError = null) }
            }

            is EditRecipeEvent.IngredientUnitChanged -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(unit = event.unit)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated) }
            }

            is EditRecipeEvent.IngredientOptionalToggled -> {
                val updated = _uiState.value.ingredients.map { ingredient ->
                    if (ingredient.id == event.id) {
                        ingredient.copy(isOptional = event.isOptional)
                    } else ingredient
                }
                _uiState.update { it.copy(ingredients = updated) }
            }

            is EditRecipeEvent.RemoveIngredient -> {
                val updated = _uiState.value.ingredients.filter { it.id != event.id }
                if (updated.isNotEmpty()) {
                    _uiState.update { it.copy(ingredients = updated) }
                }
            }

            is EditRecipeEvent.AddIngredientAfter -> {
                val currentList = _uiState.value.ingredients
                val index = currentList.indexOfFirst { it.id == event.id }
                if (index != -1) {
                    val updated = currentList.toMutableList().apply {
                        add(index + 1, IngredientInput())
                    }
                    _uiState.update { it.copy(ingredients = updated) }
                }
            }

            EditRecipeEvent.AddIngredient -> {
                val updated = _uiState.value.ingredients + IngredientInput()
                _uiState.update { it.copy(ingredients = updated) }
            }

            is EditRecipeEvent.StepChanged -> {
                val updated = _uiState.value.steps.map { step ->
                    if (step.id == event.id) {
                        step.copy(instruction = event.instruction)
                    } else step
                }
                _uiState.update { it.copy(steps = updated, stepsError = null) }
            }

            is EditRecipeEvent.RemoveStep -> {
                val updated = _uiState.value.steps.filter { it.id != event.id }
                if (updated.isNotEmpty()) {
                    _uiState.update { it.copy(steps = updated) }
                }
            }

            is EditRecipeEvent.AddStepAfter -> {
                val currentList = _uiState.value.steps
                val index = currentList.indexOfFirst { it.id == event.id }
                if (index != -1) {
                    val updated = currentList.toMutableList().apply {
                        add(index + 1, StepInput())
                    }
                    _uiState.update { it.copy(steps = updated) }
                }
            }

            EditRecipeEvent.AddStep -> {
                val updated = _uiState.value.steps + StepInput()
                _uiState.update { it.copy(steps = updated) }
            }

            EditRecipeEvent.SaveClicked -> {
                updateRecipe()
            }
        }
    }

    private fun updateRecipe() {
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
                // Mapear ingredientes
                val ingredients = validIngredients.map { input ->
                    Ingredient(
                        id = input.id,
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

                // Crear receta actualizada
                val updatedRecipe = com.pantrychef.back.model.Recipe(
                    id = recipeId,
                    name = _uiState.value.name,
                    imageUrl = _uiState.value.imageUrl,
                    prepTimeMinutes = _uiState.value.prepTimeMinutes.toIntOrNull() ?: 0,
                    servings = _uiState.value.servings.toIntOrNull() ?: 0,
                    difficulty = difficulty,
                    steps = steps,
                    ingredients = ingredients,
                    createdBy = _uiState.value.createdBy,
                    isPublic = _uiState.value.isPublic,
                    createdAt = _uiState.value.createdAt
                )

                val result = updateRecipeUseCase(updatedRecipe)

                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false) }
                        _navigation.value = EditRecipeNavigation.Back
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al actualizar receta"
                        )}
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar receta"
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