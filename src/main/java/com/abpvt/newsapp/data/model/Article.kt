package com.abpvt.newsapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single article from The Guardian API.
 * Maps to each item inside response.results[].
 */
data class Article(
    @SerializedName("id")          val id: String = "",
    @SerializedName("webTitle")    val title: String = "",
    @SerializedName("webUrl")      val url: String = "",
    @SerializedName("webPublicationDate") val publishedAt: String = "",
    @SerializedName("sectionName") val sectionName: String = "",
    @SerializedName("fields")      val fields: Fields? = null,
    @Transient val isOffline: Boolean = false,
    @Transient val cachedAt: Long? = null
) {
    /** Optional enriched fields — only present when show-fields is requested. */
    data class Fields(
        @SerializedName("thumbnail")  val urlToImage: String? = null,
        @SerializedName("trailText")  val description: String? = null,
        @SerializedName("byline")     val author: String? = null,
        @SerializedName("bodyText")   val content: String? = null
    )

    // Convenience helpers so existing UI code keeps working unchanged
    val author: String?      get() = fields?.author
    val urlToImage: String?  get() = fields?.urlToImage
    val content: String?     get() = fields?.content

    // Guardian's trailText field contains HTML (<strong>, <em>, etc.)
    // Strip tags so card descriptions show clean plain text
    val description: String?
        get() {
            val raw = fields?.description ?: return null
            return android.text.Html.fromHtml(raw, android.text.Html.FROM_HTML_MODE_COMPACT)
                .toString()
                .trim()
        }

    /** Mirrors the old Article.Source shape used in bookmarks / comments. */
    val source: Source get() {
        val host = runCatching { java.net.URI(url).host?.removePrefix("www.") }.getOrNull()
        val publisher = when {
            host == "theguardian.com" || host?.endsWith(".theguardian.com") == true -> "The Guardian"
            sectionName.lowercase() in CATEGORY_LABELS && !host.isNullOrBlank() -> host
            else -> sectionName.ifBlank { host ?: "Unknown publisher" }
        }
        return Source(id = null, name = publisher)
    }

    data class Source(val id: String?, val name: String)

    private companion object {
        val CATEGORY_LABELS = setOf(
            "technology", "tech", "business", "sport", "sports", "health",
            "society", "world", "culture", "entertainment", "science", "politics", "news"
        )
    }
}
