package com.pantrychef.front.meallog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.pantrychef.front.components.MealLogCard
import com.pantrychef.front.components.PrimaryButton
import com.pantrychef.front.components.SearchBar
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight
import com.pantrychef.front.theme.TextSecondary

@Composable
fun MealLogScreen(
    navController: NavController,
    viewModel: MealLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            MealLogNavigation.ToRegisterMeal -> {
                // TODO: Navigate to register meal screen
                viewModel.clearNavigation()
            }
            MealLogNavigation.ToStats -> {
                // TODO: Navigate to stats screen
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    MealLogContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun MealLogContent(
    uiState: MealLogUiState,
    onEvent: (MealLogEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            /*Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onEvent(MealLogEvent.CalendarClicked) }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Calendario"
                    )
                }
                IconButton(onClick = { onEvent(MealLogEvent.RegisterMealClicked) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir comida"
                    )
                }
            }*/
        }

        // Search bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { onEvent(MealLogEvent.SearchQueryChanged(it)) },
            placeholder = "Buscar comidas o notas...",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // View mode tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ViewMode.values().forEach { mode ->
                val isSelected = uiState.viewMode == mode

                Surface(
                    onClick = { onEvent(MealLogEvent.ViewModeChanged(mode)) },
                    color = if (isSelected) PrimaryGreenLight else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (mode) {
                            ViewMode.WEEK -> "Semana"
                            ViewMode.MONTH -> "Mes"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total comidas
            Surface(
                color = PrimaryGreenLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🍽",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${uiState.weeklyMealsCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "comidas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Caseras
            Surface(
                color = PrimaryGreenLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🌿",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${uiState.homemadeMealsCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "caseras",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Meals list grouped by date
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            uiState.mealsByDate.forEach { (date, meals) ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(meals) { meal ->
                    MealLogCard(
                        mealName = meal.mealName,
                        mealType = meal.mealType,
                        time = meal.time,
                        calories = meal.calories,
                        imageUrl = meal.imageUrl,
                        status = meal.status,
                        onClick = { onEvent(MealLogEvent.MealClicked(meal.id)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom spacing for button
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        /* Bottom action button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = "Registrar comida",
                    onClick = { onEvent(MealLogEvent.RegisterMealClicked) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = { onEvent(MealLogEvent.ViewStatsClicked) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver estadísticas")
                }
            }
        }*/
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun MealLogScreenPreview() {
    PantryChefTheme {
        MealLogContent(
            uiState = MealLogUiState(
                weeklyMealsCount = 12,
                homemadeMealsCount = 7
            ),
            onEvent = {}
        )
    }
}