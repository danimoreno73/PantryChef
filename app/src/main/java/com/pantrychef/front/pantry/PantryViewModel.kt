package com.pantrychef.front.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    // TODO: Inject ProductRepository
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
                filterProducts()
            }

            is PantryEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                filterProducts()
            }

            is PantryEvent.SortOptionChanged -> {
                _uiState.update { it.copy(sortBy = event.sortOption) }
                filterProducts()
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

            // TODO: Call repository
            kotlinx.coroutines.delay(300)

            val mockProducts = getMockProducts()
            _uiState.update { it.copy(
                products = mockProducts,
                filteredProducts = mockProducts,
                isLoading = false
            )}
        }
    }

    private fun filterProducts() {
        val products = _uiState.value.products
        val query = _uiState.value.searchQuery
        val category = _uiState.value.selectedCategory

        var filtered = products

        // Filter by category
        if (category != ProductCategory.ALL) {
            filtered = filtered.filter { product ->
                product.category.contains(category.name, ignoreCase = true) ||
                        when (category) {
                            ProductCategory.DAIRY -> product.category.contains("lácteo", ignoreCase = true)
                            ProductCategory.PROTEINS -> product.category.contains("proteína", ignoreCase = true)
                            ProductCategory.GRAINS -> product.category.contains("grano", ignoreCase = true)
                            ProductCategory.VEGETABLES -> product.category.contains("verdura", ignoreCase = true)
                            ProductCategory.FRUITS -> product.category.contains("fruta", ignoreCase = true)
                            ProductCategory.CONDIMENTS -> product.category.contains("condimento", ignoreCase = true)
                            else -> true
                        }
            }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            filtered = filtered.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true) ||
                        product.location.contains(query, ignoreCase = true)
            }
        }

        // Sort
        filtered = when (_uiState.value.sortBy) {
            SortOption.NAME -> filtered.sortedBy { it.name }
            SortOption.CATEGORY -> filtered.sortedBy { it.category }
            SortOption.QUANTITY -> filtered.sortedBy { it.quantity }
            SortOption.EXPIRY_DATE -> filtered.sortedByDescending { it.alertLevel.ordinal }
        }

        _uiState.update { it.copy(filteredProducts = filtered) }
    }

    private fun getMockProducts() = listOf(
        ProductCardUiModel(
            id = "1",
            name = "Leche entera",
            quantity = "2.0 L",
            category = "Lácteos",
            location = "Refrigerador",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        ),
        ProductCardUiModel(
            id = "2",
            name = "Yogur natural",
            quantity = "6 uds",
            category = "Lácteos",
            location = "Refrigerador",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        ),
        ProductCardUiModel(
            id = "3",
            name = "Huevos",
            quantity = "3 uds",
            category = "Proteínas",
            location = "Refrigerador",
            imageUrl = null,
            alertLevel = ProductAlertLevel.LOW_STOCK
        ),
        ProductCardUiModel(
            id = "4",
            name = "Pechuga de pollo",
            quantity = "0.6 kg",
            category = "Proteínas",
            location = "Refrigerador",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        ),
        ProductCardUiModel(
            id = "5",
            name = "Arroz",
            quantity = "1.5 kg",
            category = "Granos",
            location = "Despensa",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        ),
        ProductCardUiModel(
            id = "6",
            name = "Pasta",
            quantity = "0.3 kg",
            category = "Granos",
            location = "Despensa",
            imageUrl = null,
            alertLevel = ProductAlertLevel.LOW_STOCK
        ),
        ProductCardUiModel(
            id = "7",
            name = "Tomates",
            quantity = "4 uds",
            category = "Verduras",
            location = "Frutero",
            imageUrl = null,
            alertLevel = ProductAlertLevel.EXPIRING_SOON
        ),
        ProductCardUiModel(
            id = "8",
            name = "Cebollas",
            quantity = "2 uds",
            category = "Verduras",
            location = "Despensa",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        ),
        ProductCardUiModel(
            id = "9",
            name = "Manzanas",
            quantity = "1.0 kg",
            category = "Frutas",
            location = "Frutero",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        ),
        ProductCardUiModel(
            id = "10",
            name = "Plátanos",
            quantity = "5 uds",
            category = "Frutas",
            location = "Frutero",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        ),
        ProductCardUiModel(
            id = "11",
            name = "Aceite de oliva",
            quantity = "0.4 L",
            category = "Condimentos",
            location = "Despensa",
            imageUrl = null,
            alertLevel = ProductAlertLevel.LOW_STOCK
        ),
        ProductCardUiModel(
            id = "12",
            name = "Sal fina",
            quantity = "0.2 kg",
            category = "Condimentos",
            location = "Despensa",
            imageUrl = null,
            alertLevel = ProductAlertLevel.NONE
        )
    )

    fun clearNavigation() {
        _navigation.value = null
    }
}