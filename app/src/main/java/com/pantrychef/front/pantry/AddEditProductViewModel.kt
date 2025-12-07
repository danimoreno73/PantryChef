package com.pantrychef.front.pantry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Unit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditProductUiState(
    val productId: String? = null,
    val name: String = "",
    val category: String = "Lácteos",
    val quantity: String = "",
    val unit: String = "g",
    val location: String = "Refrigerador",
    val brand: String = "",
    val expiryDate: String? = null,
    val lowStockThreshold: String = "",
    val categoryOptions: List<String> = emptyList(),
    val unitOptions: List<String> = emptyList(),
    val locationOptions: List<String> = listOf(
        "Refrigerador",
        "Despensa",
        "Congelador",
        "Frutero",
        "Especiero",
        "Otro"
    ),
    val nameError: String? = null,
    val quantityError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface AddEditProductEvent {
    data class NameChanged(val name: String) : AddEditProductEvent
    data class CategoryChanged(val category: String) : AddEditProductEvent
    data class QuantityChanged(val quantity: String) : AddEditProductEvent
    data class UnitChanged(val unit: String) : AddEditProductEvent
    data class LocationChanged(val location: String) : AddEditProductEvent
    data class BrandChanged(val brand: String) : AddEditProductEvent
    data class ExpiryDateChanged(val date: String?) : AddEditProductEvent
    data class LowStockThresholdChanged(val threshold: String) : AddEditProductEvent
    object SaveClicked : AddEditProductEvent
    object DeleteClicked : AddEditProductEvent
}

sealed interface AddEditProductNavigation {
    object Back : AddEditProductNavigation
    object ToPantry : AddEditProductNavigation
}

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow(
        AddEditProductUiState(
            productId = productId,
            categoryOptions = Category.values().map { categoryToSpanish(it) },
            unitOptions = Unit.values().map { unitToSpanish(it) }
        )
    )
    val uiState: StateFlow<AddEditProductUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<AddEditProductNavigation?>(null)
    val navigation: StateFlow<AddEditProductNavigation?> = _navigation.asStateFlow()

    init {
        if (productId != null) {
            loadProduct(productId)
        }
    }

    fun onEvent(event: AddEditProductEvent) {
        when (event) {
            is AddEditProductEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, nameError = null) }
            }

            is AddEditProductEvent.CategoryChanged -> {
                _uiState.update { it.copy(category = event.category) }
            }

            is AddEditProductEvent.QuantityChanged -> {
                _uiState.update { it.copy(quantity = event.quantity, quantityError = null) }
            }

            is AddEditProductEvent.UnitChanged -> {
                _uiState.update { it.copy(unit = event.unit) }
            }

            is AddEditProductEvent.LocationChanged -> {
                _uiState.update { it.copy(location = event.location) }
            }

            is AddEditProductEvent.BrandChanged -> {
                _uiState.update { it.copy(brand = event.brand) }
            }

            is AddEditProductEvent.ExpiryDateChanged -> {
                _uiState.update { it.copy(expiryDate = event.date) }
            }

            is AddEditProductEvent.LowStockThresholdChanged -> {
                _uiState.update { it.copy(lowStockThreshold = event.threshold) }
            }

            AddEditProductEvent.SaveClicked -> {
                saveProduct()
            }

            AddEditProductEvent.DeleteClicked -> {
                deleteProduct()
            }
        }
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = productRepository.getProductById(id)

            result.fold(
                onSuccess = { product ->
                    _uiState.update { it.copy(
                        name = product.name,
                        category = categoryToSpanish(product.category),
                        quantity = product.quantity.toString(),
                        unit = unitToSpanish(product.unit),
                        location = product.location ?: "Refrigerador",
                        brand = product.brand ?: "",
                        expiryDate = null, // TODO: Add expiry date to model
                        lowStockThreshold = product.lowStockThreshold.toString(),
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

    private fun saveProduct() {
        // Validación
        val nameError = if (_uiState.value.name.isBlank()) "El nombre es requerido" else null
        val quantityError = if (_uiState.value.quantity.isBlank()) "La cantidad es requerida" else null

        if (nameError != null || quantityError != null) {
            _uiState.update { it.copy(
                nameError = nameError,
                quantityError = quantityError
            )}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val product = Product(
                id = productId ?: UUID.randomUUID().toString(),
                name = _uiState.value.name,
                category = spanishToCategory(_uiState.value.category),
                quantity = _uiState.value.quantity.toFloatOrNull() ?: 0f,
                unit = spanishToUnit(_uiState.value.unit),
                lowStockThreshold = _uiState.value.lowStockThreshold.toFloatOrNull() ?: 0f,
                location = _uiState.value.location.ifBlank { null },
                brand = _uiState.value.brand.ifBlank { null },
                updatedAt = System.currentTimeMillis()
            )

            val result = if (productId != null) {
                productRepository.updateProduct(product)
            } else {
                productRepository.addProduct(product)
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigation.value = AddEditProductNavigation.Back
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al guardar producto"
                    )}
                }
            )
        }
    }

    private fun deleteProduct() {
        if (productId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = productRepository.deleteProduct(productId)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigation.value = AddEditProductNavigation.ToPantry
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


    private fun unitToSpanish(unit: Unit): String {
        return when (unit) {
            Unit.GRAMS -> "g"
            Unit.KILOGRAMS -> "kg"
            Unit.LITERS -> "L"
            Unit.MILLILITERS -> "ml"
            Unit.UNITS -> "uds"
            Unit.TABLESPOONS -> "cdas"
            Unit.CUPS -> "tazas"
            Unit.PACKAGES -> "paquetes"
            Unit.DOZEN -> "docena"
        }
    }

    private fun spanishToUnit(spanish: String): Unit {
        return when (spanish) {
            "g" -> Unit.GRAMS
            "kg" -> Unit.KILOGRAMS
            "L" -> Unit.LITERS
            "ml" -> Unit.MILLILITERS
            "uds" -> Unit.UNITS
            "cdas" -> Unit.TABLESPOONS
            "tazas" -> Unit.CUPS
            "paquetes" -> Unit.PACKAGES
            "docena" -> Unit.DOZEN
            else -> Unit.UNITS
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}