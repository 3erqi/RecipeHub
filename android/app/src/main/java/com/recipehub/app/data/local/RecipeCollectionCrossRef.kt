package com.recipehub.app.data.local

import androidx.room.Entity

@Entity(tableName = "recipe_collection_cross_ref", primaryKeys = ["recipeLocalId", "collectionId"])
data class RecipeCollectionCrossRef(
    val recipeLocalId: Long,
    val collectionId: Long,
)
