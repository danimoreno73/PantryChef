package com.pantrychef.front.pantry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.model.enums.Source
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.ShoppingListRepository
import com.pantrychef.back.model.Product
import com.pantrychef.back.usecase.GetRecipesByIngredientUseCase
import com.pantrychef.back.utils.UnitsConverter
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

data class AlertStatus(
    val message: String,
    val severity: BadgeSeverity
)

data class RecipeSuggestion(
    val id: String,
    val name: String,
    val time: String,
    val usesAmount: String
)

data class ProductDetailUiState(
    val productName: String = "",
    val category: String = "",
    val categoryEmoji: String = "",
    val location: String = "",
    val brand: String = "",
    val originalQuantity: Float = 0f,
    val currentQuantity: Float = 0f,
    val unit: String = "",
    val lowStockThreshold: Float = 0f,
    val suggestedQuantity: String = "",
    val suggestedAmount: Float = 0f,
    val alertStatus: AlertStatus? = null,
    val recipeSuggestions: List<RecipeSuggestion> = emptyList(),
    val hasUnsavedChanges: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ProductDetailEvent {
    object IncreaseQuantity : ProductDetailEvent
    object DecreaseQuantity : ProductDetailEvent
    object SaveQuantityChanges : ProductDetailEvent
    object AddToShoppingList : ProductDetailEvent
    data class RecipeClicked(val recipeId: String) : ProductDetailEvent
    object EditClicked : ProductDetailEvent
    object DeleteClicked : ProductDetailEvent
    object ConfirmDelete : ProductDetailEvent
    object DismissDeleteDialog : ProductDetailEvent
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
    private val getRecipesByIngredientUseCase: GetRecipesByIngredientUseCase,
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
            ProductDetailEvent.IncreaseQuantity -> {
                val newQuantity = _uiState.value.currentQuantity + 1
                _uiState.update { it.copy(
                    currentQuantity = newQuantity,
                    hasUnsavedChanges = newQuantity != it.originalQuantity
                )}
            }

            ProductDetailEvent.DecreaseQuantity -> {
                val newQuantity = (_uiState.value.currentQuantity - 1).coerceAtLeast(0f)
                _uiState.update { it.copy(
                    currentQuantity = newQuantity,
                    hasUnsavedChanges = newQuantity != it.originalQuantity
                )}
            }

            ProductDetailEvent.SaveQuantityChanges -> {
                saveQuantityChanges()
            }

            ProductDetailEvent.AddToShoppingList -> {
                addToShoppingList()
            }

            is ProductDetailEvent.RecipeClicked -> {
                _navigation.value = ProductDetailNavigation.ToRecipeDetail(event.recipeId)
            }

            ProductDetailEvent.EditClicked -> {
                _navigation.value = ProductDetailNavigation.ToEdit
            }

            ProductDetailEvent.DeleteClicked -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }

            ProductDetailEvent.ConfirmDelete -> {
                deleteProduct()
            }

            ProductDetailEvent.DismissDeleteDialog -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = productRepository.getProductById(productId)

                result.fold(
                    onSuccess = { product ->
                        currentProduct = product

                        // Determinar alerta de stock
                        val alertStatus = determineAlertStatus(product)

                        // Calcular cantidad sugerida
                        val quantityNeeded = (product.lowStockThreshold * 2 - product.quantity).coerceAtLeast(0f)
                        val suggestedQuantity = UnitsConverter.formatQuantityShort(quantityNeeded, product.unit)

                        // Cargar recetas que usan este producto
                        val recipeSuggestions = loadRecipeSuggestions(product)

                        // Categoría con emoji
                        val (categoryName, emoji) = categoryToSpanishWithEmoji(product.category)

                        _uiState.update { it.copy(
                            productName = product.name,
                            category = categoryName,
                            categoryEmoji = emoji,
                            location = product.location ?: "Sin ubicación",
                            brand = product.brand ?: "Sin marca",
                            originalQuantity = product.quantity,
                            currentQuantity = product.quantity,
                            unit = UnitsConverter.getUnitAbbreviation(product.unit),
                            lowStockThreshold = product.lowStockThreshold,
                            suggestedQuantity = suggestedQuantity,
                            suggestedAmount = quantityNeeded,
                            alertStatus = alertStatus,
                            recipeSuggestions = recipeSuggestions,
                            hasUnsavedChanges = false,
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
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar producto"
                )}
            }
        }
    }

    private suspend fun loadRecipeSuggestions(product: Product): List<RecipeSuggestion> {
        return try {
            val recipes = getRecipesByIngredientUseCase(product).first()

            recipes.take(3).map { recipe ->
                val ingredient = recipe.ingredients.find {
                    it.productName.trim().equals(product.name.trim(), ignoreCase = true)
                }

                RecipeSuggestion(
                    id = recipe.id,
                    name = recipe.name,
                    time = "${recipe.prepTimeMinutes} min",
                    usesAmount = ingredient?.let {
                        "Usa ${UnitsConverter.formatQuantityShort(it.quantity, it.unit)}"
                    } ?: "Ingrediente"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun determineAlertStatus(product: Product): AlertStatus? {
        return when {
            product.quantity == 0f -> AlertStatus(
                message = "Sin stock - Comprar urgente",
                severity = BadgeSeverity.CRITICAL
            )
            product.quantity < product.lowStockThreshold * 0.5f -> AlertStatus(
                message = "Stock crítico - Quedan ${UnitsConverter.formatQuantityShort(product.quantity, product.unit)}",
                severity = BadgeSeverity.URGENT
            )
            product.quantity <= product.lowStockThreshold -> AlertStatus(
                message = "Stock bajo - Quedan ${UnitsConverter.formatQuantityShort(product.quantity, product.unit)}",
                severity = BadgeSeverity.WARNING
            )
            else -> null
        }
    }

    private fun categoryToSpanishWithEmoji(category: com.pantrychef.back.model.enums.Category): Pair<String, String> {
        return when (category) {
            com.pantrychef.back.model.enums.Category.DAIRY -> "Lácteos" to "🥛"
            com.pantrychef.back.model.enums.Category.PROTEINS -> "Proteínas" to "🥩"
            com.pantrychef.back.model.enums.Category.GRAINS -> "Granos" to "🌾"
            com.pantrychef.back.model.enums.Category.VEGETABLES -> "Verduras" to "🥬"
            com.pantrychef.back.model.enums.Category.FRUITS -> "Frutas" to "🍎"
            com.pantrychef.back.model.enums.Category.CONDIMENTS -> "Condimentos" to "🧂"
            com.pantrychef.back.model.enums.Category.OTHERS -> "Otros" to "📦"
        }
    }

    private fun saveQuantityChanges() {
        viewModelScope.launch {
            val product = currentProduct ?: return@launch

            _uiState.update { it.copy(isLoading = true) }

            val updatedProduct = product.copy(
                quantity = _uiState.value.currentQuantity,
                updatedAt = System.currentTimeMillis()
            )

            val result = productRepository.updateProduct(updatedProduct)

            result.fold(
                onSuccess = {
                    // Recargar para actualizar todo
                    loadProduct()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al actualizar cantidad"
                    )}
                }
            )
        }
    }

    private fun addToShoppingList() {
        viewModelScope.launch {
            val product = currentProduct ?: return@launch

            val quantityToAdd = _uiState.value.suggestedAmount

            val shoppingItem = ShoppingItem(
                id = "shop-${UUID.randomUUID()}",
                productName = product.name,
                quantity = quantityToAdd,
                unit = product.unit,
                source = Source.MANUAL,
                linkedRecipeId = null,
                isPurchased = false,
                priority = 5
            )

            val result = shoppingListRepository.addItem(shoppingItem)

            result.fold(
                onSuccess = {
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
            _uiState.update { it.copy(
                isLoading = true,
                showDeleteDialog = false
            )}

            val result = productRepository.deleteProduct(productId)

            result.fold(
                onSuccess = {
                    _navigation.value = ProductDetailNavigation.Back
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al eliminar producto"
                    )}
                }
            )
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}