package com.pantrychef.back.usecase

import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.repository.ShoppingListRepository

class BuildSuggestedShoppingListUseCase(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val getAlmostCookableRecipesUseCase: GetAlmostCookableRecipesUseCase

) {
}