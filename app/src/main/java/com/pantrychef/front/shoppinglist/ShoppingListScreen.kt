package com.pantrychef.front.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.pantrychef.front.components.PrimaryButton
import com.pantrychef.front.components.SearchBar
import com.pantrychef.front.components.ShoppingItemRow
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight
import com.pantrychef.front.theme.TextSecondary

@Composable
fun ShoppingListScreen(
    navController: NavController,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            ShoppingListNavigation.ToPantry -> {
                navController.navigate(Routes.PANTRY)
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    ShoppingListContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ShoppingListContent(
    uiState: ShoppingListUiState,
    onEvent: (ShoppingListEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header - SIN botones de compartir y menú
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lista de compra",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Search bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { onEvent(ShoppingListEvent.SearchQueryChanged(it)) },
            placeholder = "Buscar en la lista...",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        Surface(
            color = PrimaryGreenLight,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.totalItems}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "artículos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.checkedItems}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "marcados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter tabs
        ScrollableTabRow(
            selectedTabIndex = uiState.activeFilter.ordinal,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background,
            edgePadding = 16.dp
        ) {
            ShoppingFilter.values().forEach { filter ->
                val isSelected = uiState.activeFilter == filter

                Tab(
                    selected = isSelected,
                    onClick = { onEvent(ShoppingListEvent.FilterChanged(filter)) }
                ) {
                    Surface(
                        color = if (isSelected) PrimaryGreenLight else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = when (filter) {
                                ShoppingFilter.ALL -> "≡ Todos"
                                ShoppingFilter.PENDING -> "○ Pendientes"
                                ShoppingFilter.CHECKED -> "✓ Marcados"
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

        // Items list
        val filteredItems = when (uiState.activeFilter) {
            ShoppingFilter.ALL -> uiState.items
            ShoppingFilter.PENDING -> uiState.items.filter { !it.isChecked }
            ShoppingFilter.CHECKED -> uiState.items.filter { it.isChecked }
        }

        val itemsByCategory = filteredItems.groupBy { it.category }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // De recetas section
            itemsByCategory[ShoppingCategory.FROM_RECIPES]?.let { items ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "De recetas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                onEvent(ShoppingListEvent.AddAllFromCategory(ShoppingCategory.FROM_RECIPES))
                            }
                        ) {
                            Text("Añadir todo")
                        }
                    }
                }

                items(items) { item ->
                    ShoppingItemRow(
                        name = item.name,
                        quantity = item.quantity,
                        unit = item.unit,
                        isChecked = item.isChecked,
                        source = item.source,
                        sourceTag = item.sourceTag,
                        sourceTagSeverity = item.sourceTagSeverity,
                        onCheckedChange = { checked ->
                            onEvent(ShoppingListEvent.ItemCheckedChanged(item.id, checked))
                        }
                    )
                }
            }

            // Bajo stock section
            itemsByCategory[ShoppingCategory.LOW_STOCK]?.let { items ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sugeridos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                onEvent(ShoppingListEvent.AddAllFromCategory(ShoppingCategory.LOW_STOCK))
                            }
                        ) {
                            Text("Añadir todo")
                        }
                    }
                }

                items(items) { item ->
                    ShoppingItemRow(
                        name = item.name,
                        quantity = item.quantity,
                        unit = item.unit,
                        isChecked = item.isChecked,
                        source = item.source,
                        sourceTag = item.sourceTag,
                        sourceTagSeverity = item.sourceTagSeverity,
                        onCheckedChange = { checked ->
                            onEvent(ShoppingListEvent.ItemCheckedChanged(item.id, checked))
                        }
                    )
                }
            }

            // Otros section
            itemsByCategory[ShoppingCategory.OTHERS]?.let { items ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Otros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { onEvent(ShoppingListEvent.AddManualItem) }) {
                            Text("Añadir artículo")
                        }
                    }
                }

                items(items) { item ->
                    ShoppingItemRow(
                        name = item.name,
                        quantity = item.quantity,
                        unit = item.unit,
                        isChecked = item.isChecked,
                        source = item.source,
                        sourceTag = item.sourceTag,
                        sourceTagSeverity = item.sourceTagSeverity,
                        onCheckedChange = { checked ->
                            onEvent(ShoppingListEvent.ItemCheckedChanged(item.id, checked))
                        }
                    )
                }
            }
        }

        // Bottom action button - SOLO UNO
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                PrimaryButton(
                    text = "Marcar todo como comprado",
                    onClick = { onEvent(ShoppingListEvent.MarkAllAsPurchased) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.checkedItems > 0
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ShoppingListScreenPreview() {
    PantryChefTheme {
        ShoppingListContent(
            uiState = ShoppingListUiState(
                totalItems = 7,
                checkedItems = 3
            ),
            onEvent = {}
        )
    }
}