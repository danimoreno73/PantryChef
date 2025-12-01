package com.pantrychef.back.usecase


import com.pantrychef.back.model.MealLog

import com.pantrychef.back.model.enums.MealType
import com.pantrychef.back.repository.MealLogRepository
import java.util.UUID

class RegisterMealUseCase(
    private val mealLogRepository: MealLogRepository,
    private val decrementIngredientsStockUseCase: DecrementIngredientsStockUseCase
) {
    suspend operator fun invoke(
        recipeId: String,
        recipeName: String,
        mealType: MealType,
        servings: Int,
        caloriesEstimate: Int? = null
    ): Result<MealLog> {
        return try {
            val mealLog = MealLog(
                id = "meal-${UUID.randomUUID()}",
                recipeId = recipeId,
                recipeName = recipeName,
                timestamp = System.currentTimeMillis(),
                mealType = mealType,
                servings = servings,
                caloriesEstimate = caloriesEstimate
            )

            mealLogRepository.logMeal(mealLog).getOrThrow()

            decrementIngredientsStockUseCase(recipeId, servings).getOrThrow()

            Result.success(mealLog)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}