package com.abpvt.newsapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class RssFeedSourceTest {
    @Test fun parsesPublisherFeedWithoutFetchingArticleBody() {
        val xml = """<?xml version="1.0"?><rss xmlns:media="http://search.yahoo.com/mrss/"><channel>
            <item><title>Story title</title><link>https://example.com/story</link>
            <description><![CDATA[<p>Story summary</p>]]></description>
            <pubDate>Fri, 02 Oct 2026 08:47:47 GMT</pubDate>
            <media:thumbnail url="https://example.com/photo.jpg" /></item>
            </channel></rss>"""
        val article = RssFeedSource.parse(xml, "Example News").single()
        assertEquals("Story title", article.title)
        assertEquals("https://example.com/story", article.url)
        assertEquals("Example News", article.sectionName)
        assertEquals("https://example.com/photo.jpg", article.urlToImage)
        assertEquals("2026-10-02T08:47:47Z", article.publishedAt)
    }

    @Test fun separatesTrendingHashtagsIntoSearchTerms() {
        assertEquals("Artificial Intelligence", RssFeedSource.normalizedSearch("#ArtificialIntelligence"))
        assertEquals("Space X", RssFeedSource.normalizedSearch("#SpaceX"))
    }

    @Test fun rejectsDoctypeFeeds() {
        assertEquals(emptyList<Any>(), RssFeedSource.parse("<!DOCTYPE rss><rss/>", "Example"))
    }
}
