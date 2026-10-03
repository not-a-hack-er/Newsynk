package com.abpvt.newsapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abpvt.newsapp.data.repository.RssFeedSource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RssFeedSourceAndroidTest {
    @Test fun parsesRssOnAndroidXmlParser() {
        val xml = """<rss><channel><item><title>Headline</title>
            <link>https://example.com/story</link>
            <description>Summary</description>
            <pubDate>Fri, 02 Oct 2026 08:47:47 GMT</pubDate>
            </item></channel></rss>"""
        val story = RssFeedSource.parse(xml, "Example").single()
        assertEquals("Headline", story.title)
        assertEquals("2026-10-02T08:47:47Z", story.publishedAt)
        assertEquals(emptyList<Any>(), RssFeedSource.parse("<!DOCTYPE rss><rss/>", "Example"))
    }
}
