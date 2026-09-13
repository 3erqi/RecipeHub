package com.recipehub.app.data.repository

import com.recipehub.app.data.local.CollectionDao
import com.recipehub.app.data.local.CollectionEntity
import com.recipehub.app.data.model.Collection
import com.recipehub.app.data.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val dao: CollectionDao,
) {
    fun observeCollections(): Flow<List<Collection>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeCollectionIdsForRecipe(recipeLocalId: Long): Flow<Set<Long>> =
        dao.observeCollectionIdsForRecipe(recipeLocalId).map { it.toSet() }

    suspend fun createCollection(name: String): Long =
        dao.insert(CollectionEntity(name = name, createdAt = Instant.now().toString()))

    suspend fun renameCollection(id: Long, name: String) {
        dao.getById(id)?.let { dao.update(it.copy(name = name)) }
    }

    suspend fun deleteCollection(id: Long) {
        dao.delete(id)
    }

    suspend fun setRecipeCollections(recipeLocalId: Long, collectionIds: Set<Long>) {
        dao.setRecipeCollections(recipeLocalId, collectionIds)
    }
}
