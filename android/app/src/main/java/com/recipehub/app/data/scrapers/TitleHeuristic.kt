package com.recipehub.app.data.scrapers

// Broad-enough emoji/symbol ranges to strip decorative characters from captions without a
// full Unicode grapheme library.
private val EMOJI_REGEX = Regex(
    "[\\u2190-\\u2BFF\\u2600-\\u27BF\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\uFE0F]+",
)
private val HASHTAG_REGEX = Regex("#\\S+")
private val WHITESPACE_REGEX = Regex("\\s+")

private const val MAX_TITLE_LENGTH = 60

object TitleHeuristic {
    fun deriveTitle(caption: String?, fallback: String): String {
        val cleaned = caption
            ?.replace(HASHTAG_REGEX, "")
            ?.replace(EMOJI_REGEX, "")
            ?.replace(WHITESPACE_REGEX, " ")
            ?.trim()

        if (cleaned.isNullOrBlank()) return fallback

        return if (cleaned.length > MAX_TITLE_LENGTH) {
            cleaned.take(MAX_TITLE_LENGTH).trimEnd() + "…"
        } else {
            cleaned
        }
    }
}
