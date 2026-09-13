package com.recipehub.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Downloads and caches thumbnail images to local app storage. TikTok/Instagram thumbnail URLs
 * are signed and expire (e.g. TikTok's carry an `x-expires` query param), so for a bookmarking
 * tool the bytes need to be saved locally rather than just referencing the remote URL.
 */
class ThumbnailStore @Inject constructor(
    private val client: OkHttpClient,
    @ApplicationContext private val context: Context,
) {
    suspend fun download(url: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bytes = response.body?.bytes() ?: return@withContext null

                val dir = File(context.filesDir, "thumbnails").apply { mkdirs() }
                val file = File(dir, "${UUID.randomUUID()}.jpg")
                file.writeBytes(bytes)
                file.absolutePath
            }
        }.getOrNull()
    }
}
