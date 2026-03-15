package com.abpvt.newsapp.data.remote

import com.abpvt.newsapp.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Singleton Retrofit client providing the NewsApiService. */
object RetrofitClient {
    val newsApiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())  // JSON parser
            .build()
            .create(NewsApiService::class.java)
    }
}
