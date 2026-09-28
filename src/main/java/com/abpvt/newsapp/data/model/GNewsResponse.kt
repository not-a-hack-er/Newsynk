package com.abpvt.newsapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * GNews API top-level response.
 * JSON shape:
 * {
 *   "totalArticles": 100,
 *   "articles": [ ...GNewsArticle... ]
 * }
 */
data class GNewsResponse(
    @SerializedName("totalArticles") val totalArticles: Int = 0,
    @SerializedName("articles")      val articles: List<GNewsArticle> = emptyList()
)

data class GNewsArticle(
    @SerializedName("title")       val title: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("content")     val content: String? = null,
    @SerializedName("url")         val url: String = "",
    @SerializedName("image")       val image: String? = null,
    @SerializedName("publishedAt") val publishedAt: String = "",
    @SerializedName("source")      val source: GNewsSource? = null
) {
    data class GNewsSource(
        @SerializedName("name") val name: String = "",
        @SerializedName("url")  val url: String = ""
    )

    /** Map to the shared Article model used everywhere in the UI. */
    fun toArticle(): Article = Article(
        id          = url,
        title       = title,
        url         = url,
        publishedAt = publishedAt,
        sectionName = source?.name ?: "GNews",
        fields      = Article.Fields(
            urlToImage  = image,
            description = description,
            author      = source?.name,
            content     = content
        )
    )
}
