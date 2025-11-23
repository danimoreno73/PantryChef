package com.pantrychef.back.di

import android.content.Context
import androidx.room.Room
import com.pantrychef.back.data.local.PantryDatabase
import com.pantrychef.back.data.local.dao.AlertDao
import com.pantrychef.back.data.local.dao.MealLogDao
import com.pantrychef.back.data.local.dao.ProductDao
import com.pantrychef.back.data.local.dao.RecipeDao
import com.pantrychef.back.data.local.dao.ShoppingItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PantryDatabase{
        return Room.databaseBuilder(
            context,
            PantryDatabase::class.java,
            "pantry_database"
        ).build()
    }
    @Provides
    fun provideProductDao(database: PantryDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideRecipeDao(database: PantryDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideMealLogDao(database: PantryDatabase): MealLogDao {
        return database.mealLogDao()
    }

    @Provides
    fun provideShoppingItemDao(database: PantryDatabase): ShoppingItemDao {
        return database.shoppingItemDao()
    }

    @Provides
    fun provideAlertDao(database: PantryDatabase): AlertDao {
        return database.alertDao()
    }
}