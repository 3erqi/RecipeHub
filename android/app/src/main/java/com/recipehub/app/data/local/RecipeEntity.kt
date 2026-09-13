package com.recipehub.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object RecipeStatus {
    const val PENDING = "PENDING"
    const val NEEDS_NAME = "NEEDS_NAME"
    const val SAVED = "SAVED"
    const val FAILED = "FAILED"
}

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val thumbnailPath: String? = null,
    val sourceUrl: String,
    val sourcePlatform: String,
    val authorUsername: String? = null,
    val captionText: String? = null,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)
