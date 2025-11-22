package com.pantrychef.front.pantry

import androidx.lifecycle.SavedStateHandle
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

data class ProductDetailUiState(
    val productName: String = "",
    val currentQuantity: Double = 0.0,
    val unit: String = "",
    val location: String = "",
    val brand: String = "",
    val lowStockThreshold: Double = 0.0,
    val alertStatus: AlertStatus? = null,
    val recipeSuggestions: List<RecipeSuggestion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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
    val status: BadgeSeverity
)

sealed interface ProductDetailEvent {
    data class QuantityChanged(val quantity: Double) : ProductDetailEvent
    object IncreaseQuantity : ProductDetailEvent
    object DecreaseQuantity : ProductDetailEvent
    object AddToShoppingList : ProductDetailEvent
    object MarkAsResolved : ProductDetailEvent
    object DiscardRemaining : ProductDetailEvent
    data class RecipeClicked(val recipeId: String) : ProductDetailEvent
    object EditProduct : ProductDetailEvent
}

sealed interface ProductDetailNavigation {
    data class ToRecipe(val recipeId: String) : ProductDetailNavigation
    object ToShoppingList : ProductDetailNavigation
    object Back : ProductDetailNavigation
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
    // TODO: Inject ProductRepository
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<ProductDetailNavigation?>(null)
    val navigation: StateFlow<ProductDetailNavigation?> = _navigation.asStateFlow()

    init {
        loadProductDetail()
    }

    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            is ProductDetailEvent.QuantityChanged -> {
                _uiState.update { it.copy(currentQuantity = event.quantity) }
            }

            ProductDetailEvent.IncreaseQuantity -> {
                val newQuantity = _uiState.value.currentQuantity + 0.5
                _uiState.update { it.copy(currentQuantity = newQuantity) }
            }

            ProductDetailEvent.DecreaseQuantity -> {
                val newQuantity = (_uiState.value.currentQuantity - 0.5).coerceAtLeast(0.0)
                _uiState.update { it.copy(currentQuantity = newQuantity) }
            }

            ProductDetailEvent.AddToShoppingList -> {
                // TODO: Implement
                _navigation.value = ProductDetailNavigation.ToShoppingList
            }

            ProductDetailEvent.MarkAsResolved -> {
                // TODO: Implement - actualizar alerta
            }

            ProductDetailEvent.DiscardRemaining -> {
                // TODO: Implement - eliminar producto
                _navigation.value = ProductDetailNavigation.Back
            }

            is ProductDetailEvent.RecipeClicked -> {
                _navigation.value = ProductDetailNavigation.ToRecipe(event.recipeId)
            }

            ProductDetailEvent.EditProduct -> {
                // TODO: Navigate to edit screen
            }
        }
    }

    private fun loadProductDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Call repository
            kotlinx.coroutines.delay(300)

            // Mock data
            _uiState.update { it.copy(
                productName = "Leche",
                currentQuantity = 0.5,
                unit = "L",
                location = "Refrigerador",
                brand = "Entera",
                lowStockThreshold = 1.0,
                alertStatus = AlertStatus(
                    message = "Baja en 2 días",
                    severity = BadgeSeverity.WARNING,
                    daysUntilExpiry = 2,
                    actionLabel = "Reponer"
                ),
                recipeSuggestions = listOf(
                    RecipeSuggestion(
                        id = "1",
                        name = "Batido de frutas",
                        time = "15 min",
                        usesAmount = "Usa 0.3 L de leche",
                        status = BadgeSeverity.SUCCESS
                    ),
                    RecipeSuggestion(
                        id = "2",
                        name = "Crepes sencillos",
                        time = "20 min",
                        usesAmount = "Falta: harina",
                        status = BadgeSeverity.WARNING
                    )
                ),
                isLoading = false
            )}
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}