package com.abpvt.newsapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.google.gson.Gson

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

    @Test
    fun `NewsData response maps publisher and image`() {
        val json = """{"status":"success","results":[{"article_id":"nd-1","title":"A story","link":"https://example.com/a","source_name":"Example Daily","image_url":"https://example.com/a.jpg","pubDate":"2026-10-03 08:00:00","creator":["A Reporter"]}]}"""
        val article = Gson().fromJson(json, NewsDataResponse::class.java).results.single().toArticle()!!
        assertEquals("Example Daily", article.sectionName)
        assertEquals("https://example.com/a.jpg", article.urlToImage)
        assertEquals("A Reporter", article.author)
        assertEquals("https://example.com/a", article.url)
    }

    @Test
    fun `NewsData mapping drops malformed links`() {
        assertNull(NewsDataArticle(title = "A story", link = "javascript:alert(1)").toArticle())
    }

    @Test
    fun `Guardian section labels show the actual publisher`() {
        val article = Article(url = "https://www.theguardian.com/world/story", sectionName = "World")
        assertEquals("The Guardian", article.source.name)
    }

    @Test
    fun `Generic category labels show the publisher domain`() {
        val article = Article(url = "https://www.example.com/story", sectionName = "technology")
        assertEquals("example.com", article.source.name)
    }
}
