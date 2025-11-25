package com.pantrychef.back.usecase

import com.pantrychef.back.data.local.entity.RecipeWithIngredients
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
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
    operator fun invoke(): Flow<List<AlmostCookableRecipe>> = flow{
        combine(
            recipeRepository.getAllRecipes(),
            productRepository.getAllProducts()
        ){ recipes, products ->
            recipes.mapNotNull { recipe ->
                val missing = recipe.ingredients.filter { ingredient ->
                    products.none { product ->
                        product.name.equals(ingredient.productName, ignoreCase = true) &&
                                product.quantity >= ingredient.quantity
                    }
                }
                val totalIngredients = recipe.ingredients.size
                val availableIngredients = totalIngredients - missing.size
                val ratio = availableIngredients.toFloat() / totalIngredients

                if (missing.isNotEmpty() && ratio >= 0.8f){
                    AlmostCookableRecipe(
                        recipe = recipe,
                        missingIngredients = missing.map { it.productName },
                        availableRatio = ratio
                    )
                }else{
                    null
                }
            }.sortedByDescending { it.availableRatio }
        }.collect { emit(it) }
    }
}