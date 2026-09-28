package com.abpvt.newsapp.utils

import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.model.StoryCluster
import java.security.MessageDigest

object StoryClusterer {
    private val stopWords = setOf(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "in",
        "is", "it", "of", "on", "or", "that", "the", "this", "to", "was", "will", "with"
    )

    fun cluster(articles: List<Article>, similarityThreshold: Double = 0.38): List<StoryCluster> {
        val groups = mutableListOf<MutableList<Article>>()
        articles.distinctBy { it.url }.forEach { candidate ->
            val target = groups.firstOrNull { group ->
                similarity(candidate.title, group.first().title) >= similarityThreshold
            }
            if (target == null) groups += mutableListOf(candidate) else target += candidate
        }
        return groups.map { group ->
            val primary = group.maxByOrNull { qualityScore(it) } ?: group.first()
            StoryCluster(
                id = stableId(primary.title),
                primary = primary,
                related = group.filterNot { it.url == primary.url }
            )
        }
    }

    fun similarity(first: String, second: String): Double {
        val a = tokens(first)
        val b = tokens(second)
        if (a.isEmpty() || b.isEmpty()) return 0.0
        return a.intersect(b).size.toDouble() / a.union(b).size.toDouble()
    }

    private fun tokens(title: String): Set<String> = title
        .lowercase()
        .replace(Regex("[^a-z0-9 ]"), " ")
        .split(Regex("\\s+"))
        .filter { it.length > 2 && it !in stopWords }
        .toSet()

    private fun qualityScore(article: Article): Int =
        (if (!article.urlToImage.isNullOrBlank()) 3 else 0) +
            (if (!article.content.isNullOrBlank()) 3 else 0) +
            (if (!article.description.isNullOrBlank()) 2 else 0) +
            article.title.length.coerceAtMost(100) / 25

    private fun stableId(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.lowercase().toByteArray())
        .take(8)
        .joinToString("") { "%02x".format(it) }
}
