package com.pantrychef.back.usecase

import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetAlmostCookableRecipesUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository
) {
    data class AlmostCookableRecipe(
        val recipe: Recipe,
        val missingIngredients: List<String>,
        val availableRatio: Float
    )

    suspend operator fun invoke(): Flow<List<AlmostCookableRecipe>> = combine(
        recipeRepository.getAllRecipes(),
        productRepository.getAllProducts()
    ) { recipes, products ->
        recipes.mapNotNull { recipe ->
            // Encontrar ingredientes que faltan
            val missing = recipe.ingredients.filter { ingredient ->
                products.none { product ->
                    // 1. Coincidencia de nombre
                    val nameMatch = product.name.equals(ingredient.productName, ignoreCase = true)

                    // 2. TODO: AQUÍ NECESITAS UN CONVERSOR DE UNIDADES REAL
                    // Por ahora, asumimos que si las unidades son distintas, hacemos una conversión simple manual
                    // para que tus Mocks funcionen (KG vs GRAMS)

                    val productQtyNormalized = when {
                        product.unit.name == "KILOGRAMS" && ingredient.unit.name == "GRAMS" -> product.quantity * 1000
                        product.unit.name == "GRAMS" && ingredient.unit.name == "KILOGRAMS" -> product.quantity / 1000
                        else -> product.quantity // Asumimos misma unidad
                    }

                    nameMatch && (productQtyNormalized >= ingredient.quantity)
                }
            }

            val totalIngredients = recipe.ingredients.size
            // Evitar división por cero
            if (totalIngredients == 0) return@mapNotNull null

            val availableIngredients = totalIngredients - missing.size
            val ratio = availableIngredients.toFloat() / totalIngredients

            // CORRECCIÓN 1: Permitimos ratio >= 0.7 (para aceptar 3 de 4 ingredientes)
            // CORRECCIÓN 2: Quitamos "missing.isNotEmpty()" para incluir las recetas completas (100%)
            if (ratio >= 0.7f) {
                AlmostCookableRecipe(
                    recipe = recipe,
                    missingIngredients = missing.map { it.productName },
                    availableRatio = ratio
                )
            } else {
                null
            }
        }.sortedByDescending { it.availableRatio }
    }
}