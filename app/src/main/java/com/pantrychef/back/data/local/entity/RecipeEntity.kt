package com.pantrychef.back.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pantrychef.back.model.enums.Difficulty

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "prep_time_minutes") val prepTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val steps: String, // JSON serializado
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "is_public") val isPublic: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
