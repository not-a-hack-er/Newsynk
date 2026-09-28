package com.abpvt.newsapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Currents API top-level response.
 * JSON shape:
 * {
 *   "status": "ok",
 *   "news": [ ...CurrentsArticle... ]
 * }
 */
data class CurrentsResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("news")   val news: List<CurrentsArticle> = emptyList()
)

data class CurrentsArticle(
    @SerializedName("id")          val id: String = "",
    @SerializedName("title")       val title: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("url")         val url: String = "",
    @SerializedName("author")      val author: String? = null,
    @SerializedName("image")       val image: String? = null,
    @SerializedName("published")   val published: String = "",
    @SerializedName("category")    val category: List<String> = emptyList()
) {
    /** Map to the shared Article model used everywhere in the UI. */
    fun toArticle(): Article = Article(
        id          = id.ifBlank { url },
        title       = title,
        url         = url,
        publishedAt = published,
        sectionName = category.firstOrNull() ?: "Currents",
        fields      = Article.Fields(
            urlToImage  = image?.takeIf { it != "None" },
            description = description,
            author      = author,
            content     = null
        )
    )
}
