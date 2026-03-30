package com.abpvt.newsapp.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives FCM messages and routes them to the correct notification type.
 *
 * Expected FCM data payload keys:
 *   type        → "breaking" | "category" | "comment_reply" | "digest"
 *   category    → "sports" | "technology" | "business" | "health" | "world" | "entertainment"
 *   title       → notification title
 *   body        → notification body
 *   article_url → URL to deep-link into
 *   image_url   → optional image for rich notification
 *   replier     → commenter display name (for comment_reply type)
 */
class NewsFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message from: ${remoteMessage.from}")

        val data       = remoteMessage.data
        val type       = data["type"] ?: "breaking"
        val title      = remoteMessage.notification?.title ?: data["title"] ?: "Newsynk"
        val body       = remoteMessage.notification?.body  ?: data["body"]  ?: ""
        val articleUrl = data[NotificationsPrefs.EXTRA_ARTICLE_URL]
        val imageUrl   = data["image_url"] ?: remoteMessage.notification?.imageUrl?.toString()
        val category   = data["category"]  ?: ""
        val replier    = data["replier"]   ?: "Someone"

        val prefs = getSharedPreferences(NotificationsPrefs.PREFS_NAME, MODE_PRIVATE)

        when (type) {
            "breaking" -> {
                if (!prefs.getBoolean(NotificationsPrefs.KEY_NOTIF_BREAKING, true)) return
                scope.launch {
                    NotificationHelper.postBreakingNews(
                        applicationContext, title, body, articleUrl, imageUrl
                    )
                }
            }
            "category" -> {
                val prefKey = categoryPrefKey(category)
                if (!prefs.getBoolean(prefKey, true)) return
                scope.launch {
                    NotificationHelper.postCategoryNews(
                        applicationContext, category, title, body, articleUrl, imageUrl
                    )
                }
            }
            "comment_reply" -> {
                if (!prefs.getBoolean(NotificationsPrefs.KEY_NOTIF_COMMENTS, true)) return
                NotificationHelper.postCommentReply(
                    applicationContext, replier, body, articleUrl
                )
            }
            "digest" -> {
                if (!prefs.getBoolean(NotificationsPrefs.KEY_NOTIF_DIGEST, true)) return
                // Body is a newline-separated list of headlines
                val headlines = body.split("\n").filter { it.isNotBlank() }
                NotificationHelper.postDailyDigest(applicationContext, headlines)
            }
            else -> {
                // Fallback: treat as breaking news
                scope.launch {
                    NotificationHelper.postBreakingNews(
                        applicationContext, title, body, articleUrl, imageUrl
                    )
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        val prefs = getSharedPreferences(NotificationsPrefs.PREFS_NAME, MODE_PRIVATE)
        // Re-subscribe to all enabled topics
        allTopics.forEach { (prefKey, topic) ->
            if (prefs.getBoolean(prefKey, true)) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
            }
        }
    }

    private fun categoryPrefKey(category: String) = when (category.lowercase()) {
        "sports"        -> NotificationsPrefs.KEY_NOTIF_SPORTS
        "technology"    -> NotificationsPrefs.KEY_NOTIF_TECH
        "business"      -> NotificationsPrefs.KEY_NOTIF_BUSINESS
        "health"        -> NotificationsPrefs.KEY_NOTIF_HEALTH
        "world"         -> NotificationsPrefs.KEY_NOTIF_WORLD
        "entertainment" -> NotificationsPrefs.KEY_NOTIF_ENTERTAINMENT
        else            -> NotificationsPrefs.KEY_NOTIF_BREAKING
    }

    companion object {
        /** All pref-key → topic pairs, used for token-refresh re-subscription. */
        val allTopics = listOf(
            NotificationsPrefs.KEY_NOTIF_BREAKING     to NotificationsPrefs.TOPIC_BREAKING_NEWS,
            NotificationsPrefs.KEY_NOTIF_SPORTS       to NotificationsPrefs.TOPIC_SPORTS,
            NotificationsPrefs.KEY_NOTIF_TECH         to NotificationsPrefs.TOPIC_TECH,
            NotificationsPrefs.KEY_NOTIF_BUSINESS     to NotificationsPrefs.TOPIC_BUSINESS,
            NotificationsPrefs.KEY_NOTIF_HEALTH       to NotificationsPrefs.TOPIC_HEALTH,
            NotificationsPrefs.KEY_NOTIF_WORLD        to NotificationsPrefs.TOPIC_WORLD,
            NotificationsPrefs.KEY_NOTIF_ENTERTAINMENT to NotificationsPrefs.TOPIC_ENTERTAINMENT,
            NotificationsPrefs.KEY_NOTIF_COMMENTS     to NotificationsPrefs.TOPIC_COMMENTS,
        )
    }
}
