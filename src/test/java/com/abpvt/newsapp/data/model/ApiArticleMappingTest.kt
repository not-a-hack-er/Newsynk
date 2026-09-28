package com.abpvt.newsapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiArticleMappingTest {

    @Test
    fun `GNews response maps to shared article model`() {
        val source = GNewsArticle.GNewsSource(name = "Example News", url = "https://example.com")
        val article = GNewsArticle(
            title = "Headline",
            description = "Summary",
            content = "Body",
            url = "https://example.com/story",
            image = "https://example.com/image.jpg",
            publishedAt = "2026-09-28T10:00:00Z",
            source = source
        ).toArticle()

        assertEquals("https://example.com/story", article.id)
        assertEquals("Headline", article.title)
        assertEquals("Example News", article.sectionName)
        assertEquals("Example News", article.author)
        assertEquals("Summary", article.fields?.description)
        assertEquals("Body", article.content)
    }

    @Test
    fun `GNews mapping supplies source fallback`() {
        assertEquals("GNews", GNewsArticle(url = "https://example.com").toArticle().source.name)
    }

    @Test
    fun `Currents response maps category and fields`() {
        val article = CurrentsArticle(
            id = "currents-1",
            title = "Headline",
            description = "Summary",
            url = "https://example.com/story",
            author = "Reporter",
            image = "https://example.com/image.jpg",
            published = "2026-09-28 10:00:00 +0000",
            category = listOf("technology")
        ).toArticle()

        assertEquals("currents-1", article.id)
        assertEquals("technology", article.sectionName)
        assertEquals("Reporter", article.author)
        assertEquals("https://example.com/image.jpg", article.urlToImage)
    }

    @Test
    fun `Currents mapping normalizes missing identifiers and images`() {
        val article = CurrentsArticle(
            id = "",
            url = "https://example.com/fallback",
            image = "None"
        ).toArticle()

        assertEquals("https://example.com/fallback", article.id)
        assertEquals("Currents", article.sectionName)
        assertNull(article.urlToImage)
    }
}
