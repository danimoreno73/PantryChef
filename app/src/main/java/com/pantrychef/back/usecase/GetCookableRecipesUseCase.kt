package com.pantrychef.back.usecase

import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetCookableRecipesUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(): Flow<List<Recipe>> = combine(
        recipeRepository.getAllRecipes(),
        productRepository.getAllProducts()
    ) { recipes, products ->
        recipes.filter { recipe ->
            recipe.ingredients.all { ingredient ->
                products.any { product ->
                    val nameMatch = product.name.equals(ingredient.productName, ignoreCase = true)

                    // Normalización simple de unidades (KG a G) para evitar errores
                    val productQtyNormalized = when (product.unit.name) {
                        "KILOGRAMS" if ingredient.unit.name == "GRAMS" -> product.quantity * 1000
                        "GRAMS" if ingredient.unit.name == "KILOGRAMS" -> product.quantity / 1000
                        else -> product.quantity
                    }

                    nameMatch && productQtyNormalized >= ingredient.quantity
                }
            }
        }
    }
}