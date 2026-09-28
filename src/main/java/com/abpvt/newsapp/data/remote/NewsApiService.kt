package com.abpvt.newsapp.data.remote

import com.abpvt.newsapp.data.model.NewsResponse
import com.abpvt.newsapp.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for The Guardian Open Platform API.
 * Base URL: https://content.guardianapis.com/
 *
 * Docs: https://open-platform.theguardian.com/documentation/
 */
interface NewsApiService {

    /**
     * Fetch the latest content across all sections.
     * show-fields=thumbnail,trailText,byline adds the enriched fields.
     */
    @GET("search")
    suspend fun getTopHeadlines(
        @Query("page")        page: Int = 1,
        @Query("order-by")    orderBy: String = "newest",
        @Query("show-fields") showFields: String = "thumbnail,trailText,byline,bodyText",
        @Query("page-size")   pageSize: Int = 30,
        @Query("api-key")     apiKey: String = Constants.NEWS_API_KEY
    ): Response<NewsResponse>

    /**
     * Search or filter content by section / keyword.
     */
    @GET("search")
    suspend fun getLatestNews(
        @Query("q")           query: String,
        @Query("page")        page: Int = 1,
        @Query("order-by")    orderBy: String = "newest",
        @Query("show-fields") showFields: String = "thumbnail,trailText,byline,bodyText",
        @Query("page-size")   pageSize: Int = 30,
        @Query("api-key")     apiKey: String = Constants.NEWS_API_KEY
    ): Response<NewsResponse>

    /**
     * Fetch by section slug, e.g. "sport", "technology", "business".
     */
    @GET("search")
    suspend fun getBySection(
        @Query("section")     section: String,
        @Query("page")        page: Int = 1,
        @Query("order-by")    orderBy: String = "newest",
        @Query("show-fields") showFields: String = "thumbnail,trailText,byline",
        @Query("page-size")   pageSize: Int = 30,
        @Query("api-key")     apiKey: String = Constants.NEWS_API_KEY
    ): Response<NewsResponse>
}
