package com.pantrychef.back.usecase

import com.pantrychef.back.repository.AlertRepository
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository

class DecrementIngredientsStockUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val alertRepository: AlertRepository
) {

}
