package com.pantrychef.front.pantry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditProductUiState(
    val productId: String? = null,
    val name: String = "",
    val category: String = "Lácteos",
    val quantity: String = "",
    val unit: String = "L",
    val location: String = "Refrigerador",
    val brand: String = "",
    val expiryDate: String? = null,
    val lowStockThreshold: String = "",
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
}

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
    // TODO: Inject ProductRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow(AddEditProductUiState(productId = productId))
    val uiState: StateFlow<AddEditProductUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<AddEditProductNavigation?>(null)
    val navigation: StateFlow<AddEditProductNavigation?> = _navigation.asStateFlow()

    init {
        if (productId != null) {  // Solo carga si hay productId
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

            // TODO: Call repository
            kotlinx.coroutines.delay(300)

            // Mock data - cargar producto existente
            _uiState.update { it.copy(
                name = "Leche entera",
                category = "Lácteos",
                quantity = "2.0",
                unit = "L",
                location = "Refrigerador",
                brand = "Pascual",
                expiryDate = "2025-12-15",
                lowStockThreshold = "1.0",
                isLoading = false
            )}
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

            // TODO: Call repository to save
            kotlinx.coroutines.delay(500)

            _uiState.update { it.copy(isLoading = false) }
            _navigation.value = AddEditProductNavigation.Back
        }
    }

    private fun deleteProduct() {
        if (productId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: Call repository to delete
            kotlinx.coroutines.delay(500)

            _uiState.update { it.copy(isLoading = false) }
            _navigation.value = AddEditProductNavigation.Back
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}