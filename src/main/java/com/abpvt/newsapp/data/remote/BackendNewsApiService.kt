package com.abpvt.newsapp.data.remote

import com.abpvt.newsapp.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BackendNewsApiService {
    @GET("feed")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("query") query: String? = null,
        @Query("category") category: String? = null
    ): Response<NewsResponse>
}
