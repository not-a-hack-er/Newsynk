package com.abpvt.newsapp.data.repository

import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.remote.NewsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.abpvt.newsapp.utils.Resource

class NewsRepository(private val apiService: NewsApiService) {

    /** Fetch top headlines from the News API. */
    suspend fun getTopHeadlines(country: String = "us"): Resource<List<Article>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTopHeadlines(country)
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    if (newsResponse != null) {
                        Resource.Success(newsResponse.articles)
                    } else {
                        Resource.Error("Empty response")
                    }
                } else {
                    // API returned an error code
                    Resource.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: HttpException) {
                // Non-2xx HTTP response as exception
                Resource.Error("Network error: ${e.message()}")
            } catch (e: IOException) {
                // IO/network problem (no connection, timeout, etc.)
                Resource.Error("Network error: Please check your connection")
            } catch (e: Exception) {
                // Unknown or unexpected exception
                Resource.Error(e.message ?: "Unknown error occurred")
            }
            }
        }
// Removed extra brace

    /** Fetch latest news sorted by publication time. */
    suspend fun getLatestNews(query: String = "general"): Resource<List<Article>> {
        return withContext(Dispatchers.IO) {
            try {
                // Use the new endpoint with sortBy=publishedAt
                val response = apiService.getLatestNews(query)
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    if (newsResponse != null) {
                        Resource.Success(newsResponse.articles)
                    } else {
                        Resource.Error("Empty response")
                    }
                } else {
                    Resource.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
