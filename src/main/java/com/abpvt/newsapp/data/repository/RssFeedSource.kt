package com.abpvt.newsapp.data.repository

import com.abpvt.newsapp.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

/** Public publisher RSS is a keyless fallback, not a substitute for full-text news search. */
@Singleton
class RssFeedSource @Inject constructor() {
    private data class Cached(val at: Long, val articles: List<Article>)
    private val memoryCache = ConcurrentHashMap<String, Cached>()
    private val client = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    private val feeds = mapOf(
        "world" to listOf("https://feeds.bbci.co.uk/news/rss.xml", "https://www.theguardian.com/world/rss"),
        "technology" to listOf("https://feeds.bbci.co.uk/news/technology/rss.xml", "https://www.theguardian.com/technology/rss"),
        "business" to listOf("https://feeds.bbci.co.uk/news/business/rss.xml", "https://www.theguardian.com/business/rss"),
        "sports" to listOf("https://feeds.bbci.co.uk/sport/rss.xml", "https://www.theguardian.com/sport/rss"),
        "health" to listOf("https://feeds.bbci.co.uk/news/health/rss.xml", "https://www.theguardian.com/society/health/rss"),
        "entertainment" to listOf("https://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml", "https://www.theguardian.com/culture/rss")
    )

    suspend fun headlines(page: Int): List<Article> =
        if (page > 1) emptyList() else fetch(listOf("world", "technology", "business"))

    suspend fun category(category: String, page: Int): List<Article> =
        if (page > 1) emptyList() else fetch(listOf(canonicalCategory(category)))

    suspend fun search(query: String, page: Int): List<Article> {
        if (page > 1) return emptyList()
        val words = normalizedSearch(query).lowercase(Locale.ROOT).split(Regex("\\s+"))
            .filter { it.length > 2 }
        if (words.isEmpty()) return emptyList()
        return fetch(feeds.keys.toList()).filter { article ->
            val content = "${article.title} ${article.fields?.description.orEmpty()} ${article.sectionName}"
                .lowercase(Locale.ROOT)
            words.all { it in content }
        }
    }

    private suspend fun fetch(categories: List<String>): List<Article> = coroutineScope {
        categories.flatMap { feeds[it].orEmpty() }.distinct().map { url ->
            async { runCatching { load(url) }.getOrDefault(emptyList()) }
        }.awaitAll().flatten().distinctBy { it.url }.sortedByDescending { it.publishedAt }
    }

    private suspend fun load(url: String): List<Article> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        memoryCache[url]?.takeIf { now - it.at < 5 * 60 * 1000L }?.let { return@withContext it.articles }
        val request = Request.Builder().url(url).header("User-Agent", "Newsynk/2.0 (RSS reader)").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            parse(response.body?.string().orEmpty(), if ("bbc.co.uk" in url) "BBC News" else "The Guardian")
                .also { if (it.isNotEmpty()) memoryCache[url] = Cached(now, it) }
        }
    }

    companion object {
        internal fun normalizedSearch(query: String): String = query.removePrefix("#")
            .replace(Regex("(?<=[a-z])(?=[A-Z])"), " ").trim()

        private fun canonicalCategory(category: String): String = when (category.lowercase(Locale.ROOT)) {
            "tech" -> "technology"
            "sport" -> "sports"
            else -> category.lowercase(Locale.ROOT)
        }

        internal fun parse(xml: String, publisher: String): List<Article> {
            if (xml.isBlank()) return emptyList()
            // Android's built-in DOM parser does not support the usual Xerces
            // disallow-doctype feature. Reject DTDs before parsing instead.
            if (xml.length > 2_000_000 || xml.contains("<!DOCTYPE", ignoreCase = true) ||
                xml.contains("<!ENTITY", ignoreCase = true)) return emptyList()
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
                runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
                runCatching { isXIncludeAware = false }
                isExpandEntityReferences = false
            }
            val items = factory.newDocumentBuilder().parse(InputSource(StringReader(xml))).getElementsByTagName("item")
            return (0 until items.length).mapNotNull { index ->
                val item = items.item(index) as? Element ?: return@mapNotNull null
                val link = item.childText("link")?.trim().orEmpty()
                val title = item.childText("title")?.trim().orEmpty()
                if (!link.startsWith("https://") || title.isBlank()) return@mapNotNull null
                val image = (0 until item.childNodes.length).mapNotNull { childIndex ->
                    val child = item.childNodes.item(childIndex) as? Element ?: return@mapNotNull null
                    if (child.localName == "thumbnail" || child.localName == "content")
                        child.getAttribute("url").takeIf { it.startsWith("https://") }
                    else null
                }.firstOrNull()
                Article(
                    id = link,
                    title = title,
                    url = link,
                    publishedAt = rssDateToIso(item.childText("pubDate").orEmpty()),
                    sectionName = publisher,
                    fields = Article.Fields(
                        urlToImage = image,
                        description = item.childText("description")
                    )
                )
            }
        }

        private fun rssDateToIso(date: String): String = runCatching {
            val parsed = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(date)
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(parsed!!)
        }.getOrDefault("")

        private fun Element.childText(name: String): String? =
            (0 until childNodes.length).map { childNodes.item(it) }
                .firstOrNull { it.nodeType == Node.ELEMENT_NODE && it.nodeName == name }
                ?.textContent
    }
}
