package com.pantrychef.front.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pantrychef.front.components.DropdownSelector
import com.pantrychef.front.components.InputField
import com.pantrychef.front.components.PrimaryButton
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun EditRecipeScreen(
    recipeId: String,
    navController: NavController,
    viewModel: EditRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            EditRecipeNavigation.Back -> {
                // Notificar que la receta se actualizó
                navController.previousBackStackEntry?.savedStateHandle?.set("recipe_updated", true)
                navController.popBackStack()
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    EditRecipeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRecipeContent(
    uiState: EditRecipeUiState,
    onEvent: (EditRecipeEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Editar receta") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información básica
            Text(
                text = "Información básica",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            InputField(
                value = uiState.name,
                onValueChange = { onEvent(EditRecipeEvent.NameChanged(it)) },
                label = "Nombre de la receta",
                placeholder = "Ej. Ensalada César",
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InputField(
                    value = uiState.prepTimeMinutes,
                    onValueChange = { onEvent(EditRecipeEvent.PrepTimeChanged(it)) },
                    label = "Tiempo (min)",
                    placeholder = "30",
                    keyboardType = KeyboardType.Number,
                    isError = uiState.prepTimeError != null,
                    errorMessage = uiState.prepTimeError,
                    modifier = Modifier.weight(1f)
                )

                InputField(
                    value = uiState.servings,
                    onValueChange = { onEvent(EditRecipeEvent.ServingsChanged(it)) },
                    label = "Porciones",
                    placeholder = "4",
                    keyboardType = KeyboardType.Number,
                    isError = uiState.servingsError != null,
                    errorMessage = uiState.servingsError,
                    modifier = Modifier.weight(1f)
                )
            }

            DropdownSelector(
                label = "Dificultad",
                value = uiState.difficulty,
                options = uiState.difficultyOptions,
                onValueChange = { onEvent(EditRecipeEvent.DifficultyChanged(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ingredientes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingredientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onEvent(EditRecipeEvent.AddIngredient) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir ingrediente",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.ingredientsError != null) {
                Text(
                    text = uiState.ingredientsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            uiState.ingredients.forEachIndexed { index, ingredient ->
                IngredientCard(
                    index = index + 1,
                    ingredient = ingredient,
                    unitOptions = uiState.unitOptions,
                    onNameChanged = { onEvent(EditRecipeEvent.IngredientNameChanged(ingredient.id, it)) },
                    onQuantityChanged = { onEvent(EditRecipeEvent.IngredientQuantityChanged(ingredient.id, it)) },
                    onUnitChanged = { onEvent(EditRecipeEvent.IngredientUnitChanged(ingredient.id, it)) },
                    onOptionalToggled = { onEvent(EditRecipeEvent.IngredientOptionalToggled(ingredient.id, it)) },
                    onRemove = { onEvent(EditRecipeEvent.RemoveIngredient(ingredient.id)) },
                    onAddAfter = { onEvent(EditRecipeEvent.AddIngredientAfter(ingredient.id)) },
                    canRemove = uiState.ingredients.size > 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pasos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pasos de preparación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onEvent(EditRecipeEvent.AddStep) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir paso",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.stepsError != null) {
                Text(
                    text = uiState.stepsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            uiState.steps.forEachIndexed { index, step ->
                StepCard(
                    number = index + 1,
                    step = step,
                    onInstructionChanged = { onEvent(EditRecipeEvent.StepChanged(step.id, it)) },
                    onRemove = { onEvent(EditRecipeEvent.RemoveStep(step.id)) },
                    onAddAfter = { onEvent(EditRecipeEvent.AddStepAfter(step.id)) },
                    canRemove = uiState.steps.size > 1
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón guardar
            PrimaryButton(
                text = "Guardar cambios",
                onClick = { onEvent(EditRecipeEvent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun IngredientCard(
    index: Int,
    ingredient: IngredientInput,
    unitOptions: List<String>,
    onNameChanged: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    onOptionalToggled: (Boolean) -> Unit,
    onRemove: () -> Unit,
    onAddAfter: () -> Unit,
    canRemove: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingrediente $index",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            InputField(
                value = ingredient.productName,
                onValueChange = onNameChanged,
                label = "Nombre",
                placeholder = "Ej. Tomate"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InputField(
                    value = ingredient.quantity,
                    onValueChange = onQuantityChanged,
                    label = "Cantidad",
                    placeholder = "250",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )

                DropdownSelector(
                    label = "Unidad",
                    value = ingredient.unit,
                    options = unitOptions,
                    onValueChange = onUnitChanged,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = ingredient.isOptional,
                        onCheckedChange = onOptionalToggled
                    )
                    Text(
                        text = "Opcional",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                OutlinedButton(
                    onClick = onAddAfter,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir ingrediente después",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir otro", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    step: StepInput,
    onInstructionChanged: (String) -> Unit,
    onRemove: () -> Unit,
    onAddAfter: () -> Unit,
    canRemove: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paso $number",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            InputField(
                value = step.instruction,
                onValueChange = onInstructionChanged,
                label = "Instrucción",
                placeholder = "Describe este paso...",
                singleLine = false,
                minLines = 3,
                maxLines = 6
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onAddAfter,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir paso después",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir otro", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun EditRecipeScreenPreview() {
    PantryChefTheme {
        EditRecipeContent(
            uiState = EditRecipeUiState(
                name = "Pasta Carbonara",
                prepTimeMinutes = "30",
                servings = "4",
                difficulty = "Media"
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}