package com.pantrychef.front.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pantrychef.front.components.DropdownSelector
import com.pantrychef.front.components.InputField
import com.pantrychef.front.components.PrimaryButton
import com.pantrychef.front.components.SecondaryButton
import com.pantrychef.front.theme.PantryChefTheme

@Composable
fun AddEditProductScreen(
    productId: String?,
    navController: NavController,
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            AddEditProductNavigation.Back -> {
                // Notificar a la pantalla anterior que el producto fue actualizado
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("product_updated", true)

                navController.popBackStack()
                viewModel.clearNavigation()
            }
            AddEditProductNavigation.ToPantry -> {
                // Eliminar: Salir de Edit + Detail, volver a Pantry
                navController.popBackStack() // Sale de Edit
                navController.popBackStack() // Sale de Detail
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    AddEditProductContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditProductContent(
    uiState: AddEditProductUiState,
    onEvent: (AddEditProductEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = if (uiState.productId != null) "Editar producto" else "Añadir producto"
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            actions = {
                if (uiState.productId != null) {
                    IconButton(onClick = { onEvent(AddEditProductEvent.DeleteClicked) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información básica
            Text(
                text = "Información básica",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            InputField(
                value = uiState.name,
                onValueChange = { onEvent(AddEditProductEvent.NameChanged(it)) },
                label = "Nombre del producto",
                placeholder = "Ej. Leche entera",
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError
            )

            DropdownSelector(
                label = "Categoría",
                value = uiState.category,
                options = listOf(
                    "Lácteos",
                    "Proteínas",
                    "Granos",
                    "Verduras",
                    "Frutas",
                    "Condimentos",
                    "Otros"
                ),
                onValueChange = { onEvent(AddEditProductEvent.CategoryChanged(it)) }
            )

            InputField(
                value = uiState.brand,
                onValueChange = { onEvent(AddEditProductEvent.BrandChanged(it)) },
                label = "Marca (opcional)",
                placeholder = "Ej. Pascual"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cantidad
            Text(
                text = "Cantidad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InputField(
                    value = uiState.quantity,
                    onValueChange = { onEvent(AddEditProductEvent.QuantityChanged(it)) },
                    label = "Cantidad",
                    placeholder = "2.0",
                    keyboardType = KeyboardType.Decimal,
                    isError = uiState.quantityError != null,
                    errorMessage = uiState.quantityError,
                    modifier = Modifier.weight(1f)
                )

                DropdownSelector(
                    label = "Unidad",
                    value = uiState.unit,
                    options = listOf(
                        "L",
                        "kg",
                        "g",
                        "uds",
                        "paquetes",
                        "latas",
                        "botellas"
                    ),
                    onValueChange = { onEvent(AddEditProductEvent.UnitChanged(it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            InputField(
                value = uiState.lowStockThreshold,
                onValueChange = { onEvent(AddEditProductEvent.LowStockThresholdChanged(it)) },
                label = "Límite de stock bajo (opcional)",
                placeholder = "1.0",
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Almacenamiento
            Text(
                text = "Almacenamiento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            DropdownSelector(
                label = "Ubicación",
                value = uiState.location,
                options = listOf(
                    "Refrigerador",
                    "Despensa",
                    "Congelador",
                    "Frutero",
                    "Especiero",
                    "Otro"
                ),
                onValueChange = { onEvent(AddEditProductEvent.LocationChanged(it)) }
            )

            // Fecha de caducidad (por ahora como input text, después podemos hacer DatePicker)
            InputField(
                value = uiState.expiryDate ?: "",
                onValueChange = { onEvent(AddEditProductEvent.ExpiryDateChanged(it.ifBlank { null })) },
                label = "Fecha de caducidad (opcional)",
                placeholder = "YYYY-MM-DD"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones
            PrimaryButton(
                text = if (uiState.productId != null) "Guardar cambios" else "Añadir producto",
                onClick = { onEvent(AddEditProductEvent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            if (uiState.productId == null) {
                SecondaryButton(
                    text = "Cancelar",
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun AddEditProductScreenPreview() {
    PantryChefTheme {
        AddEditProductContent(
            uiState = AddEditProductUiState(),
            onEvent = {},
            onBackClick = {}
        )
    }
}