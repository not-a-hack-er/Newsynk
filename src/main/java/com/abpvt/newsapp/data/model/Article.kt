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
    @SerializedName("fields")      val fields: Fields? = null
) {
    /** Optional enriched fields — only present when show-fields is requested. */
    data class Fields(
        @SerializedName("thumbnail")  val urlToImage: String? = null,
        @SerializedName("trailText")  val description: String? = null,
        @SerializedName("byline")     val author: String? = null,
        @SerializedName("bodyText")   val content: String? = null
    )

    // Convenience helpers so existing UI code keeps working unchanged
    val author: String?     get() = fields?.author
    val description: String? get() = fields?.description
    val urlToImage: String? get() = fields?.urlToImage
    val content: String?    get() = fields?.content

    /** Mirrors the old Article.Source shape used in bookmarks / comments. */
    val source: Source get() = Source(id = null, name = sectionName.ifBlank { "The Guardian" })

    data class Source(val id: String?, val name: String)
}
