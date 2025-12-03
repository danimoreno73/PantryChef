package com.pantrychef.back.di

import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.repository.ShoppingListRepository
import com.pantrychef.back.usecase.BuildSuggestedShoppingListUseCase
import com.pantrychef.back.usecase.GetAlmostCookableRecipesUseCase
import com.pantrychef.back.usecase.GetCookableRecipesUseCase
import com.pantrychef.back.usecase.GetLowStockProductsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetLowStockProductsUseCase(
        productRepository: ProductRepository
    ): GetLowStockProductsUseCase {
        return GetLowStockProductsUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideGetCookableRecipesUseCase(
        recipeRepository: RecipeRepository,
        productRepository: ProductRepository
    ): GetCookableRecipesUseCase {
        return GetCookableRecipesUseCase(recipeRepository, productRepository)
    }

    @Provides
    @Singleton
    fun provideGetAlmostCookableRecipesUseCase(
        recipeRepository: RecipeRepository,
        productRepository: ProductRepository
    ): GetAlmostCookableRecipesUseCase {
        return GetAlmostCookableRecipesUseCase(recipeRepository, productRepository)
    }

    @Provides
    @Singleton
    fun provideBuildSuggestedShoppingListUseCase(
        productRepository: ProductRepository,
        recipeRepository: RecipeRepository,
        shoppingListRepository: ShoppingListRepository,
        getAlmostCookableRecipesUseCase: GetAlmostCookableRecipesUseCase
    ): BuildSuggestedShoppingListUseCase {
        return BuildSuggestedShoppingListUseCase(
            productRepository,
            recipeRepository,
            shoppingListRepository,
            getAlmostCookableRecipesUseCase
        )
    }
}