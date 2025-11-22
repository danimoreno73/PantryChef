package com.pantrychef.front.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pantrychef.front.components.*
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight
import com.pantrychef.front.theme.TextSecondary

@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (val nav = navigation) {
            is ProductDetailNavigation.ToRecipe -> {
                navController.navigate(Routes.recipeDetail(nav.recipeId))
                viewModel.clearNavigation()
            }
            ProductDetailNavigation.ToShoppingList -> {
                navController.navigate(Routes.SHOPPING_LIST)
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
                IconButton(onClick = { onEvent(ProductDetailEvent.EditProduct) }) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Ajustes"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Alert Status Banner
            if (uiState.alertStatus != null) {
                Surface(
                    color = when (uiState.alertStatus.severity) {
                        BadgeSeverity.URGENT -> com.pantrychef.front.theme.BadgeUrgent.copy(alpha = 0.15f)
                        BadgeSeverity.WARNING -> com.pantrychef.front.theme.BadgeWarning.copy(alpha = 0.15f)
                        else -> PrimaryGreenLight
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estado de alerta",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.alertStatus.message,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.alertStatus.daysUntilExpiry != null) {
                                Text(
                                    text = "Categoría: ${uiState.location} • Última compra: hace 10 días",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        StatusBadge(
                            text = uiState.alertStatus.actionLabel,
                            severity = uiState.alertStatus.severity
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Cantidad actual
            Text(
                text = "Cantidad actual",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.currentQuantity} ${uiState.unit}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ajustar cantidad
            Surface(
                color = PrimaryGreenLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ajustar cantidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { onEvent(ProductDetailEvent.DecreaseQuantity) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }

                        Text(
                            text = "${uiState.currentQuantity} ${uiState.unit}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 60.dp)
                        )

                        IconButton(
                            onClick = { onEvent(ProductDetailEvent.IncreaseQuantity) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Acciones rápidas
            Text(
                text = "Acciones rápidas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickActionCard(
                title = "Añadir a la lista de compra",
                subtitle = "Se sugiere 2 L para la semana",
                actionLabel = "+2 L",
                onClick = { onEvent(ProductDetailEvent.AddToShoppingList) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionCard(
                title = "Marcar como repuesto",
                subtitle = "Mover alerta a resuelto",
                actionLabel = "Actualizar",
                onClick = { onEvent(ProductDetailEvent.MarkAsResolved) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionCard(
                title = "Descartar restante",
                subtitle = "Eliminar 0.5 L del inventario",
                actionLabel = "Eliminar",
                onClick = { onEvent(ProductDetailEvent.DiscardRemaining) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Usar antes de que venza
            Text(
                text = "Usar antes de que venza",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            uiState.recipeSuggestions.forEach { recipe ->
                RecipeSuggestionCard(
                    recipe = recipe,
                    onClick = { onEvent(ProductDetailEvent.RecipeClicked(recipe.id)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Detalles de despensa
            Text(
                text = "Detalles de despensa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = "Ubicación", value = uiState.location)
            DetailRow(label = "Marca", value = uiState.brand)
            DetailRow(label = "Límite bajo", value = "${uiState.lowStockThreshold} ${uiState.unit}")

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = "Añadir 2 L a Lista",
                    onClick = { onEvent(ProductDetailEvent.AddToShoppingList) },
                    modifier = Modifier.weight(1f)
                )

                SecondaryButton(
                    text = "Actualizar cantidad",
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    actionLabel: String,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun RecipeSuggestionCard(
    recipe: RecipeSuggestion,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "☕",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .size(48.dp)
                    .wrapContentSize(Alignment.Center)
            )

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

            StatusBadge(
                text = if (recipe.status == BadgeSeverity.SUCCESS) "Cocinable" else "Casi",
                severity = recipe.status
            )
        }
    }
}

@Composable
private fun DetailRow(
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

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun ProductDetailScreenPreview() {
    PantryChefTheme {
        ProductDetailContent(
            uiState = ProductDetailUiState(
                productName = "Leche",
                currentQuantity = 0.5,
                unit = "L"
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}