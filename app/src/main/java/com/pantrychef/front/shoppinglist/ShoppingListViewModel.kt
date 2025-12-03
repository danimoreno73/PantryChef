package com.pantrychef.front.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.model.enums.Source
import com.pantrychef.back.repository.ShoppingListRepository
import com.pantrychef.back.usecase.BuildSuggestedShoppingListUseCase
import com.pantrychef.front.components.BadgeSeverity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShoppingCategory {
    FROM_RECIPES,
    LOW_STOCK,
    OTHERS
}

enum class ShoppingFilter {
    ALL,
    PENDING,
    CHECKED
}

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

data class ShoppingListUiState(
    val items: List<ShoppingItemUiModel> = emptyList(),
    val activeFilter: ShoppingFilter = ShoppingFilter.ALL,
    val searchQuery: String = "",
    val totalItems: Int = 0,
    val checkedItems: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ShoppingListEvent {
    object GenerateSuggestions : ShoppingListEvent
    data class SearchQueryChanged(val query: String) : ShoppingListEvent
    data class FilterChanged(val filter: ShoppingFilter) : ShoppingListEvent
    data class ItemCheckedChanged(val itemId: String, val isChecked: Boolean) : ShoppingListEvent
    data class QuantityIncreased(val itemId: String) : ShoppingListEvent
    data class QuantityDecreased(val itemId: String) : ShoppingListEvent
    object AddManualItem : ShoppingListEvent
    object MarkAllAsPurchased : ShoppingListEvent
    object MoveCheckedToPantry : ShoppingListEvent
}

sealed interface ShoppingListNavigation {
    object ToPantry : ShoppingListNavigation
}

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val buildSuggestedShoppingListUseCase: BuildSuggestedShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<ShoppingListNavigation?>(null)
    val navigation: StateFlow<ShoppingListNavigation?> = _navigation.asStateFlow()

    private var allItems: List<ShoppingItem> = emptyList()

    init {
        loadShoppingList()
    }

    fun onEvent(event: ShoppingListEvent) {
        when (event) {
            ShoppingListEvent.GenerateSuggestions -> {
                generateSuggestions()
            }

            is ShoppingListEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                updateFilteredItems()
            }

            is ShoppingListEvent.FilterChanged -> {
                _uiState.update { it.copy(activeFilter = event.filter) }
            }

            is ShoppingListEvent.ItemCheckedChanged -> {
                toggleItemChecked(event.itemId, event.isChecked)
            }

            is ShoppingListEvent.QuantityIncreased -> {
                adjustQuantity(event.itemId, increase = true)
            }

            is ShoppingListEvent.QuantityDecreased -> {
                adjustQuantity(event.itemId, increase = false)
            }

            ShoppingListEvent.AddManualItem -> {
                // TODO: Show dialog to add manual item
            }

            ShoppingListEvent.MarkAllAsPurchased -> {
                markAllAsPurchased()
            }

            ShoppingListEvent.MoveCheckedToPantry -> {
                moveCheckedToPantry()
            }
        }
    }

    private fun loadShoppingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                shoppingListRepository.getShoppingList().collect { items ->
                    allItems = items
                    updateFilteredItems()
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar lista"
                )}
            }
        }
    }

    private fun updateFilteredItems() {
        val query = _uiState.value.searchQuery

        var filtered = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter { it.productName.contains(query, ignoreCase = true) }
        }

        val uiItems = filtered.map { mapToUiModel(it) }
        val checkedCount = uiItems.count { it.isChecked }

        _uiState.update { it.copy(
            items = uiItems,
            totalItems = uiItems.size,
            checkedItems = checkedCount
        )}
    }

    private fun generateSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = buildSuggestedShoppingListUseCase()

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al generar sugerencias"
                    )}
                }
            )
        }
    }

    private fun toggleItemChecked(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            val result = if (isChecked) {
                shoppingListRepository.markAsPurchased(itemId)
            } else {
                shoppingListRepository.unmarkAsPurchased(itemId)
            }

            result.fold(
                onSuccess = { /* Flow updates automatically */ },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        error = error.message ?: "Error al actualizar item"
                    )}
                }
            )
        }
    }

    private fun adjustQuantity(itemId: String, increase: Boolean) {
        // TODO: Implement quantity adjustment
        // Necesitarías añadir un método updateQuantity en ShoppingListRepository
    }

    private fun markAllAsPurchased() {
        viewModelScope.launch {
            allItems.forEach { item ->
                if (!item.isPurchased) {
                    shoppingListRepository.markAsPurchased(item.id)
                }
            }
        }
    }

    private fun moveCheckedToPantry() {
        viewModelScope.launch {
            _navigation.value = ShoppingListNavigation.ToPantry
        }
    }

    private fun mapToUiModel(item: ShoppingItem): ShoppingItemUiModel {
        val category = when (item.source) {
            Source.RECIPE -> ShoppingCategory.FROM_RECIPES
            Source.LOW_STOCK -> ShoppingCategory.LOW_STOCK
            Source.MANUAL -> ShoppingCategory.OTHERS
        }

        val (sourceText, sourceTag, tagSeverity) = when (item.source) {
            Source.RECIPE -> Triple("Receta", "De receta", BadgeSeverity.SUCCESS)
            Source.LOW_STOCK -> Triple("Stock bajo", "Sugerido", BadgeSeverity.WARNING)
            Source.MANUAL -> Triple("Manual", "", BadgeSeverity.INFO)  // ✅ INFO en lugar de null
        }

        return ShoppingItemUiModel(
            id = item.id,
            name = item.productName,
            quantity = item.quantity.toDouble(),
            unit = item.unit.name.lowercase(),
            isChecked = item.isPurchased,
            source = sourceText,
            sourceTag = sourceTag,
            sourceTagSeverity = tagSeverity,
            category = category
        )
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}