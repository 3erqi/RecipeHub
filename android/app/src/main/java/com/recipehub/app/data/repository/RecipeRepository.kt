package com.recipehub.app.data.repository

import android.util.Log
import com.recipehub.app.data.local.RecipeDao
import com.recipehub.app.data.local.RecipeEntity
import com.recipehub.app.data.local.RecipeStatus
import com.recipehub.app.data.local.ThumbnailStore
import com.recipehub.app.data.model.Recipe
import com.recipehub.app.data.model.toDomain
import com.recipehub.app.data.scrapers.InstagramScraper
import com.recipehub.app.data.scrapers.PlatformDetector
import com.recipehub.app.data.scrapers.TikTokScraper
import com.recipehub.app.data.scrapers.TitleHeuristic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RecipeRepository"

sealed interface SubmitOutcome {
    data class Success(val localId: Long) : SubmitOutcome
    data class NeedsName(val localId: Long) : SubmitOutcome
    data class Failure(val localId: Long) : SubmitOutcome
}

@Singleton
class RecipeRepository @Inject constructor(
    private val dao: RecipeDao,
    private val tikTokScraper: TikTokScraper,
    private val instagramScraper: InstagramScraper,
    private val thumbnailStore: ThumbnailStore,
) {
    /** Empty [collectionIds] means unfiltered (All Recipes); otherwise a union across the given lists. */
    fun observeRecipes(collectionIds: Set<Long> = emptySet()): Flow<List<Recipe>> {
        val source = if (collectionIds.isEmpty()) dao.observeAll() else dao.observeByCollections(collectionIds.toList())
        return source.map { list -> list.map { it.toDomain() } }
    }

    fun observeRecipe(localId: Long): Flow<Recipe?> =
        dao.observeById(localId).map { it?.toDomain() }

    /**
     * Inserts a pending placeholder, fetches the post's metadata directly from TikTok/Instagram,
     * and caches its thumbnail locally. If there's a caption or author to derive a title from,
     * the recipe is saved immediately (fast path, e.g. TikTok). If the platform blocked the
     * fetch and gave us nothing to name it with (e.g. Instagram's login-wall), the row is left
     * as NEEDS_NAME and the caller must supply a title via [confirmName] before it's finalized —
     * it will not appear in the list until then.
     */
    suspend fun submitUrl(url: String): SubmitOutcome {
        val localId = dao.insert(
            RecipeEntity(
                title = url,
                ingredients = emptyList(),
                steps = emptyList(),
                sourceUrl = url,
                sourcePlatform = PlatformDetector.platformFromUrl(url) ?: "unknown",
                status = RecipeStatus.PENDING,
                createdAt = nowIso(),
                updatedAt = nowIso(),
            )
        )
        return try {
            val placeholder = dao.getById(localId) ?: error("Recipe not found")
            val platform = PlatformDetector.platformFromUrl(url)
                ?: error("Unrecognized link: not a TikTok or Instagram URL")
            val metadata = when (platform) {
                "tiktok" -> tikTokScraper.fetch(url)
                else -> instagramScraper.fetch(url)
            }
            val thumbnailPath = metadata.thumbnailUrl?.let { thumbnailStore.download(it) }
            val hasNameableText = !metadata.captionText.isNullOrBlank() || !metadata.authorUsername.isNullOrBlank()

            val updated = placeholder.copy(
                thumbnailPath = thumbnailPath,
                sourceUrl = metadata.canonicalUrl,
                sourcePlatform = platform,
                authorUsername = metadata.authorUsername,
                captionText = metadata.captionText,
                updatedAt = nowIso(),
            )

            if (hasNameableText) {
                val title = TitleHeuristic.deriveTitle(metadata.captionText, fallback = metadata.authorUsername ?: url)
                dao.update(updated.copy(title = title, status = RecipeStatus.SAVED))
                SubmitOutcome.Success(localId)
            } else {
                dao.update(updated.copy(status = RecipeStatus.NEEDS_NAME))
                SubmitOutcome.NeedsName(localId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch metadata for $url", e)
            dao.getById(localId)?.let { entity ->
                dao.update(entity.copy(status = RecipeStatus.FAILED, updatedAt = nowIso()))
            }
            SubmitOutcome.Failure(localId)
        }
    }

    suspend fun retry(localId: Long): SubmitOutcome {
        val entity = dao.getById(localId) ?: return SubmitOutcome.Failure(localId)
        dao.delete(localId)
        return submitUrl(entity.sourceUrl)
    }

    /** Discards a draft row (PENDING/NEEDS_NAME/FAILED) the user chose not to keep. */
    suspend fun discard(localId: Long) {
        dao.delete(localId)
    }

    /** Finalizes a NEEDS_NAME (or failed) row with a user-supplied title. */
    suspend fun confirmName(localId: Long, title: String) {
        dao.getById(localId)?.let { entity ->
            dao.update(entity.copy(title = title, status = RecipeStatus.SAVED, updatedAt = nowIso()))
        }
    }

    /** Permanently removes a saved recipe, its list memberships, and its cached thumbnail file. */
    suspend fun deleteRecipe(localId: Long) {
        dao.getById(localId)?.thumbnailPath?.let { path -> File(path).delete() }
        dao.deleteRecipeAndCrossRefs(localId)
    }

    suspend fun saveEdits(localId: Long, title: String, ingredients: List<String>, steps: List<String>) {
        dao.getById(localId)?.let { entity ->
            dao.update(
                entity.copy(
                    title = title,
                    ingredients = ingredients,
                    steps = steps,
                    status = RecipeStatus.SAVED,
                    updatedAt = nowIso(),
                )
            )
        }
    }

    private fun nowIso(): String = Instant.now().toString()
}
