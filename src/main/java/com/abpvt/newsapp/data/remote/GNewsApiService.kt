package com.abpvt.newsapp.data.remote

import com.abpvt.newsapp.data.model.GNewsResponse
import com.abpvt.newsapp.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for GNews API.
 * Base URL: https://gnews.io/api/v4/
 * Docs: https://gnews.io/docs/v4
 */
interface GNewsApiService {

    /**
     * Fetch top headlines.
     * lang=en → English only
     * max=10  → 10 articles per request (conserves daily quota)
     */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String? = null,
        @Query("page")     page: Int = 1,
        @Query("lang")     language: String = "en",
        @Query("max")      max: Int = 10,
        @Query("apikey")   apiKey: String = Constants.GNEWS_API_KEY
    ): Response<GNewsResponse>

    /**
     * Search GNews by keyword.
     */
    @GET("search")
    suspend fun searchNews(
        @Query("q")       query: String,
        @Query("page")    page: Int = 1,
        @Query("lang")    language: String = "en",
        @Query("max")     max: Int = 10,
        @Query("apikey")  apiKey: String = Constants.GNEWS_API_KEY
    ): Response<GNewsResponse>
}
