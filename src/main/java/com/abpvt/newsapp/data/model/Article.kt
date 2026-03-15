package com.abpvt.newsapp.data.model

data class Article(
    val source: Article.Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
) {
    data class Source(
        val id: String?,
        val name: String
    )
}
