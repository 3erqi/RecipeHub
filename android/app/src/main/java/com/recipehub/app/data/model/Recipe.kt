package com.recipehub.app.data.model

import com.recipehub.app.data.local.RecipeEntity

data class Recipe(
    val localId: Long,
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val thumbnailPath: String?,
    val sourceUrl: String,
    val sourcePlatform: String,
    val authorUsername: String?,
    val status: String,
)

fun RecipeEntity.toDomain(): Recipe = Recipe(
    localId = localId,
    title = title,
    ingredients = ingredients,
    steps = steps,
    thumbnailPath = thumbnailPath,
    sourceUrl = sourceUrl,
    sourcePlatform = sourcePlatform,
    authorUsername = authorUsername,
    status = status,
)
