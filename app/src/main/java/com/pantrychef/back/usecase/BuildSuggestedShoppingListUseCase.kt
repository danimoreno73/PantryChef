package com.pantrychef.back.usecase

import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.model.enums.Source
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.repository.ShoppingListRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class BuildSuggestedShoppingListUseCase(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val getAlmostCookableRecipesUseCase: GetAlmostCookableRecipesUseCase
) {
    suspend operator fun invoke(): Result<List<ShoppingItem>> {
        return try {
            val products = productRepository.getAllProducts().first()
            val almostCookable = getAlmostCookableRecipesUseCase().first()

            val itemsMap = mutableMapOf<String, ShoppingItem>()

            products.filter { it.quantity <= it.lowStockThreshold }.forEach { product ->
                val suggestedQuantity = product.lowStockThreshold * 2 - product.quantity
                itemsMap[product.name] = ShoppingItem(
                    id = "shop-${UUID.randomUUID()}",
                    productName = product.name,
                    quantity = suggestedQuantity,
                    unit = product.unit,
                    source = Source.LOW_STOCK,
                    linkedRecipeId = null,
                    isPurchased = false,
                    priority = 5
                )
            }

            almostCookable.forEach { almostCookableRecipe ->
                almostCookableRecipe.missingIngredients.forEach { ingredientName ->
                    val ingredient = almostCookableRecipe.recipe.ingredients
                        .find { it.productName == ingredientName }

                    if (ingredient != null) {
                        itemsMap[ingredientName] = ShoppingItem(
                            id = "shop-${UUID.randomUUID()}",
                            productName = ingredientName,
                            quantity = ingredient.quantity,
                            unit = ingredient.unit,
                            source = Source.RECIPE,
                            linkedRecipeId = almostCookableRecipe.recipe.id,
                            isPurchased = false,
                            priority = (almostCookableRecipe.availableRatio * 10).toInt()
                        )
                    }
                }
            }

            val items = itemsMap.values.toList()
            items.forEach { item ->
                shoppingListRepository.addItem(item)
            }

            Result.success(items.sortedByDescending { it.priority })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}