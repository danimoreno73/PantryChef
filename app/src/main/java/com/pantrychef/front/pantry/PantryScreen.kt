package com.pantrychef.front.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.pantrychef.front.components.ProductCard
import com.pantrychef.front.components.SearchBar
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight

@Composable
fun PantryScreen(
    navController: NavController,
    viewModel: PantryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (val nav = navigation) {
            is PantryNavigation.ToProductDetail -> {
                navController.navigate(Routes.productDetail(nav.productId))
                viewModel.clearNavigation()
            }
            PantryNavigation.ToAddProduct -> {
                navController.navigate(Routes.PANTRY_ADD)
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    PantryContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun PantryContent(
    uiState: PantryUiState,
    onEvent: (PantryEvent) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(PantryEvent.AddProductClicked) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Añadir producto",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Despensa",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.filteredProducts.size} productos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(PantryEvent.SearchQueryChanged(it)) },
                placeholder = "Buscar productos...",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category filters
            ScrollableTabRow(
                selectedTabIndex = uiState.categoryOptions.indexOf(uiState.selectedCategory),
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.background,
                edgePadding = 16.dp
            ) {
                uiState.categoryOptions.forEach { category ->
                    val isSelected = uiState.selectedCategory == category

                    Tab(
                        selected = isSelected,
                        onClick = { onEvent(PantryEvent.CategorySelected(category)) }
                    ) {
                        Surface(
                            color = if (isSelected) PrimaryGreenLight else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
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

            // Products grid
            if (uiState.filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📦",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "No hay productos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotBlank())
                                "No se encontraron productos con '${uiState.searchQuery}'"
                            else
                                "Añade productos a tu despensa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredProducts) { product ->
                        ProductCard(
                            name = product.name,
                            quantity = product.quantity,
                            category = product.category,
                            location = product.location,
                            imageUrl = product.imageUrl,
                            alertLevel = product.alertLevel,
                            onClick = { onEvent(PantryEvent.ProductClicked(product.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun PantryScreenPreview() {
    PantryChefTheme {
        PantryContent(
            uiState = PantryUiState(
                categoryOptions = listOf("Todos", "Lácteos", "Proteínas", "Granos", "Verduras", "Frutas", "Condimentos", "Otros")
            ),
            onEvent = {}
        )
    }
}