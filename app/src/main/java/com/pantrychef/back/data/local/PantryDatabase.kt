package com.pantrychef.back.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pantrychef.back.data.local.dao.AlertDao
import com.pantrychef.back.data.local.dao.MealLogDao
import com.pantrychef.back.data.local.dao.ProductDao
import com.pantrychef.back.data.local.dao.RecipeDao
import com.pantrychef.back.data.local.dao.ShoppingItemDao
import com.pantrychef.back.data.local.entity.AlertEntity
import com.pantrychef.back.data.local.entity.IngredientEntity
import com.pantrychef.back.data.local.entity.MealLogEntity
import com.pantrychef.back.data.local.entity.ProductEntity
import com.pantrychef.back.data.local.entity.RecipeEntity
import com.pantrychef.back.data.local.entity.ShoppingItemEntity

@Database(
    entities = [
        ProductEntity::class,
        RecipeEntity::class,
        IngredientEntity::class,
        MealLogEntity::class,
        ShoppingItemEntity::class,
        AlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PantryDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun alertDao(): AlertDao
}