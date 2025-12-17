package com.pantrychef.front.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pantrychef.front.components.*
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (val nav = navigation) {
            is HomeNavigation.ToProductDetail -> {
                navController.navigate(Routes.productDetail(nav.productId))
                viewModel.clearNavigation()
            }
            is HomeNavigation.ToRecipeDetail -> {
                navController.navigate(Routes.recipeDetail(nav.recipeId))
                viewModel.clearNavigation()
            }
            HomeNavigation.ToPantry -> {
                navController.navigate(Routes.PANTRY)
                viewModel.clearNavigation()
            }
            is HomeNavigation.ToRecipes -> {
                // TODO: Pasar el filtro cuando se implemente en RecipesScreen
                navController.navigate(Routes.RECIPES)
                viewModel.clearNavigation()
            }
            HomeNavigation.ToShoppingList -> {
                navController.navigate(Routes.SHOPPING_LIST)
                viewModel.clearNavigation()
            }
            HomeNavigation.ToSettings -> {
                navController.navigate(Routes.SETTINGS)
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PantryChef",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onEvent(HomeEvent.SettingsClicked) }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configuración"
                    )
                }
            }
        }

        // Resumen diario
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resumen diario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "☀️ Hoy",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DailySummaryCard(
                        count = uiState.cookableCount,
                        label = "cocinables",
                        modifier = Modifier.weight(1f)
                    )

                    DailySummaryCard(
                        count = uiState.almostCookableCount,
                        label = "casi cocinables",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Outlined.Add,
                        label = "Añadir productos en la despensa",
                        onClick = { onEvent(HomeEvent.AddToPantryClicked) },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        icon = Icons.Filled.CameraAlt,
                        label = "Escanear",
                        onClick = { onEvent(HomeEvent.ScanClicked) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Alertas
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alertas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.alerts.isNotEmpty()) {
                    TextButton(onClick = { onEvent(HomeEvent.ViewAllAlertsClicked) }) {
                        Text("Ver todo")
                    }
                }
            }
        }

        // Contenido de alertas o mensaje vacío
        if (uiState.alerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = " No hay alertas actualmente\nTodos tus productos están en buen estado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(uiState.alerts.take(3)) { alert ->
                AlertListItem(
                    productName = alert.productName,
                    message = alert.message,
                    categoryInfo = alert.categoryInfo,
                    severity = alert.severity,
                    actionLabel = alert.actionLabel,
                    onClick = { onEvent(HomeEvent.AlertClicked(alert.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Recetas cocinables
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recetas cocinables",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.cookableRecipes.isNotEmpty()) {
                    TextButton(onClick = { onEvent(HomeEvent.ViewAllCookableRecipesClicked) }) {
                        Text("Ver todas")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Contenido de recetas cocinables o mensaje vacío
        if (uiState.cookableRecipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🍳 No hay recetas completamente cocinables\nAñade más productos a tu despensa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(uiState.cookableRecipes.take(3)) { recipe ->
                RecipeCardHorizontal(
                    title = recipe.title,
                    prepTime = recipe.prepTime,
                    servings = recipe.servings,
                    imageUrl = recipe.imageUrl,
                    badge = recipe.badge,
                    badgeSeverity = recipe.badgeSeverity,
                    onClick = { onEvent(HomeEvent.RecipeClicked(recipe.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Casi cocinables
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Casi cocinables",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.almostCookableRecipes.isNotEmpty()) {
                    TextButton(onClick = { onEvent(HomeEvent.ViewAllAlmostCookableRecipesClicked) }) {
                        Text("Ver todas")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Contenido de casi cocinables o mensaje vacío
        if (uiState.almostCookableRecipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📝 No hay recetas casi cocinables\nExplora nuevas recetas en la sección de Recetas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(uiState.almostCookableRecipes.take(3)) { recipe ->
                RecipeCardHorizontal(
                    title = recipe.title,
                    prepTime = recipe.prepTime,
                    servings = recipe.servings,
                    imageUrl = recipe.imageUrl,
                    badge = recipe.badge,
                    badgeSeverity = recipe.badgeSeverity,
                    onClick = { onEvent(HomeEvent.RecipeClicked(recipe.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Lista de compra sugerida
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista de compra sugerida",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.shoppingPreview.isNotEmpty()) {
                    TextButton(onClick = { onEvent(HomeEvent.GoToShoppingListClicked) }) {
                        Text("Abrir lista")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Contenido de lista de compra o mensaje vacío
        if (uiState.shoppingPreview.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛒 No hay sugerencias de compra\nTu despensa está bien abastecida",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(uiState.shoppingPreview.take(2)) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.source,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 2000)
@Composable
private fun HomeScreenPreview() {
    PantryChefTheme {
        HomeContent(
            uiState = HomeUiState(
                cookableCount = 12,
                almostCookableCount = 7
            ),
            onEvent = {}
        )
    }
}