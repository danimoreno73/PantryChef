package com.pantrychef.back.usecase

import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.utils.UnitsConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class GetAlmostCookableRecipesUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository
) {
    data class AlmostCookableRecipe(
        val recipe: Recipe,
        val missingIngredients: List<String>,
        val availableRatio: Float
    )

    operator fun invoke(): Flow<List<AlmostCookableRecipe>> = flow {
        combine(
            recipeRepository.getAllRecipes(),
            productRepository.getAllProducts()
        ) { recipes, products ->
            recipes.mapNotNull { recipe ->
                // Identificar ingredientes faltantes o insuficientes
                val missing = recipe.ingredients.filter { ingredient ->
                    products.none { product ->
                        product.name.trim().equals(ingredient.productName.trim(), ignoreCase = true) &&
                                UnitsConverter.hasSufficientQuantity(
                                    productQuantity = product.quantity,
                                    productUnit = product.unit,
                                    requiredQuantity = ingredient.quantity,
                                    requiredUnit = ingredient.unit
                                )
                    }
                }

                val totalIngredients = recipe.ingredients.size
                val availableIngredients = totalIngredients - missing.size
                val ratio = availableIngredients.toFloat() / totalIngredients


                if (ratio in 0.7f..<1.0f) {
                    AlmostCookableRecipe(
                        recipe = recipe,
                        missingIngredients = missing.map { it.productName },
                        availableRatio = ratio
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.availableRatio }
        }.collect { emit(it) }
    }
}