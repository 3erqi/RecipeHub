package com.recipehub.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity

/** Pure share-intent forwarder: no UI, extracts the URL and hands off to [MainActivity]. */
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)
        val url = extractUrl(sharedText)

        if (url != null) {
            val launchIntent = Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_SHARED_URL, url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Couldn't find a link in what you shared", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun extractUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val matcher = Patterns.WEB_URL.matcher(text)
        return if (matcher.find()) matcher.group() else null
    }

    companion object {
        const val EXTRA_SHARED_URL = "shared_url"
    }
}
