package com.pantrychef.back.usecase

import com.pantrychef.back.model.Alert
import com.pantrychef.back.model.enums.AlertType
import com.pantrychef.back.model.enums.Severity
import com.pantrychef.back.repository.AlertRepository
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.utils.UnitsConverter
import kotlinx.coroutines.flow.first

class DecrementIngredientsStockUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val alertRepository: AlertRepository
) {
    suspend operator fun invoke(recipeId: String, servings: Int): Result<Unit> {
        return try {

            val recipe = recipeRepository.getRecipeById(recipeId).getOrThrow()


            val servingFactor = servings.toFloat() / recipe.servings


            recipe.ingredients.forEach { ingredient ->
                val products = productRepository.getAllProducts().first()
                val product = products.find {
                    it.name.trim().equals(ingredient.productName.trim(), ignoreCase = true)
                }

                if (product != null) {

                    val quantityNeeded = ingredient.quantity * servingFactor


                    val quantityToDecrement = UnitsConverter.convert(
                        quantity = quantityNeeded,
                        from = ingredient.unit,
                        to = product.unit
                    )

                    if (quantityToDecrement != null) {
                        val newQuantity = (product.quantity - quantityToDecrement).coerceAtLeast(0f)
                        val updatedProduct = product.copy(
                            quantity = newQuantity,
                            updatedAt = System.currentTimeMillis()
                        )

                        productRepository.updateProduct(updatedProduct).getOrThrow()


                        if (newQuantity <= product.lowStockThreshold) {
                            createLowStockAlert(product.id, product.name, newQuantity, product.lowStockThreshold)
                        }
                    } else {
                        // No se pudo convertir (unidades incompatibles)
                        // Estrategia: Descontar de todas formas usando la cantidad original
                        // Asumiendo que el usuario sabe lo que hace
                        val newQuantity = (product.quantity - quantityNeeded).coerceAtLeast(0f)
                        val updatedProduct = product.copy(
                            quantity = newQuantity,
                            updatedAt = System.currentTimeMillis()
                        )

                        productRepository.updateProduct(updatedProduct).getOrThrow()

                        if (newQuantity <= product.lowStockThreshold) {
                            createLowStockAlert(product.id, product.name, newQuantity, product.lowStockThreshold)
                        }
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea una alerta de bajo stock
     */
    private suspend fun createLowStockAlert(
        productId: String,
        productName: String,
        currentQuantity: Float,
        threshold: Float
    ) {
        val severity = when {
            currentQuantity == 0f -> Severity.URGENT
            currentQuantity <= threshold * 0.5f -> Severity.RESTOCK
            else -> Severity.LOW
        }

        val message = when (severity) {
            Severity.URGENT -> "$productName is out of stock after cooking"
            Severity.RESTOCK -> "$productName is running very low after cooking"
            Severity.LOW -> "Low stock of $productName after cooking"
        }

        val alert = Alert(
            id = "alert-${productId}-${System.currentTimeMillis()}",
            productId = productId,
            productName = productName,
            alertType = if (currentQuantity == 0f) AlertType.OUT_OF_STOCK else AlertType.LOW_STOCK,
            severity = severity,
            message = message,
            createdAt = System.currentTimeMillis(),
            isResolved = false
        )

        alertRepository.createAlert(alert)
    }
}