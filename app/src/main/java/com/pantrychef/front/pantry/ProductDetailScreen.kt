package com.pantrychef.front.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pantrychef.front.components.*
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Recargar cuando vuelves de edición
    LaunchedEffect(Unit) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("product_updated")?.observeForever { updated ->
            if (updated == true) {
                viewModel.refresh()
                savedStateHandle.remove<Boolean>("product_updated")
            }
        }
    }

    // Handle navigation
    LaunchedEffect(navigation) {
        when (val nav = navigation) {
            is ProductDetailNavigation.ToRecipeDetail -> {
                navController.navigate(Routes.recipeDetail(nav.recipeId))
                viewModel.clearNavigation()
            }
            ProductDetailNavigation.ToShoppingList -> {
                navController.navigate(Routes.SHOPPING_LIST)
                viewModel.clearNavigation()
            }
            ProductDetailNavigation.ToEdit -> {
                navController.navigate(Routes.pantryEdit(productId))
                viewModel.clearNavigation()
            }
            ProductDetailNavigation.Back -> {
                navController.popBackStack()
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    ProductDetailContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailContent(
    uiState: ProductDetailUiState,
    onEvent: (ProductDetailEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text(uiState.productName) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            actions = {
                IconButton(onClick = { onEvent(ProductDetailEvent.EditClicked) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.categoryEmoji,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Category Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(
                        text = uiState.category,
                        severity = BadgeSeverity.INFO
                    )
                    Text(
                        text = "•",
                        color = TextSecondary
                    )
                    Text(
                        text = uiState.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Alert Status (si aplica)
                if (uiState.alertStatus != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = when (uiState.alertStatus.severity) {
                            BadgeSeverity.CRITICAL -> com.pantrychef.front.theme.BadgeCritical.copy(alpha = 0.15f)
                            BadgeSeverity.URGENT -> com.pantrychef.front.theme.BadgeUrgent.copy(alpha = 0.15f)
                            BadgeSeverity.WARNING -> com.pantrychef.front.theme.BadgeWarning.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.alertStatus.message,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(
                                text = when (uiState.alertStatus.severity) {
                                    BadgeSeverity.CRITICAL -> "Urgente"
                                    BadgeSeverity.URGENT -> "Crítico"
                                    BadgeSeverity.WARNING -> "Bajo"
                                    else -> ""
                                },
                                severity = uiState.alertStatus.severity
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cantidad Actual
                Text(
                    text = "Cantidad actual",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ajustador de cantidad
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onEvent(ProductDetailEvent.DecreaseQuantity) },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = "−",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.widthIn(min = 120.dp)
                            ) {
                                Text(
                                    text = "${uiState.currentQuantity}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = uiState.unit,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                            }

                            IconButton(
                                onClick = { onEvent(ProductDetailEvent.IncreaseQuantity) },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Botón guardar (solo si hay cambios)
                        if (uiState.hasUnsavedChanges) {
                            Spacer(modifier = Modifier.height(16.dp))

                            PrimaryButton(
                                text = "✓ Guardar cambios",
                                onClick = { onEvent(ProductDetailEvent.SaveQuantityChanges) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Sugerencia de reposición (si aplica)
                if (uiState.suggestedAmount > 0) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Sugerencia de reposición",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        onClick = { onEvent(ProductDetailEvent.AddToShoppingList) },
                        color = com.pantrychef.front.theme.PrimaryGreenLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Añadir ${uiState.suggestedQuantity} a lista de compra",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Para alcanzar el nivel óptimo de stock",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Añadir a Lista →",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Recetas que usan este producto
                if (uiState.recipeSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Recetas que usan este producto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.recipeSuggestions.forEach { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onEvent(ProductDetailEvent.RecipeClicked(recipe.id)) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Información
                Text(
                    text = "Información",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(label = "Categoría", value = uiState.category)
                InfoRow(label = "Marca", value = uiState.brand)
                InfoRow(label = "Ubicación", value = uiState.location)
                InfoRow(label = "Umbral bajo", value = "${uiState.lowStockThreshold} ${uiState.unit}")

                Spacer(modifier = Modifier.height(32.dp))

                // Acciones
                Text(
                    text = "Acciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    onClick = { onEvent(ProductDetailEvent.DeleteClicked) },
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eliminar producto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Esta acción no se puede deshacer",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Eliminar",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Dialog de confirmación
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(ProductDetailEvent.DismissDeleteDialog) },
                title = { Text("¿Eliminar producto?") },
                text = { Text("Esta acción no se puede deshacer. El producto se eliminará permanentemente de tu despensa.") },
                confirmButton = {
                    TextButton(
                        onClick = { onEvent(ProductDetailEvent.ConfirmDelete) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(ProductDetailEvent.DismissDeleteDialog) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: RecipeSuggestion,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍽️",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${recipe.time} • ${recipe.usesAmount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, heightDp = 2000)
@Composable
private fun ProductDetailScreenPreview() {
    PantryChefTheme {
        ProductDetailContent(
            uiState = ProductDetailUiState(
                productName = "Aceite de oliva",
                category = "Condimentos",
                categoryEmoji = "🧂",
                location = "Despensa",
                brand = "Carbonell",
                originalQuantity = 53f,
                currentQuantity = 55f,
                unit = "cdas",
                lowStockThreshold = 0.2f,
                suggestedQuantity = "47 cdas",
                suggestedAmount = 47f,
                alertStatus = AlertStatus(
                    message = "Stock bajo - Quedan 53 cdas",
                    severity = BadgeSeverity.WARNING
                ),
                recipeSuggestions = listOf(
                    RecipeSuggestion(
                        id = "1",
                        name = "Ensalada mediterránea",
                        time = "15 min",
                        usesAmount = "Usa 2 cdas"
                    )
                ),
                hasUnsavedChanges = true
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}