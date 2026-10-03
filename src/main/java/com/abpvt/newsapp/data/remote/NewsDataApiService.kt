package com.abpvt.newsapp.data.remote

import com.abpvt.newsapp.data.model.NewsDataResponse
import com.abpvt.newsapp.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** NewsData.io uses cursor-based pagination; this service intentionally fetches page one. */
interface NewsDataApiService {
    @GET("latest")
    suspend fun getLatest(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("language") language: String = "en",
        @Query("apikey") apiKey: String = Constants.NEWSDATA_API_KEY
    ): Response<NewsDataResponse>
}
