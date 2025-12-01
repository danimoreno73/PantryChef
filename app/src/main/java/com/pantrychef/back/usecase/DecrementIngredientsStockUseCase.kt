package com.pantrychef.back.usecase

import com.pantrychef.back.model.Alert
import com.pantrychef.back.model.enums.AlertType
import com.pantrychef.back.model.enums.Severity
import com.pantrychef.back.repository.AlertRepository
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
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
                val quantityToDecrement = ingredient.quantity * servingFactor

                val products = productRepository.getAllProducts().first()
                val product = products.find {
                    it.name.equals(ingredient.productName, ignoreCase = true)
                }

                if (product != null) {
                    val newQuantity = (product.quantity - quantityToDecrement).coerceAtLeast(0f)
                    val updatedProduct = product.copy(
                        quantity = newQuantity,
                        updatedAt = System.currentTimeMillis()
                    )

                    productRepository.updateProduct(updatedProduct).getOrThrow()

                    if (newQuantity <= product.lowStockThreshold) {
                        val severity = when {
                            newQuantity == 0f -> Severity.URGENT
                            newQuantity <= product.lowStockThreshold * 0.5f -> Severity.RESTOCK
                            else -> Severity.LOW
                        }

                        val alert = Alert(
                            id = "alert-${product.id}-${System.currentTimeMillis()}",
                            productId = product.id,
                            productName = product.name,
                            alertType = if (newQuantity == 0f) AlertType.OUT_OF_STOCK else AlertType.LOW_STOCK,
                            severity = severity,
                            message = "Low stock after cooking: ${product.name}",
                            createdAt = System.currentTimeMillis(),
                            isResolved = false
                        )

                        alertRepository.createAlert(alert)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}