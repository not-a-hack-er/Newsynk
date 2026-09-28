package com.abpvt.newsapp.utils

import com.abpvt.newsapp.data.model.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryClustererTest {
    @Test
    fun `groups headlines describing the same event`() {
        val articles = listOf(
            article("1", "Central bank cuts interest rates after inflation falls", "Source A"),
            article("2", "Central bank cuts rates as inflation falls", "Source B"),
            article("3", "Local football club wins cup final", "Source C")
        )

        val clusters = StoryClusterer.cluster(articles)

        assertEquals(2, clusters.size)
        assertTrue(clusters.any { it.allArticles.size == 2 && it.sourceCount == 2 })
    }

    @Test
    fun `does not duplicate identical urls`() {
        val story = article("1", "New satellite launches successfully", "Source A")

        val clusters = StoryClusterer.cluster(listOf(story, story.copy(title = "Updated title")))

        assertEquals(1, clusters.sumOf { it.allArticles.size })
    }

    @Test
    fun `similarity ignores punctuation and common words`() {
        val score = StoryClusterer.similarity(
            "The markets rally after the rate decision",
            "Markets rally after rate decision!"
        )

        assertTrue(score > 0.7)
    }

    private fun article(id: String, title: String, source: String) = Article(
        id = id,
        title = title,
        url = "https://example.com/$id",
        sectionName = source
    )
}
