package com.pantrychef.back.di

import com.pantrychef.back.data.local.dao.AlertDao
import com.pantrychef.back.data.local.dao.MealLogDao
import com.pantrychef.back.data.local.dao.ProductDao
import com.pantrychef.back.data.local.dao.RecipeDao
import com.pantrychef.back.data.local.dao.ShoppingItemDao
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Provides

import com.pantrychef.back.data.mock.MockAuthDataSource
import com.pantrychef.back.data.mock.MockProductDataSource
import com.pantrychef.back.data.mock.MockRecipeDataSource
import com.pantrychef.back.repository.AlertRepository
import com.pantrychef.back.repository.AuthRepository
import com.pantrychef.back.repository.MealLogRepository
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import com.pantrychef.back.repository.ShoppingListRepository
import com.pantrychef.back.data.repository.AuthRepositoryImpl
import com.pantrychef.back.data.repository.AlertRepositoryImpl
import com.pantrychef.back.data.repository.MealLogRepositoryImpl
import com.pantrychef.back.data.repository.RecipeRepositoryImpl
import com.pantrychef.back.data.repository.ProductRepositoryImpl
import com.pantrychef.back.data.repository.ShoppingListRepositoryImpl


import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        mockAuthDataSource: MockAuthDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(mockAuthDataSource)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        productDao: ProductDao,
        mockProductDataSource: MockProductDataSource
    ): ProductRepository {
        return ProductRepositoryImpl(productDao, mockProductDataSource)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipeDao: RecipeDao,
        mockRecipeDataSource: MockRecipeDataSource
    ): RecipeRepository {
        return RecipeRepositoryImpl(recipeDao, mockRecipeDataSource)
    }

    @Provides
    @Singleton
    fun provideMealLogRepository(
        mealLogDao: MealLogDao
    ): MealLogRepository {
        return MealLogRepositoryImpl(mealLogDao)
    }

    @Provides
    @Singleton
    fun provideShoppingListRepository(
        shoppingItemDao: ShoppingItemDao
    ): ShoppingListRepository {
        return ShoppingListRepositoryImpl(shoppingItemDao)
    }

    @Provides
    @Singleton
    fun provideAlertRepository(
        alertDao: AlertDao
    ): AlertRepository {
        return AlertRepositoryImpl(alertDao)
    }
}