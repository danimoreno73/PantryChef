package com.pantrychef.back.di

import com.pantrychef.back.repository.AlertRepository
import com.pantrychef.back.repository.AuthRepository
import com.pantrychef.back.repository.MealLogRepository
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.repository.ShoppingListRepository
import com.pantrychef.back.usecase.AddProductUseCase
import com.pantrychef.back.usecase.AddRecipeUseCase
import com.pantrychef.back.usecase.BuildSuggestedShoppingListUseCase
import com.pantrychef.back.usecase.ComputeLowStockAlertsUseCase
import com.pantrychef.back.usecase.DecrementIngredientsStockUseCase
import com.pantrychef.back.usecase.DeleteProductUseCase
import com.pantrychef.back.usecase.GetAllProductsUseCase
import com.pantrychef.back.usecase.GetAlmostCookableRecipesUseCase
import com.pantrychef.back.usecase.GetCookableRecipesUseCase
import com.pantrychef.back.usecase.GetLowStockProductsUseCase
import com.pantrychef.back.usecase.LogOutUserUseCase
import com.pantrychef.back.usecase.LoginUserUseCase
import com.pantrychef.back.usecase.RegisterMealUseCase
import com.pantrychef.back.usecase.RegisterUserUseCase
import com.pantrychef.back.usecase.UpdateProductQuantityUseCase
import com.pantrychef.back.usecase.UpdateRecipeUseCase
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

    @Provides
    @Singleton
    fun provideAddProductUseCase(
        productRepository: ProductRepository
    ): AddProductUseCase{
        return AddProductUseCase(
            productRepository
        )
    }

    @Provides
    @Singleton
    fun provideAddRecipeUseCase(
        recipeRepository: RecipeRepository
    ): AddRecipeUseCase{
        return AddRecipeUseCase(
            recipeRepository
        )
    }

    @Provides
    @Singleton
    fun provideComputeLowStockAlertsUseCase(
        productRepository: ProductRepository,
        alertRepository: AlertRepository
    ): ComputeLowStockAlertsUseCase{
        return ComputeLowStockAlertsUseCase(
            productRepository,
            alertRepository
        )
    }

    @Provides
    @Singleton
    fun provideDecrementingIngredientsStockUseCase(
        recipeRepository: RecipeRepository,
        productRepository: ProductRepository,
        alertRepository: AlertRepository
    ): DecrementIngredientsStockUseCase{
        return DecrementIngredientsStockUseCase(
            recipeRepository,
            productRepository,
            alertRepository
        )
    }


    @Provides
    @Singleton
    fun provideDeleteProductUseCase(
        productRepository: ProductRepository
    ): DeleteProductUseCase{
        return DeleteProductUseCase(productRepository)
    }


    @Provides
    @Singleton
    fun provideGetAllProductsUseCase(
        productRepository: ProductRepository
    ): GetAllProductsUseCase{
        return GetAllProductsUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterMealUseCase(
        mealLogRepository: MealLogRepository,
        decrementIngredientsStockUseCase: DecrementIngredientsStockUseCase
    ): RegisterMealUseCase{
        return RegisterMealUseCase(mealLogRepository, decrementIngredientsStockUseCase)
    }

    @Provides
    @Singleton
    fun provideUpdateProductQuantityUseCase(
        productRepository: ProductRepository
    ): UpdateProductQuantityUseCase{
        return UpdateProductQuantityUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateRecipeUseCase(
        recipeRepository: RecipeRepository
    ): UpdateRecipeUseCase{
        return UpdateRecipeUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideLoginUserUseCase(
        authRepository: AuthRepository
    ): LoginUserUseCase{
        return LoginUserUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideLogOutUserUseCase(
        authRepository: AuthRepository
    ): LogOutUserUseCase{
        return LogOutUserUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterUserUseCase(
        authRepository: AuthRepository
    ): RegisterUserUseCase{
        return RegisterUserUseCase(authRepository)
    }
}