package com.abpvt.newsapp.utils

import com.abpvt.newsapp.BuildConfig

object Constants {
    val BACKEND_BASE_URL: String get() = BuildConfig.NEWSYNK_BACKEND_URL
    val USE_SECURE_BACKEND: Boolean get() = !BACKEND_BASE_URL.contains("example.invalid")
    // Guardian
    const val GUARDIAN_BASE_URL = "https://content.guardianapis.com/"
    val GUARDIAN_API_KEY: String get() = BuildConfig.NEWS_API_KEY

    // GNews
    const val GNEWS_BASE_URL = "https://gnews.io/api/v4/"
    val GNEWS_API_KEY: String get() = BuildConfig.GNEWS_API_KEY

    // Currents
    const val CURRENTS_BASE_URL = "https://api.currentsapi.services/v1/"
    val CURRENTS_API_KEY: String get() = BuildConfig.CURRENTS_API_KEY

    // NewsData.io
    const val NEWSDATA_BASE_URL = "https://newsdata.io/api/1/"
    val NEWSDATA_API_KEY: String get() = BuildConfig.NEWSDATA_API_KEY

    // Legacy alias so any existing code referencing Constants.BASE_URL or NEWS_API_KEY still compiles
    const val BASE_URL = GUARDIAN_BASE_URL
    val NEWS_API_KEY: String get() = GUARDIAN_API_KEY
}
