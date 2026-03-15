package com.abpvt.newsapp.data.remote

import com.abpvt.newsapp.data.model.NewsResponse
import com.abpvt.newsapp.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit service interface for the News API. */
interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String = Constants.NEWS_API_KEY
    ): Response<NewsResponse>

    @GET("everything")
    suspend fun getLatestNews(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String = Constants.NEWS_API_KEY
    ): Response<NewsResponse>
}
