package com.abpvt.newsapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedArticleEntity::class],
    version = 1,
    exportSchema = true
)
abstract class NewsynkDatabase : RoomDatabase() {
    abstract fun articleCacheDao(): ArticleCacheDao
}
