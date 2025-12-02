package com.pantrychef.back.usecase

import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.utils.UnitsConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class GetCookableRecipesUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository
) {

    operator fun invoke(): Flow<List<Recipe>> = flow {
        combine(
            recipeRepository.getAllRecipes(),
            productRepository.getAllProducts()
        ) { recipes, products ->
            recipes.filter { recipe ->

                recipe.ingredients.all { ingredient ->
                    products.any { product ->

                        product.name.trim().equals(ingredient.productName.trim(), ignoreCase = true) &&
                                // Comparar cantidad con conversión de unidades
                                UnitsConverter.hasSufficientQuantity(
                                    productQuantity = product.quantity,
                                    productUnit = product.unit,
                                    requiredQuantity = ingredient.quantity,
                                    requiredUnit = ingredient.unit
                                )
                    }
                }
            }
        }.collect { emit(it) }
    }
}