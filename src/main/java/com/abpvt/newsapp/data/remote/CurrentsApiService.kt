package com.abpvt.newsapp.data.remote

import com.abpvt.newsapp.data.model.CurrentsResponse
import com.abpvt.newsapp.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for Currents API.
 * Base URL: https://api.currentsapi.services/v1/
 * Docs: https://currentsapi.services/en/docs/
 */
interface CurrentsApiService {

    /**
     * Fetch the latest news.
     * language=en → English only
     */
    @GET("latest-news")
    suspend fun getLatestNews(
        @Query("page_number") page: Int = 1,
        @Query("language")    language: String = "en",
        @Query("apiKey")      apiKey: String = Constants.CURRENTS_API_KEY
    ): Response<CurrentsResponse>

    /**
     * Search Currents by keyword.
     */
    @GET("search")
    suspend fun searchNews(
        @Query("keywords")    keywords: String,
        @Query("page_number") page: Int = 1,
        @Query("language")    language: String = "en",
        @Query("apiKey")      apiKey: String = Constants.CURRENTS_API_KEY
    ): Response<CurrentsResponse>

    /**
     * Search Currents by category (e.g. "technology", "business", "sports", "health", "world").
     */
    @GET("search")
    suspend fun getByCategory(
        @Query("category")    category: String,
        @Query("page_number") page: Int = 1,
        @Query("language")    language: String = "en",
        @Query("apiKey")      apiKey: String = Constants.CURRENTS_API_KEY
    ): Response<CurrentsResponse>
}
