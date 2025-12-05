package com.pantrychef.front.pantry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.model.enums.Source
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.ShoppingListRepository
import com.pantrychef.back.model.Product
import com.pantrychef.front.components.BadgeSeverity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AlertStatus(
    val message: String,
    val severity: BadgeSeverity,
    val daysUntilExpiry: Int?,
    val actionLabel: String
)

data class RecipeSuggestion(
    val id: String,
    val name: String,
    val time: String,
    val usesAmount: String,
    val status: String
)

data class ProductDetailUiState(
    val productName: String = "",
    val currentQuantity: Float = 0f,
    val unit: String = "",
    val location: String = "",
    val brand: String = "",
    val lowStockThreshold: Float = 0f,
    val suggestedQuantity: String = "",
    val alertStatus: AlertStatus? = null,
    val recipeSuggestions: List<RecipeSuggestion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed interface ProductDetailEvent {
    data class QuantityChanged(val quantity: Float) : ProductDetailEvent
    object IncreaseQuantity : ProductDetailEvent
    object DecreaseQuantity : ProductDetailEvent
    object AddToShoppingList : ProductDetailEvent
    object DeleteProduct : ProductDetailEvent
    data class RecipeClicked(val recipeId: String) : ProductDetailEvent
    object UpdateQuantity : ProductDetailEvent
    object EditClicked : ProductDetailEvent
}

sealed interface ProductDetailNavigation {
    data class ToRecipeDetail(val recipeId: String) : ProductDetailNavigation
    object ToShoppingList : ProductDetailNavigation
    object ToEdit : ProductDetailNavigation
    object Back : ProductDetailNavigation
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<ProductDetailNavigation?>(null)
    val navigation: StateFlow<ProductDetailNavigation?> = _navigation.asStateFlow()

    private var currentProduct: Product? = null

    init {
        loadProduct()
    }

    fun refresh() {
        loadProduct()
    }

    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            is ProductDetailEvent.QuantityChanged -> {
                _uiState.update { it.copy(currentQuantity = event.quantity) }
            }

            ProductDetailEvent.IncreaseQuantity -> {
                val newQuantity = _uiState.value.currentQuantity + 1
                _uiState.update { it.copy(currentQuantity = newQuantity) }
            }

            ProductDetailEvent.DecreaseQuantity -> {
                val newQuantity = (_uiState.value.currentQuantity - 1).coerceAtLeast(0f)
                _uiState.update { it.copy(currentQuantity = newQuantity) }
            }

            ProductDetailEvent.AddToShoppingList -> {
                addToShoppingList()
            }

            ProductDetailEvent.DeleteProduct -> {
                deleteProduct()
            }

            is ProductDetailEvent.RecipeClicked -> {
                _navigation.value = ProductDetailNavigation.ToRecipeDetail(event.recipeId)
            }

            ProductDetailEvent.UpdateQuantity -> {
                updateProductQuantity()
            }

            ProductDetailEvent.EditClicked -> {
                _navigation.value = ProductDetailNavigation.ToEdit
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = productRepository.getProductById(productId)

            result.fold(
                onSuccess = { product ->
                    currentProduct = product

                    val alertStatus = determineAlertStatus(product)

                    // Calcular cantidad sugerida
                    val quantityNeeded = (product.lowStockThreshold * 2 - product.quantity).coerceAtLeast(0f)
                    val suggestedQuantity = formatQuantity(quantityNeeded, product.unit.name.lowercase())

                    _uiState.update { it.copy(
                        productName = product.name,
                        currentQuantity = product.quantity,
                        unit = product.unit.name.lowercase(),
                        location = product.location ?: "Sin ubicación",
                        brand = product.brand ?: "Sin marca",
                        lowStockThreshold = product.lowStockThreshold,
                        suggestedQuantity = suggestedQuantity,
                        alertStatus = alertStatus,
                        recipeSuggestions = emptyList(), // TODO: Load from recipe repository
                        isLoading = false
                    )}
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar producto"
                    )}
                }
            )
        }
    }

    private fun determineAlertStatus(product: Product): AlertStatus? {
        return when {
            product.quantity == 0f -> AlertStatus(
                message = "Sin stock",
                severity = BadgeSeverity.CRITICAL,
                daysUntilExpiry = null,
                actionLabel = "Comprar ahora"
            )
            product.quantity < product.lowStockThreshold * 0.5f -> AlertStatus(
                message = "Stock crítico - Quedan ${product.quantity} ${product.unit.name.lowercase()}",
                severity = BadgeSeverity.URGENT,
                daysUntilExpiry = null,
                actionLabel = "Añadir a lista"
            )
            product.quantity <= product.lowStockThreshold -> AlertStatus(
                message = "Stock bajo - Quedan ${product.quantity} ${product.unit.name.lowercase()}",
                severity = BadgeSeverity.WARNING,
                daysUntilExpiry = null,
                actionLabel = "Reponer pronto"
            )
            else -> null
        }
    }

    private fun addToShoppingList() {
        viewModelScope.launch {
            val product = currentProduct ?: return@launch

            val quantityNeeded = (product.lowStockThreshold * 2 - product.quantity).coerceAtLeast(0f)

            val shoppingItem = ShoppingItem(
                id = "shop-${UUID.randomUUID()}",
                productName = product.name,
                quantity = quantityNeeded,
                unit = product.unit,
                source = Source.MANUAL,
                linkedRecipeId = null,
                isPurchased = false,
                priority = 5
            )

            val result = shoppingListRepository.addItem(shoppingItem)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        successMessage = "Añadido a lista de compra"
                    )}
                    _navigation.value = ProductDetailNavigation.ToShoppingList
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        error = error.message ?: "Error al añadir a lista"
                    )}
                }
            )
        }
    }

    private fun deleteProduct() {
        viewModelScope.launch {
            val result = productRepository.deleteProduct(productId)

            result.fold(
                onSuccess = {
                    _navigation.value = ProductDetailNavigation.Back
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        error = error.message ?: "Error al eliminar producto"
                    )}
                }
            )
        }
    }

    private fun updateProductQuantity() {
        viewModelScope.launch {
            val product = currentProduct ?: return@launch

            val updatedProduct = product.copy(
                quantity = _uiState.value.currentQuantity,
                updatedAt = System.currentTimeMillis()
            )

            val result = productRepository.updateProduct(updatedProduct)

            result.fold(
                onSuccess = {
                    _navigation.value = ProductDetailNavigation.Back
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        error = error.message ?: "Error al actualizar cantidad"
                    )}
                }
            )
        }
    }

    private fun formatQuantity(quantity: Float, unit: String): String {
        return when {
            quantity == 0f -> "0 $unit"
            quantity == quantity.toInt().toFloat() -> "${quantity.toInt()} $unit"
            else -> "${"%.1f".format(quantity)} $unit"
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}