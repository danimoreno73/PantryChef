package com.pantrychef.front.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Source
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.ShoppingListRepository
import com.pantrychef.back.usecase.BuildSuggestedShoppingListUseCase
import com.pantrychef.front.components.BadgeSeverity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
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
    data class AddAllFromCategory(val category: ShoppingCategory) : ShoppingListEvent
    object AddManualItem : ShoppingListEvent
    object MarkAllAsPurchased : ShoppingListEvent
}

sealed interface ShoppingListNavigation {
    object ToPantry : ShoppingListNavigation
}

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val productRepository: ProductRepository,
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

            is ShoppingListEvent.AddAllFromCategory -> {
                addAllFromCategory(event.category)
            }

            ShoppingListEvent.AddManualItem -> {
                // TODO: Show dialog to add manual item
            }

            ShoppingListEvent.MarkAllAsPurchased -> {
                markAllAsPurchased()
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

    private fun addAllFromCategory(category: ShoppingCategory) {
        viewModelScope.launch {
            val itemsToAdd = allItems.filter { item ->
                val uiCategory = when (item.source) {
                    Source.RECIPE -> ShoppingCategory.FROM_RECIPES
                    Source.LOW_STOCK -> ShoppingCategory.LOW_STOCK
                    Source.MANUAL -> ShoppingCategory.OTHERS
                }
                uiCategory == category && !item.isPurchased
            }

            itemsToAdd.forEach { item ->
                shoppingListRepository.markAsPurchased(item.id)
            }
        }
    }

    private fun markAllAsPurchased() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // 1. Obtener todos los items marcados
                val purchasedItems = allItems.filter { it.isPurchased }

                if (purchasedItems.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No hay items marcados"
                    )}
                    return@launch
                }

                // 2. Obtener todos los productos actuales de la despensa
                val existingProducts = productRepository.getAllProducts().first()

                // 3. Para cada item comprado, añadir o actualizar en despensa
                purchasedItems.forEach { item ->
                    val existingProduct = existingProducts.find {
                        it.name.equals(item.productName, ignoreCase = true)
                    }

                    if (existingProduct != null) {
                        // Producto existe -> Incrementar cantidad
                        val updatedProduct = existingProduct.copy(
                            quantity = existingProduct.quantity + item.quantity,
                            updatedAt = System.currentTimeMillis()
                        )
                        productRepository.updateProduct(updatedProduct)
                    } else {
                        // Producto no existe -> Crear nuevo
                        val newProduct = Product(
                            id = UUID.randomUUID().toString(),
                            name = item.productName,
                            category = Category.OTHERS, // Categoría por defecto
                            quantity = item.quantity,
                            unit = item.unit,
                            lowStockThreshold = 1.0f,
                            location = null,
                            brand = null,
                            updatedAt = System.currentTimeMillis()
                        )
                        productRepository.addProduct(newProduct)
                    }

                    // 4. Eliminar item de la lista de compras
                    shoppingListRepository.removeItem(item.id)
                }

                // 5. Navegar a despensa
                _uiState.update { it.copy(isLoading = false) }
                _navigation.value = ShoppingListNavigation.ToPantry

            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al mover items a despensa"
                )}
            }
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
            Source.MANUAL -> Triple("Manual", "", BadgeSeverity.INFO)
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