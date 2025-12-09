package com.pantrychef.front.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.front.components.ProductAlertLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductCardUiModel(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val location: String,
    val imageUrl: String?,
    val alertLevel: ProductAlertLevel
)

enum class SortOption {
    NAME,
    QUANTITY,
    EXPIRY_DATE,
    CATEGORY
}

data class PantryUiState(
    val products: List<ProductCardUiModel> = emptyList(),
    val filteredProducts: List<ProductCardUiModel> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Todos",
    val categoryOptions: List<String> = emptyList(),
    val sortBy: SortOption = SortOption.NAME,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface PantryEvent {
    data class SearchQueryChanged(val query: String) : PantryEvent
    data class CategorySelected(val category: String) : PantryEvent
    data class SortOptionChanged(val sortOption: SortOption) : PantryEvent
    data class ProductClicked(val productId: String) : PantryEvent
    object AddProductClicked : PantryEvent
    object Refresh : PantryEvent
}

sealed interface PantryNavigation {
    data class ToProductDetail(val productId: String) : PantryNavigation
    object ToAddProduct : PantryNavigation
}

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PantryUiState(
            categoryOptions = listOf("Todos") + Category.values().map { categoryToSpanish(it) }
        )
    )
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<PantryNavigation?>(null)
    val navigation: StateFlow<PantryNavigation?> = _navigation.asStateFlow()

    private var allProducts: List<ProductCardUiModel> = emptyList()

    init {
        loadProducts()
    }

    fun onEvent(event: PantryEvent) {
        when (event) {
            is PantryEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                applyFilters()
            }

            is PantryEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                applyFilters()
            }

            is PantryEvent.SortOptionChanged -> {
                _uiState.update { it.copy(sortBy = event.sortOption) }
                applyFilters()
            }

            is PantryEvent.ProductClicked -> {
                _navigation.value = PantryNavigation.ToProductDetail(event.productId)
            }

            PantryEvent.AddProductClicked -> {
                _navigation.value = PantryNavigation.ToAddProduct
            }

            PantryEvent.Refresh -> {
                loadProducts()
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                productRepository.getAllProducts().collect { products ->
                    allProducts = products.map { mapToProductCard(it) }

                    _uiState.update { it.copy(
                        products = allProducts,
                        isLoading = false
                    )}

                    applyFilters()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar productos"
                )}
            }
        }
    }

    private fun applyFilters() {
        var filtered = allProducts

        // 1. Filtrar por categoría
        val selectedCategory = _uiState.value.selectedCategory
        if (selectedCategory != "Todos") {
            // Convertir español a Category enum
            val category = spanishToCategory(selectedCategory)
            filtered = filtered.filter { it.category == category.name }
        }

        // 2. Filtrar por búsqueda
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }

        // 3. Ordenar
        val sorted = sortProducts(filtered)

        _uiState.update { it.copy(filteredProducts = sorted) }
    }

    private fun sortProducts(products: List<ProductCardUiModel>): List<ProductCardUiModel> {
        return when (_uiState.value.sortBy) {
            SortOption.NAME -> products.sortedBy { it.name }
            SortOption.CATEGORY -> products.sortedBy { it.category }
            SortOption.QUANTITY -> products.sortedBy { it.quantity }
            SortOption.EXPIRY_DATE -> products.sortedByDescending { it.alertLevel.ordinal }
        }
    }

    private fun mapToProductCard(product: Product): ProductCardUiModel {
        val alertLevel = when {
            product.quantity == 0f -> ProductAlertLevel.EXPIRED
            product.quantity < product.lowStockThreshold * 0.5f -> ProductAlertLevel.EXPIRING_SOON
            product.quantity <= product.lowStockThreshold -> ProductAlertLevel.LOW_STOCK
            else -> ProductAlertLevel.NONE
        }

        return ProductCardUiModel(
            id = product.id,
            name = product.name,
            quantity = "${product.quantity} ${product.unit.name.lowercase()}",
            category = product.category.name,
            location = product.location ?: "Sin ubicación",
            imageUrl = null,
            alertLevel = alertLevel
        )
    }

    // ========== MAPEO DE CATEGORY ==========

    private fun categoryToSpanish(category: Category): String {
        return when (category) {
            Category.DAIRY -> "Lácteos"
            Category.PROTEINS -> "Proteínas"
            Category.GRAINS -> "Granos"
            Category.VEGETABLES -> "Verduras"
            Category.FRUITS -> "Frutas"
            Category.CONDIMENTS -> "Condimentos"
            Category.OTHERS -> "Otros"
        }
    }

    private fun spanishToCategory(spanish: String): Category {
        return when (spanish) {
            "Lácteos" -> Category.DAIRY
            "Proteínas" -> Category.PROTEINS
            "Granos" -> Category.GRAINS
            "Verduras" -> Category.VEGETABLES
            "Frutas" -> Category.FRUITS
            "Condimentos" -> Category.CONDIMENTS
            "Otros" -> Category.OTHERS
            else -> Category.OTHERS
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}