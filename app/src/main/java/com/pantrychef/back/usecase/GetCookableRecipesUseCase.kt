package com.pantrychef.back.usecase

import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
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
        ){ recipes, products ->
            recipes.filter { recipe ->
                recipe.ingredients.all { ingredient ->
                    products.any{ product ->
                        product.name.equals(ingredient.productName, ignoreCase = true) &&
                                product.quantity >= ingredient.quantity
                    }
                }
            }
        }.collect { emit(it) }
    }
}
