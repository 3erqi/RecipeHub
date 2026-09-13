package com.recipehub.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Insert
    suspend fun insert(entity: CollectionEntity): Long

    @Update
    suspend fun update(entity: CollectionEntity)

    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getById(id: Long): CollectionEntity?

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionRow(id: Long)

    @Query("DELETE FROM recipe_collection_cross_ref WHERE collectionId = :collectionId")
    suspend fun deleteCrossRefsForCollection(collectionId: Long)

    @Transaction
    suspend fun delete(id: Long) {
        deleteCrossRefsForCollection(id)
        deleteCollectionRow(id)
    }

    @Query("SELECT collectionId FROM recipe_collection_cross_ref WHERE recipeLocalId = :recipeLocalId")
    fun observeCollectionIdsForRecipe(recipeLocalId: Long): Flow<List<Long>>

    @Insert
    suspend fun insertCrossRefs(crossRefs: List<RecipeCollectionCrossRef>)

    @Query("DELETE FROM recipe_collection_cross_ref WHERE recipeLocalId = :recipeLocalId")
    suspend fun deleteCrossRefsForRecipe(recipeLocalId: Long)

    @Transaction
    suspend fun setRecipeCollections(recipeLocalId: Long, collectionIds: Set<Long>) {
        deleteCrossRefsForRecipe(recipeLocalId)
        if (collectionIds.isNotEmpty()) {
            insertCrossRefs(collectionIds.map { RecipeCollectionCrossRef(recipeLocalId, it) })
        }
    }
}
