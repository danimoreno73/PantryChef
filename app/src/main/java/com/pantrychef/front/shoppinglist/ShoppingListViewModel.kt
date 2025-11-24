package com.pantrychef.front.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.front.components.BadgeSeverity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingItemUiModel(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isChecked: Boolean,
    val source: String,
    val sourceTag: String,
    val sourceTagSeverity: BadgeSeverity,
    val category: ShoppingCategory
)

enum class ShoppingCategory {
    FROM_RECIPES,    // De recetas
    LOW_STOCK,       // Bajo stock
    OTHERS           // Otros
}

enum class ShoppingFilter {
    ALL,            // Todos
    PENDING,        // Pendientes
    CHECKED         // Marcados
}

data class ShoppingListUiState(
    val items: List<ShoppingItemUiModel> = emptyList(),
    val totalItems: Int = 0,
    val checkedItems: Int = 0,
    val activeFilter: ShoppingFilter = ShoppingFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ShoppingListEvent {
    data class ItemCheckedChanged(val itemId: String, val checked: Boolean) : ShoppingListEvent
    data class QuantityIncreased(val itemId: String) : ShoppingListEvent
    data class QuantityDecreased(val itemId: String) : ShoppingListEvent
    data class FilterChanged(val filter: ShoppingFilter) : ShoppingListEvent
    data class SearchQueryChanged(val query: String) : ShoppingListEvent
    object MarkAllAsPurchased : ShoppingListEvent
    object MoveCheckedToPantry : ShoppingListEvent
    object AddManualItem : ShoppingListEvent
    object Refresh : ShoppingListEvent
}

sealed interface ShoppingListNavigation {
    object ToPantry : ShoppingListNavigation
}

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    // TODO: Inject ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<ShoppingListNavigation?>(null)
    val navigation: StateFlow<ShoppingListNavigation?> = _navigation.asStateFlow()

    init {
        loadShoppingList()
    }

    fun onEvent(event: ShoppingListEvent) {
        when (event) {
            is ShoppingListEvent.ItemCheckedChanged -> {
                val updatedItems = _uiState.value.items.map { item ->
                    if (item.id == event.itemId) {
                        item.copy(isChecked = event.checked)
                    } else {
                        item
                    }
                }
                updateItemsAndStats(updatedItems)
            }

            is ShoppingListEvent.QuantityIncreased -> {
                val updatedItems = _uiState.value.items.map { item ->
                    if (item.id == event.itemId) {
                        item.copy(quantity = item.quantity + 1)
                    } else {
                        item
                    }
                }
                updateItemsAndStats(updatedItems)
            }

            is ShoppingListEvent.QuantityDecreased -> {
                val updatedItems = _uiState.value.items.map { item ->
                    if (item.id == event.itemId && item.quantity > 1) {
                        item.copy(quantity = item.quantity - 1)
                    } else {
                        item
                    }
                }
                updateItemsAndStats(updatedItems)
            }

            is ShoppingListEvent.FilterChanged -> {
                _uiState.update { it.copy(activeFilter = event.filter) }
            }

            is ShoppingListEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }

            ShoppingListEvent.MarkAllAsPurchased -> {
                val updatedItems = _uiState.value.items.map { it.copy(isChecked = true) }
                updateItemsAndStats(updatedItems)
            }

            ShoppingListEvent.MoveCheckedToPantry -> {
                viewModelScope.launch {
                    // TODO: Move checked items to pantry
                    val uncheckedItems = _uiState.value.items.filter { !it.isChecked }
                    updateItemsAndStats(uncheckedItems)
                    _navigation.value = ShoppingListNavigation.ToPantry
                }
            }

            ShoppingListEvent.AddManualItem -> {
                // TODO: Show dialog to add manual item
            }

            ShoppingListEvent.Refresh -> {
                loadShoppingList()
            }
        }
    }

    private fun updateItemsAndStats(items: List<ShoppingItemUiModel>) {
        _uiState.update { it.copy(
            items = items,
            totalItems = items.size,
            checkedItems = items.count { it.isChecked }
        )}
    }

    private fun loadShoppingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Call repository
            kotlinx.coroutines.delay(300)

            val mockItems = getMockShoppingItems()
            _uiState.update { it.copy(
                items = mockItems,
                totalItems = mockItems.size,
                checkedItems = mockItems.count { it.isChecked },
                isLoading = false
            )}
        }
    }

    private fun getMockShoppingItems() = listOf(
        // De recetas
        ShoppingItemUiModel(
            id = "1",
            name = "Tortillas",
            quantity = 12.0,
            unit = "uds",
            isChecked = false,
            source = "Tacos de pollo",
            sourceTag = "Receta",
            sourceTagSeverity = BadgeSeverity.INFO,
            category = ShoppingCategory.FROM_RECIPES
        ),
        ShoppingItemUiModel(
            id = "2",
            name = "Parmesano",
            quantity = 150.0,
            unit = "g",
            isChecked = false,
            source = "Risotto de setas",
            sourceTag = "Receta",
            sourceTagSeverity = BadgeSeverity.INFO,
            category = ShoppingCategory.FROM_RECIPES
        ),

        // Bajo stock
        ShoppingItemUiModel(
            id = "3",
            name = "Leche",
            quantity = 2.0,
            unit = "L",
            isChecked = true,
            source = "Bajo stock",
            sourceTag = "Sugerido",
            sourceTagSeverity = BadgeSeverity.WARNING,
            category = ShoppingCategory.LOW_STOCK
        ),
        ShoppingItemUiModel(
            id = "4",
            name = "Huevos",
            quantity = 1.0,
            unit = "docena",
            isChecked = false,
            source = "Quedan 3",
            sourceTag = "Sugerido",
            sourceTagSeverity = BadgeSeverity.WARNING,
            category = ShoppingCategory.LOW_STOCK
        ),

        // Otros
        ShoppingItemUiModel(
            id = "5",
            name = "Manzanas",
            quantity = 6.0,
            unit = "uds",
            isChecked = false,
            source = "Fruta",
            sourceTag = "Personal",
            sourceTagSeverity = BadgeSeverity.SUCCESS,
            category = ShoppingCategory.OTHERS
        )
    )

    fun clearNavigation() {
        _navigation.value = null
    }
}