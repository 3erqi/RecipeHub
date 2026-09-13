package com.recipehub.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert
    suspend fun insert(entity: RecipeEntity): Long

    @Update
    suspend fun update(entity: RecipeEntity)

    @Query("DELETE FROM recipes WHERE localId = :localId")
    suspend fun delete(localId: Long)

    @Query("DELETE FROM recipe_collection_cross_ref WHERE recipeLocalId = :localId")
    suspend fun deleteCrossRefsForRecipe(localId: Long)

    @Transaction
    suspend fun deleteRecipeAndCrossRefs(localId: Long) {
        deleteCrossRefsForRecipe(localId)
        delete(localId)
    }

    // Only fully-finalized recipes (a title was confirmed, one way or another) show in the list —
    // PENDING/NEEDS_NAME/FAILED rows are drafts still being resolved by the saving flow.
    @Query("SELECT * FROM recipes WHERE status = 'SAVED' ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE localId = :localId")
    fun observeById(localId: Long): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE localId = :localId")
    suspend fun getById(localId: Long): RecipeEntity?

    @Query(
        """
        SELECT DISTINCT r.* FROM recipes r
        INNER JOIN recipe_collection_cross_ref x ON r.localId = x.recipeLocalId
        WHERE x.collectionId IN (:collectionIds) AND r.status = 'SAVED'
        ORDER BY r.createdAt DESC
        """
    )
    fun observeByCollections(collectionIds: List<Long>): Flow<List<RecipeEntity>>
}
