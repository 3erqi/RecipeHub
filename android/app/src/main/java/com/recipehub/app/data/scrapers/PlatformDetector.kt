package com.recipehub.app.data.scrapers

import android.net.Uri

object PlatformDetector {
    fun platformFromUrl(url: String): String? {
        val host = Uri.parse(url).host?.lowercase() ?: return null
        return when {
            host.contains("tiktok.com") -> "tiktok"
            host.contains("instagram.com") -> "instagram"
            else -> null
        }
    }
}
