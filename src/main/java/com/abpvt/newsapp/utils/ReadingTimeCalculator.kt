package com.abpvt.newsapp.utils

import kotlin.math.max

object ReadingTimeCalculator {
    private const val WORDS_PER_MINUTE = 200

    /**
     * Calculate estimated reading time in minutes based on title, description, and content.
     */
    fun calculateMinutes(title: String, description: String?, content: String?): Int {
        val fullText = buildString {
            append(title)
            append(" ")
            if (!description.isNullOrBlank()) append(description).append(" ")
            if (!content.isNullOrBlank()) append(content)
        }

        val wordCount = fullText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val minutes = (wordCount / WORDS_PER_MINUTE)
        return max(1, minutes.coerceAtLeast(if (wordCount > 50) 2 else 1))
    }
}
