package com.recipehub.app.data.scrapers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@Serializable
private data class TikTokOEmbedResponse(
    val title: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)

class TikTokScraper @Inject constructor(private val client: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(url: String): PostMetadata = withContext(Dispatchers.IO) {
        val canonicalUrl = resolveCanonicalUrl(url)
        val empty = PostMetadata(canonicalUrl, null, null, null)

        val oembedUrl = "https://www.tiktok.com/oembed".toHttpUrl().newBuilder()
            .addQueryParameter("url", canonicalUrl)
            .build()
        val request = Request.Builder().url(oembedUrl).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext empty
            val body = response.body?.string() ?: return@withContext empty
            val parsed = runCatching { json.decodeFromString<TikTokOEmbedResponse>(body) }
                .getOrNull() ?: return@withContext empty
            PostMetadata(
                canonicalUrl = canonicalUrl,
                captionText = parsed.title,
                thumbnailUrl = parsed.thumbnailUrl,
                authorUsername = parsed.authorName,
            )
        }
    }

    private fun resolveCanonicalUrl(url: String): String {
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute().use { it.request.url.toString() }
    }
}
