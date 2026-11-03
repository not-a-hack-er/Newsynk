package com.abpvt.newsapp.data.repository

import com.abpvt.newsapp.data.model.Article
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ArticleFreshnessTest {
    @Test fun excludesOldAndUndatedStories() {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.NOVEMBER, 3, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val items = listOf(
            Article(id = "recent", publishedAt = "2026-11-02T10:00:00Z"),
            Article(id = "old", publishedAt = "2026-07-01T10:00:00Z"),
            Article(id = "undated", publishedAt = "")
        )
        assertEquals(listOf("recent"), ArticleFreshness.recent(items, now).map { it.id })
    }
}
