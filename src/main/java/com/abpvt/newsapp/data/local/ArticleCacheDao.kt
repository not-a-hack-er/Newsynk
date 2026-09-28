package com.abpvt.newsapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ArticleCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(articles: List<CachedArticleEntity>)

    @Query("SELECT * FROM cached_articles WHERE feedKey = :feedKey ORDER BY position ASC LIMIT :limit")
    suspend fun getFeed(feedKey: String, limit: Int = 100): List<CachedArticleEntity>

    @Query("DELETE FROM cached_articles WHERE feedKey = :feedKey")
    suspend fun deleteFeed(feedKey: String)

    @Query("DELETE FROM cached_articles WHERE cachedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Transaction
    suspend fun replaceFeed(feedKey: String, articles: List<CachedArticleEntity>) {
        deleteFeed(feedKey)
        upsertAll(articles)
    }
}
