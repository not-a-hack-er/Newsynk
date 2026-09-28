package com.abpvt.newsapp.di

import android.content.Context
import androidx.room.Room
import com.abpvt.newsapp.data.local.ArticleCacheDao
import com.abpvt.newsapp.data.local.NewsynkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewsynkDatabase =
        Room.databaseBuilder(context, NewsynkDatabase::class.java, "newsynk.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideArticleCacheDao(database: NewsynkDatabase): ArticleCacheDao =
        database.articleCacheDao()
}
