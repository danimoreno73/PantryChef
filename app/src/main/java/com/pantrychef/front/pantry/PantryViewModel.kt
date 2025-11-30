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

enum class ProductCategory {
    ALL,
    DAIRY,
    PROTEINS,
    GRAINS,
    VEGETABLES,
    FRUITS,
    CONDIMENTS,
    OTHERS
}

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
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val sortBy: SortOption = SortOption.NAME,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface PantryEvent {
    data class SearchQueryChanged(val query: String) : PantryEvent
    data class CategorySelected(val category: ProductCategory) : PantryEvent
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

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<PantryNavigation?>(null)
    val navigation: StateFlow<PantryNavigation?> = _navigation.asStateFlow()

    init {
        loadProducts()
    }

    fun onEvent(event: PantryEvent) {
        when (event) {
            is PantryEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                searchProducts(event.query)
            }

            is PantryEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                loadProductsByCategory(event.category)
            }

            is PantryEvent.SortOptionChanged -> {
                _uiState.update { it.copy(sortBy = event.sortOption) }
                // Re-filter con el nuevo sort
                val currentProducts = _uiState.value.filteredProducts
                _uiState.update { it.copy(filteredProducts = sortProducts(currentProducts)) }
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
                    val productCards = products.map { mapToProductCard(it) }
                    val sorted = sortProducts(productCards)
                    _uiState.update { it.copy(
                        products = sorted,
                        filteredProducts = sorted,
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar productos"
                )}
            }
        }
    }

    private fun loadProductsByCategory(category: ProductCategory) {
        if (category == ProductCategory.ALL) {
            loadProducts()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val categoryEnum = when (category) {
                    ProductCategory.DAIRY -> Category.DAIRY
                    ProductCategory.PROTEINS -> Category.PROTEINS
                    ProductCategory.GRAINS -> Category.GRAINS
                    ProductCategory.VEGETABLES -> Category.VEGETABLES
                    ProductCategory.FRUITS -> Category.FRUITS
                    ProductCategory.CONDIMENTS -> Category.CONDIMENTS
                    ProductCategory.OTHERS -> Category.OTHERS
                    ProductCategory.ALL -> null
                }

                if (categoryEnum != null) {
                    productRepository.getProductsByCategory(categoryEnum).collect { products ->
                        val productCards = products.map { mapToProductCard(it) }
                        val sorted = sortProducts(productCards)
                        _uiState.update { it.copy(
                            filteredProducts = sorted,
                            isLoading = false
                        )}
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al filtrar productos"
                )}
            }
        }
    }

    private fun searchProducts(query: String) {
        if (query.isBlank()) {
            loadProducts()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                productRepository.searchProducts(query).collect { products ->
                    val productCards = products.map { mapToProductCard(it) }
                    val sorted = sortProducts(productCards)
                    _uiState.update { it.copy(
                        filteredProducts = sorted,
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al buscar productos"
                )}
            }
        }
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

    fun clearNavigation() {
        _navigation.value = null
    }
}