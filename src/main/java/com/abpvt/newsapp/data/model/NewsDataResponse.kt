package com.abpvt.newsapp.data.model

import com.google.gson.annotations.SerializedName

data class NewsDataResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("results") val results: List<NewsDataArticle> = emptyList()
)

data class NewsDataArticle(
    @SerializedName("article_id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("link") val link: String? = null,
    @SerializedName("pubDate") val publishedAt: String? = null,
    @SerializedName("source_name") val sourceName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("creator") val creators: List<String>? = null
) {
    fun toArticle(): Article? {
        val articleUrl = link?.takeIf { it.startsWith("https://") || it.startsWith("http://") } ?: return null
        val headline = title?.takeIf { it.isNotBlank() } ?: return null
        return Article(
            id = id?.takeIf { it.isNotBlank() } ?: articleUrl,
            title = headline,
            url = articleUrl,
            publishedAt = publishedAt.orEmpty(),
            sectionName = sourceName?.takeIf { it.isNotBlank() } ?: "NewsData.io",
            fields = Article.Fields(urlToImage = imageUrl, description = description, author = creators?.firstOrNull())
        )
    }
}
