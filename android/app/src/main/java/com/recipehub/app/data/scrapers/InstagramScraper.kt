package com.recipehub.app.data.scrapers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject

// Instagram formats og:description as: N likes, M comments - user on <date>: "<caption>"
private val CAPTION_REGEX = Regex(""":\s*"(.*)"\s*$""", RegexOption.DOT_MATCHES_ALL)

internal fun extractCaption(ogDescription: String?): String? {
    if (ogDescription.isNullOrBlank()) return null
    val match = CAPTION_REGEX.find(ogDescription)
    return match?.groupValues?.get(1) ?: ogDescription
}

class InstagramScraper @Inject constructor(private val client: OkHttpClient) {

    suspend fun fetch(url: String): PostMetadata = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            )
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        client.newCall(request).execute().use { response ->
            val canonicalUrl = response.request.url.toString()
            val empty = PostMetadata(canonicalUrl, null, null, null)

            if (!response.isSuccessful) return@withContext empty
            val html = response.body?.string() ?: return@withContext empty
            val doc = Jsoup.parse(html)

            fun meta(property: String): String? =
                doc.select("meta[property=$property]").firstOrNull()?.attr("content")

            val ogDescription = meta("og:description")
            val ogTitle = meta("og:title")
            val ogImage = meta("og:image")

            if (ogDescription.isNullOrBlank() && ogTitle.isNullOrBlank()) {
                // Login-wall / blocked page: no usable meta tags.
                return@withContext empty
            }

            val authorUsername = ogTitle?.substringBefore(" on Instagram")?.takeIf { it.isNotBlank() }

            PostMetadata(
                canonicalUrl = canonicalUrl,
                captionText = extractCaption(ogDescription),
                thumbnailUrl = ogImage,
                authorUsername = authorUsername,
            )
        }
    }
}
