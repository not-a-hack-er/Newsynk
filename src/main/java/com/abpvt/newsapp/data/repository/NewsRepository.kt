package com.abpvt.newsapp.data.repository

import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.local.ArticleCacheDao
import com.abpvt.newsapp.data.local.toArticle
import com.abpvt.newsapp.data.local.toCacheEntity
import com.abpvt.newsapp.data.remote.CurrentsApiService
import com.abpvt.newsapp.data.remote.GNewsApiService
import com.abpvt.newsapp.data.remote.NewsApiService
import com.abpvt.newsapp.data.remote.BackendNewsApiService
import com.abpvt.newsapp.data.remote.NewsDataApiService
import com.abpvt.newsapp.utils.Constants
import com.abpvt.newsapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val guardianService: NewsApiService,
    private val gNewsService: GNewsApiService,
    private val currentsService: CurrentsApiService,
    private val newsDataService: NewsDataApiService,
    private val backendService: BackendNewsApiService,
    private val cacheDao: ArticleCacheDao,
    private val rssFeedSource: RssFeedSource
) {

    suspend fun getPersonalizedNews(topics: Set<String>, page: Int = 1): Resource<List<Article>> {
        val selected = topics.ifEmpty { setOf("Technology", "World", "Business") }.take(4)
        val feedKey = "for-you:${selected.map { it.lowercase() }.sorted().joinToString("-")}:$page"
        return try {
            val articles = coroutineScope {
                selected.map { topic ->
                    async {
                        when (val result = getCategoryNews(topic, page)) {
                            is Resource.Success -> result.data.orEmpty()
                            else -> emptyList()
                        }
                    }
                }.flatMap { it.await() }
            }
                .distinctBy { it.url }
                .sortedWith(
                    compareByDescending<Article> { article ->
                        selected.count { topic ->
                            article.sectionName.contains(topic, true) ||
                                article.title.contains(topic, true) ||
                                article.description?.contains(topic, true) == true
                        }
                    }.thenByDescending { it.publishedAt }
                )
            val fallback = if (articles.isEmpty()) rssFeedSource.headlines(page) else articles
            deliver(feedKey, fallback, "Your personalized feed is not available right now.")
        } catch (e: Exception) {
            cachedOrError(feedKey, e.message ?: "Your personalized feed is not available right now.")
        }
    }

    /**
     * Fetch top headlines across configured providers in parallel.
     */
    suspend fun getTopHeadlines(page: Int = 1): Resource<List<Article>> {
        return withContext(Dispatchers.IO) {
            try {
                if (Constants.USE_SECURE_BACKEND) {
                    val articles = safeCall { backendService.getFeed(page = page) }?.articles.orEmpty()
                    return@withContext deliver("top:$page", articles.ifEmpty { rssFeedSource.headlines(page) }, "No articles available. Please check your connection.")
                }
                val guardianDeferred  = async { fetchGuardianHeadlines(page) }
                val gNewsDeferred     = async { fetchGNewsHeadlines(page) }
                val currentsDeferred  = async { fetchCurrentsHeadlines(page) }
                val newsDataDeferred = async { fetchNewsData(page) }

                val guardianArticles  = guardianDeferred.await()
                val gNewsArticles     = gNewsDeferred.await()
                val currentsArticles  = currentsDeferred.await()
                val newsDataArticles = newsDataDeferred.await()

                val merged = interleave(guardianArticles, gNewsArticles, currentsArticles, newsDataArticles)

                deliver("top:$page", merged.ifEmpty { rssFeedSource.headlines(page) }, "No articles available. Please check your connection.")
            } catch (e: Exception) {
                cachedOrError("top:$page", e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Fetch category-specific articles across all configured providers in parallel.
     * Category slugs: "technology", "business", "sports", "health", "world", "entertainment".
     */
    suspend fun getCategoryNews(category: String, page: Int = 1): Resource<List<Article>> {
        if (category.equals("All", ignoreCase = true)) return getTopHeadlines(page)

        val catLower = category.lowercase()

        // Map category to Guardian section name
        val guardianSection = when (catLower) {
            "tech", "technology" -> "technology"
            "business"           -> "business"
            "sports", "sport"    -> "sport"
            "health"             -> "society"
            "world"              -> "world"
            "entertainment"      -> "culture"
            else                 -> catLower
        }

        // Map category to GNews category
        val gNewsCategory = when (catLower) {
            "tech", "technology" -> "technology"
            "business"           -> "business"
            "sports", "sport"    -> "sports"
            "health"             -> "health"
            "world"              -> "world"
            "entertainment"      -> "entertainment"
            else                 -> catLower
        }

        // Map category to Currents category
        val currentsCategory = when (catLower) {
            "tech", "technology" -> "technology"
            "business"           -> "business"
            "sports", "sport"    -> "sports"
            "health"             -> "health"
            "world"              -> "regional"
            "entertainment"      -> "entertainment"
            else                 -> catLower
        }

        return withContext(Dispatchers.IO) {
            try {
                if (Constants.USE_SECURE_BACKEND) {
                    val articles = safeCall {
                        backendService.getFeed(page = page, category = catLower)
                    }?.articles.orEmpty()
                    return@withContext deliver(
                        "category:$catLower:$page",
                        articles.ifEmpty { rssFeedSource.category(category, page) },
                        "No $category stories are available right now."
                    )
                }
                if (Constants.GUARDIAN_API_KEY.isBlank() &&
                    Constants.GNEWS_API_KEY.isBlank() && Constants.CURRENTS_API_KEY.isBlank() &&
                    Constants.NEWSDATA_API_KEY.isBlank()
                ) {
                    return@withContext deliver(
                        "category:$catLower:$page",
                        rssFeedSource.category(category, page),
                        "No $category stories are available right now."
                    )
                }
                val guardianDeferred = async {
                    if (Constants.GUARDIAN_API_KEY.isBlank()) emptyList() else
                    safeCall { guardianService.getBySection(section = guardianSection, page = page) }
                        ?.articles ?: emptyList()
                }

                val gNewsDeferred = async {
                    if (Constants.GNEWS_API_KEY.isBlank()) emptyList() else
                    safeCall { gNewsService.getTopHeadlines(category = gNewsCategory, page = page) }
                        ?.articles?.map { it.toArticle() } ?: emptyList()
                }

                val currentsDeferred = async {
                    if (Constants.CURRENTS_API_KEY.isBlank()) emptyList() else
                    safeCall { currentsService.getByCategory(category = currentsCategory, page = page) }
                        ?.news?.map { it.toArticle() } ?: emptyList()
                }

                val newsDataDeferred = async { fetchNewsData(page, category = newsDataCategory(catLower)) }

                val merged = interleave(
                    guardianDeferred.await(), gNewsDeferred.await(),
                    currentsDeferred.await(), newsDataDeferred.await()
                )

                if (merged.isEmpty()) {
                    // Fallback to keyword search if section returned no results
                    val fallback = getLatestNews(query = category, page = page)
                    if (fallback is Resource.Success) {
                        val articles = fallback.data.orEmpty()
                        cache("category:$catLower:$page", articles)
                        Resource.Success(articles)
                    } else {
                        deliver("category:$catLower:$page", rssFeedSource.category(category, page), "No $category stories are available right now.")
                    }
                } else {
                    deliver("category:$catLower:$page", merged, "No $category stories are available right now.")
                }
            } catch (e: Exception) {
                cachedOrError("category:$catLower:$page", e.message ?: "Failed to load $category news")
            }
        }
    }

    /**
     * Search across all configured providers simultaneously.
     */
    suspend fun getLatestNews(query: String = "general", page: Int = 1): Resource<List<Article>> {
        return withContext(Dispatchers.IO) {
            try {
                if (Constants.USE_SECURE_BACKEND) {
                    val articles = safeCall {
                        backendService.getFeed(page = page, query = query)
                    }?.articles.orEmpty()
                    return@withContext deliver(
                        "search:${query.trim().lowercase()}:$page",
                        articles.ifEmpty { rssFeedSource.search(query, page) },
                        "No results found for \"$query\""
                    )
                }
                val guardianDeferred = async {
                    if (Constants.GUARDIAN_API_KEY.isBlank()) emptyList() else
                    safeCall { guardianService.getLatestNews(query = query, page = page) }
                        ?.articles ?: emptyList()
                }
                val gNewsDeferred = async {
                    if (Constants.GNEWS_API_KEY.isBlank()) emptyList() else
                    safeCall { gNewsService.searchNews(query = query, page = page) }
                        ?.articles?.map { it.toArticle() } ?: emptyList()
                }
                val currentsDeferred = async {
                    if (Constants.CURRENTS_API_KEY.isBlank()) emptyList() else
                    safeCall { currentsService.searchNews(keywords = query, page = page) }
                        ?.news?.map { it.toArticle() } ?: emptyList()
                }

                val newsDataDeferred = async { fetchNewsData(page, query = query) }

                val merged = interleave(
                    guardianDeferred.await(), gNewsDeferred.await(),
                    currentsDeferred.await(), newsDataDeferred.await()
                )

                deliver("search:${query.trim().lowercase()}:$page", merged.ifEmpty { rssFeedSource.search(query, page) }, "No results found for \"$query\"")
            } catch (e: Exception) {
                cachedOrError(
                    "search:${query.trim().lowercase()}:$page",
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private suspend fun fetchGuardianHeadlines(page: Int): List<Article> =
        if (Constants.GUARDIAN_API_KEY.isBlank()) emptyList()
        else safeCall { guardianService.getTopHeadlines(page = page) }?.articles ?: emptyList()

    private suspend fun fetchGNewsHeadlines(page: Int): List<Article> =
        if (Constants.GNEWS_API_KEY.isBlank()) emptyList() else safeCall { gNewsService.getTopHeadlines(page = page) }
            ?.articles?.map { it.toArticle() } ?: emptyList()

    private suspend fun fetchCurrentsHeadlines(page: Int): List<Article> =
        if (Constants.CURRENTS_API_KEY.isBlank()) emptyList() else safeCall { currentsService.getLatestNews(page = page) }
            ?.news?.map { it.toArticle() } ?: emptyList()

    private suspend fun fetchNewsData(page: Int, query: String? = null, category: String? = null): List<Article> =
        if (Constants.NEWSDATA_API_KEY.isBlank() || page != 1) emptyList()
        else safeCall { newsDataService.getLatest(query = query, category = category) }
            ?.results?.mapNotNull { it.toArticle() } ?: emptyList()

    private fun newsDataCategory(category: String): String = when (category) {
        "tech" -> "technology"
        "sports" -> "sports"
        "entertainment" -> "entertainment"
        else -> category
    }

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): T? {
        return try {
            val response = call()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun deliver(
        feedKey: String,
        articles: List<Article>,
        emptyMessage: String
    ): Resource<List<Article>> {
        return if (articles.isNotEmpty()) {
            cache(feedKey, articles)
            Resource.Success(articles)
        } else {
            cachedOrError(feedKey, emptyMessage)
        }
    }

    private suspend fun cache(feedKey: String, articles: List<Article>) {
        if (articles.isEmpty()) return
        val now = System.currentTimeMillis()
        cacheDao.replaceFeed(
            feedKey,
            articles.distinctBy { it.url }.mapIndexed { index, article ->
                article.toCacheEntity(feedKey, index, now)
            }
        )
        // Keep a rolling seven-day cache without growing indefinitely.
        cacheDao.deleteOlderThan(now - CACHE_TTL_MS)
    }

    private suspend fun cachedOrError(feedKey: String, message: String): Resource<List<Article>> {
        val cached = cacheDao.getFeed(feedKey).map { it.toArticle() }
        return if (cached.isNotEmpty()) Resource.Success(cached) else Resource.Error(message)
    }

    private fun interleave(vararg sources: List<Article>): List<Article> {
        // Fairly mix publishers without dropping results when one provider returns fewer stories.
        val result = mutableListOf<Article>()
        val iters = sources.filter { it.isNotEmpty() }.map { it.iterator() }
        var anyLeft = true
        while (anyLeft) {
            anyLeft = false
            for (iter in iters) {
                if (iter.hasNext()) { result.add(iter.next()); anyLeft = true }
            }
        }
        return result.distinctBy { it.url.substringBefore('?').trimEnd('/').lowercase() }
    }

    private companion object {
        const val CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1000L
    }
}
