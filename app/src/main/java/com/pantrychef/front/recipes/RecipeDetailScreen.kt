package com.pantrychef.front.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pantrychef.back.model.enums.MealType
import com.pantrychef.front.components.*
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight
import com.pantrychef.front.theme.TextSecondary


@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navController: NavController,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Detectar cuando vuelves de edición
    LaunchedEffect(Unit) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("recipe_updated")?.observeForever { updated ->
            if (updated == true) {
                viewModel.reloadRecipe()
                savedStateHandle.remove<Boolean>("recipe_updated")
            }
        }
    }

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            RecipeDetailNavigation.ToShoppingList -> {
                navController.navigate(Routes.SHOPPING_LIST)
                viewModel.clearNavigation()
            }
            RecipeDetailNavigation.ToMealLog -> {
                navController.navigate(Routes.MEAL_LOG)
                viewModel.clearNavigation()
            }
            is RecipeDetailNavigation.ToEditRecipe -> {
                val recipeId = (navigation as RecipeDetailNavigation.ToEditRecipe).recipeId
                navController.navigate(Routes.RECIPES_EDIT.replace("{recipeId}", recipeId))
                viewModel.clearNavigation()
            }
            RecipeDetailNavigation.Back -> {
                navController.popBackStack()
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    RecipeDetailContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailContent(
    uiState: RecipeDetailUiState,
    onEvent: (RecipeDetailEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text(uiState.recipeTitle) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            actions = {
                IconButton(onClick = { onEvent(RecipeDetailEvent.ToggleFavorite) }) {
                    Icon(
                        imageVector = if (uiState.isFavorite)
                            Icons.Filled.Favorite
                        else
                            Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (uiState.isFavorite)
                            Color.Red
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (uiState.imageUrl != null) {
                    AsyncImage(
                        model = uiState.imageUrl,
                        contentDescription = uiState.recipeTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "🍽️",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Recipe Info Card
            Surface(
                color = PrimaryGreenLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Listo en ${uiState.prepTime} min • ${uiState.servings} porciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (uiState.availabilityStatus) {
                                    AvailabilityStatus.FULL -> "Cocinable"
                                    AvailabilityStatus.PARTIAL -> "${uiState.ingredients.count { it.isAvailable }}/${uiState.ingredients.size} ingredientes"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.difficulty,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (uiState.availabilityStatus == AvailabilityStatus.PARTIAL) {
                        StatusBadge(
                            text = "Falta ${uiState.missingIngredients.size}",
                            severity = BadgeSeverity.WARNING
                        )
                    }
                }
            }

            // Ingredientes Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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

                    if (uiState.isUserRecipe) {
                        TextButton(onClick = { onEvent(RecipeDetailEvent.EditRecipeClicked) }) {
                            Text("Editar receta")
                        }
                    } else {
                        TextButton(onClick = { /* No action for non-user recipes */ }) {
                            Text("${uiState.servings} porciones")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                uiState.ingredients.forEach { ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onCheckedChange = { checked ->
                            onEvent(RecipeDetailEvent.IngredientChecked(ingredient.id, checked))
                        }
                    )
                }

                // Botones
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isUserRecipe) {
                    // Receta del usuario: Editar + Cocinar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "Editar receta",
                            onClick = { onEvent(RecipeDetailEvent.EditRecipeClicked) },
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryButton(
                            text = "Cocinar ahora",
                            onClick = { onEvent(RecipeDetailEvent.CookNowClicked) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Receta pública: Solo cocinar
                    PrimaryButton(
                        text = "Cocinar ahora",
                        onClick = { onEvent(RecipeDetailEvent.CookNowClicked) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pasos Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Pasos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                uiState.steps.forEach { step ->
                    RecipeStepItem(step = step)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Después de cocinar Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Después de cocinar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                AfterCookingCard(
                    title = "Registrar comida",
                    subtitle = "Reduce los ingredientes de la despensa",
                    actionLabel = "Listo",
                    onClick = { onEvent(RecipeDetailEvent.RegisterMeal) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.availabilityStatus == AvailabilityStatus.PARTIAL) {
                    AfterCookingCard(
                        title = "Añadir faltantes a lista",
                        subtitle = "Para repetir esta receta",
                        actionLabel = "Sugerido",
                        onClick = { onEvent(RecipeDetailEvent.AddMissingToShoppingList) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AfterCookingCard(
                        title = "Sustituir ingredientes",
                        subtitle = "Sugerencias alternativas",
                        actionLabel = "Ver",
                        onClick = { onEvent(RecipeDetailEvent.SubstituteIngredients) }
                    )
                }

                // Botón eliminar (solo para recetas del usuario)
                if (uiState.isUserRecipe) {
                    Spacer(modifier = Modifier.height(8.dp))

                    AfterCookingCard(
                        title = "Eliminar receta",
                        subtitle = "Esta acción no se puede deshacer",
                        actionLabel = "Eliminar",
                        onClick = { onEvent(RecipeDetailEvent.DeleteRecipeClicked) },
                        isDestructive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        // Dialog de tipo de comida
        if (uiState.showMealTypeDialog) {
            MealTypeDialog(
                onDismiss = { onEvent(RecipeDetailEvent.DismissMealTypeDialog) },
                onConfirm = { mealType ->
                    onEvent(RecipeDetailEvent.ConfirmMealType(mealType))
                }
            )
        }

        // Dialog de confirmación para eliminar
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(RecipeDetailEvent.DismissDeleteDialog) },
                title = { Text("¿Eliminar receta?") },
                text = { Text("Esta acción no se puede deshacer. La receta se eliminará permanentemente.") },
                confirmButton = {
                    TextButton(
                        onClick = { onEvent(RecipeDetailEvent.ConfirmDelete) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(RecipeDetailEvent.DismissDeleteDialog) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: IngredientItemUiModel,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = ingredient.isChecked,
            onCheckedChange = onCheckedChange,
            enabled = ingredient.isAvailable,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (ingredient.isAvailable)
                    MaterialTheme.colorScheme.onSurface
                else
                    TextSecondary
            )
            Text(
                text = ingredient.quantity,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        StatusBadge(
            text = if (ingredient.isAvailable) "En despensa" else "Falta",
            severity = if (ingredient.isAvailable) BadgeSeverity.SUCCESS else BadgeSeverity.WARNING
        )
    }
}

@Composable
private fun RecipeStepItem(step: RecipeStepUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "${step.number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.instruction,
                style = MaterialTheme.typography.bodyLarge
            )
            if (step.duration != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${step.duration} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AfterCookingCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDestructive)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MealTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (MealType) -> Unit
) {
    var selectedMealType by remember { mutableStateOf(MealType.LUNCH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "¿Cuándo vas a comer esto?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Selecciona el tipo de comida:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                MealType.values().forEach { mealType ->
                    val (emoji, label) = when (mealType) {
                        MealType.BREAKFAST -> "🌅" to "Desayuno"
                        MealType.LUNCH -> "🍽️" to "Almuerzo"
                        MealType.DINNER -> "🌙" to "Cena"
                        MealType.SNACK -> "🍿" to "Snack"
                    }

                    Surface(
                        onClick = { selectedMealType = mealType },
                        color = if (selectedMealType == mealType)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedMealType == mealType)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                                color = if (selectedMealType == mealType)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (selectedMealType == mealType) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedMealType) }
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true, heightDp = 2000)
@Composable
private fun RecipeDetailScreenPreview() {
    PantryChefTheme {
        RecipeDetailContent(
            uiState = RecipeDetailUiState(
                recipeTitle = "Pasta con verduras",
                prepTime = 25,
                servings = 2,
                difficulty = "Fácil",
                availabilityStatus = AvailabilityStatus.FULL,
                isUserRecipe = true
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}