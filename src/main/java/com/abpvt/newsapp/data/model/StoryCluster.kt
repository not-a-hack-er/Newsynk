package com.abpvt.newsapp.data.model

data class StoryCluster(
    val id: String,
    val primary: Article,
    val related: List<Article>
) {
    val sourceCount: Int
        get() = (listOf(primary) + related).map { it.source.name }.distinct().size

    val allArticles: List<Article>
        get() = listOf(primary) + related
}
