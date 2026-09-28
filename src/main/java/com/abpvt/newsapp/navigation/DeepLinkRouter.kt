package com.abpvt.newsapp.navigation

object DeepLinkRouter {
    fun startDestination(hasValidSession: Boolean): String =
        if (hasValidSession) Screen.News.route else Screen.Login.route

    fun shouldOpenArticle(currentRoute: String?, articleUrl: String?, alreadyHandled: Boolean): Boolean =
        !alreadyHandled && currentRoute == Screen.News.route && !articleUrl.isNullOrBlank()
}
