package com.abpvt.newsapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abpvt.newsapp.data.model.Article

@Entity(
    tableName = "cached_articles",
    indices = [Index("feedKey"), Index("url")]
)
data class CachedArticleEntity(
    @PrimaryKey val cacheKey: String,
    val feedKey: String,
    val position: Int,
    val id: String,
    val title: String,
    val url: String,
    val publishedAt: String,
    val sectionName: String,
    val imageUrl: String?,
    val description: String?,
    val author: String?,
    val content: String?,
    val cachedAt: Long
)

fun Article.toCacheEntity(feedKey: String, position: Int, cachedAt: Long) = CachedArticleEntity(
    cacheKey = "$feedKey|$url",
    feedKey = feedKey,
    position = position,
    id = id,
    title = title,
    url = url,
    publishedAt = publishedAt,
    sectionName = sectionName,
    imageUrl = urlToImage,
    description = description,
    author = author,
    content = content,
    cachedAt = cachedAt
)

fun CachedArticleEntity.toArticle() = Article(
    id = id,
    title = title,
    url = url,
    publishedAt = publishedAt,
    sectionName = sectionName,
    fields = Article.Fields(
        urlToImage = imageUrl,
        description = description,
        author = author,
        content = content
    ),
    isOffline = true,
    cachedAt = cachedAt
)
