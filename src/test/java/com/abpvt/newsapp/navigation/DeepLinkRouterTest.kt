package com.abpvt.newsapp.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLinkRouterTest {
    @Test
    fun `authenticated startup creates news back stack before opening article`() {
        assertEquals(Screen.News.route, DeepLinkRouter.startDestination(true))
    }

    @Test
    fun `signed out startup begins at login`() {
        assertEquals(Screen.Login.route, DeepLinkRouter.startDestination(false))
    }

    @Test
    fun `notification article waits until news destination exists`() {
        assertFalse(DeepLinkRouter.shouldOpenArticle(Screen.Login.route, "https://example.com", false))
        assertTrue(DeepLinkRouter.shouldOpenArticle(Screen.News.route, "https://example.com", false))
    }

    @Test
    fun `notification article is opened only once`() {
        assertFalse(DeepLinkRouter.shouldOpenArticle(Screen.News.route, "https://example.com", true))
        assertFalse(DeepLinkRouter.shouldOpenArticle(Screen.News.route, "", false))
    }
}
