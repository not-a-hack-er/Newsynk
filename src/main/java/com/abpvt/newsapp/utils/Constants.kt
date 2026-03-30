package com.abpvt.newsapp.utils

import com.abpvt.newsapp.BuildConfig

object Constants {
    const val BASE_URL = "https://content.guardianapis.com/"
    // API key is injected from local.properties via buildConfigField — never hardcoded in source
    val NEWS_API_KEY: String get() = BuildConfig.NEWS_API_KEY
}
