package com.pantrychef.front.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.pantrychef.front.components.RecipeCardGrid
import com.pantrychef.front.components.SearchBar
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight
import com.pantrychef.front.theme.TextSecondary

@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (val nav = navigation) {
            is RecipesNavigation.ToRecipeDetail -> {
                navController.navigate(Routes.recipeDetail(nav.recipeId))
                viewModel.clearNavigation()
            }
            RecipesNavigation.ToCreateRecipe -> {
                navController.navigate(Routes.RECIPES_ADD)
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    RecipesContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun RecipesContent(
    uiState: RecipesUiState,
    onEvent: (RecipesEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recetas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Search bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { onEvent(RecipesEvent.SearchQueryChanged(it)) },
            placeholder = "Buscar recetas...",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter tabs
        ScrollableTabRow(
            selectedTabIndex = uiState.activeTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background,
            edgePadding = 16.dp
        ) {
            RecipeTab.values().forEach { tab ->
                val isSelected = uiState.activeTab == tab

                Tab(
                    selected = isSelected,
                    onClick = { onEvent(RecipesEvent.TabChanged(tab)) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Surface(
                        color = if (isSelected) PrimaryGreenLight else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = when (tab) {
                                RecipeTab.ALL -> "Todos"
                                RecipeTab.COOKABLE -> "Cocinables"
                                RecipeTab.ALMOST -> "Casi"
                                RecipeTab.UNDER_30MIN -> "< 30 min"
                                RecipeTab.YOUR_RECIPES -> "Tus recetas"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título de sección según tab activo
        Text(
            text = when (uiState.activeTab) {
                RecipeTab.ALL -> "Todas las recetas"
                RecipeTab.COOKABLE -> "Cocinables ahora"
                RecipeTab.ALMOST -> "Casi cocinables"
                RecipeTab.UNDER_30MIN -> "Recetas rápidas"
                RecipeTab.YOUR_RECIPES -> "Tus recetas"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid de recetas según tab activo
        val recipes = when (uiState.activeTab) {
            RecipeTab.ALL -> uiState.allRecipes
            RecipeTab.COOKABLE -> uiState.cookableNow
            RecipeTab.ALMOST -> uiState.almostCookable
            RecipeTab.UNDER_30MIN -> uiState.under30Min
            RecipeTab.YOUR_RECIPES -> uiState.yourRecipes
        }

        // Contenido: Grid de recetas o mensaje vacío
        if (recipes.isEmpty() && uiState.activeTab != RecipeTab.YOUR_RECIPES) {
            // Mensaje vacío para tabs normales (excepto "Tus recetas")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (uiState.activeTab) {
                        RecipeTab.ALL -> "📚 No hay recetas disponibles\nExplora el catálogo completo"
                        RecipeTab.COOKABLE -> "🍳 No tienes recetas cocinables\nAñade más productos a tu despensa"
                        RecipeTab.ALMOST -> "📝 No hay recetas casi cocinables\nRevisa tu inventario"
                        RecipeTab.UNDER_30MIN -> "⚡ No hay recetas rápidas disponibles\nExplora otras opciones"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else if (recipes.isEmpty() && uiState.activeTab == RecipeTab.YOUR_RECIPES) {
            // Estado vacío especial para "Tus recetas" con botón prominente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "✏️ Aún no has creado recetas\nEmpieza a añadir tus favoritas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    // Botón destacado para crear primera receta
                    Surface(
                        onClick = { onEvent(RecipesEvent.CreateRecipeClicked) },
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 4.dp,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Crear receta",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Crear tu primera receta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
            // Grid con recetas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeCardGrid(
                        title = recipe.title,
                        prepTime = recipe.prepTime,
                        servings = recipe.servings,
                        imageUrl = recipe.imageUrl,
                        badge = recipe.badge,
                        badgeSeverity = recipe.badgeSeverity,
                        onClick = { onEvent(RecipesEvent.RecipeClicked(recipe.id)) }
                    )
                }

                // "Crear receta" card solo en tab "Tus recetas" cuando YA HAY recetas
                if (uiState.activeTab == RecipeTab.YOUR_RECIPES) {
                    item {
                        Surface(
                            onClick = { onEvent(RecipesEvent.CreateRecipeClicked) },
                            color = PrimaryGreenLight,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Crear receta",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Crear receta",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun RecipesScreenPreview() {
    PantryChefTheme {
        RecipesContent(
            uiState = RecipesUiState(),
            onEvent = {}
        )
    }
}