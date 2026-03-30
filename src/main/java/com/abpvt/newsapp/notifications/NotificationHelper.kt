package com.abpvt.newsapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.abpvt.newsapp.MainActivity
import com.abpvt.newsapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object NotificationHelper {

    // ── Channel creation ──────────────────────────────────────────────────────

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    NotificationsPrefs.CHANNEL_BREAKING,
                    "Breaking News",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent breaking-news alerts"
                    enableVibration(true)
                },
                NotificationChannel(
                    NotificationsPrefs.CHANNEL_CATEGORY,
                    "News Categories",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Sports, Tech, Business, Health and more"
                },
                NotificationChannel(
                    NotificationsPrefs.CHANNEL_COMMENTS,
                    "Comment Replies",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Replies to your article comments"
                },
                NotificationChannel(
                    NotificationsPrefs.CHANNEL_DIGEST,
                    "Daily Digest",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Your morning news summary"
                }
            )
        )
    }

    // ── Quiet-hours guard ─────────────────────────────────────────────────────

    private fun isQuietHours(context: Context): Boolean {
        val prefs = context.getSharedPreferences(NotificationsPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(NotificationsPrefs.KEY_QUIET_HOURS_ENABLED, false)) return false
        val startHour = prefs.getInt(NotificationsPrefs.KEY_QUIET_START_HOUR, 23)
        val endHour   = prefs.getInt(NotificationsPrefs.KEY_QUIET_END_HOUR, 7)
        val nowHour   = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (startHour <= endHour) {
            nowHour in startHour until endHour
        } else {
            nowHour >= startHour || nowHour < endHour
        }
    }

    // ── Deep-link pending intent ───────────────────────────────────────────────

    private fun articleDeepLinkIntent(context: Context, articleUrl: String?, articleTitle: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!articleUrl.isNullOrBlank()) {
                putExtra(NotificationsPrefs.EXTRA_ARTICLE_URL, articleUrl)
                putExtra(NotificationsPrefs.EXTRA_ARTICLE_TITLE, articleTitle)
            }
        }
        return PendingIntent.getActivity(
            context,
            articleUrl.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ── Public post functions ─────────────────────────────────────────────────

    /**
     * Post a breaking-news alert (high priority, vibration).
     * Supports article deep-link and optional large image from URL.
     */
    suspend fun postBreakingNews(
        context: Context,
        title: String,
        body: String,
        articleUrl: String? = null,
        imageUrl: String? = null
    ) {
        if (isQuietHours(context)) return
        val bitmap = loadBitmap(context, imageUrl)
        val pendingIntent = articleDeepLinkIntent(context, articleUrl, title)

        val builder = NotificationCompat.Builder(context, NotificationsPrefs.CHANNEL_BREAKING)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔥 $title")
            .setContentText(body)
            .setStyle(
                if (bitmap != null)
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?)
                        .setSummaryText(body)
                else
                    NotificationCompat.BigTextStyle().bigText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(NotificationsPrefs.GROUP_NEWS)
            .addAction(android.R.drawable.ic_menu_view, "Read Now", pendingIntent)

        if (bitmap != null) builder.setLargeIcon(bitmap)

        postGrouped(context, builder, NotificationsPrefs.GROUP_NEWS,
            NotificationsPrefs.CHANNEL_BREAKING, "Breaking News")
    }

    /**
     * Post a category-specific news notification (sports/tech/business/…).
     */
    suspend fun postCategoryNews(
        context: Context,
        category: String,
        title: String,
        body: String,
        articleUrl: String? = null,
        imageUrl: String? = null
    ) {
        if (isQuietHours(context)) return
        val bitmap    = loadBitmap(context, imageUrl)
        val emoji     = categoryEmoji(category)
        val pendingIntent = articleDeepLinkIntent(context, articleUrl, title)

        val builder = NotificationCompat.Builder(context, NotificationsPrefs.CHANNEL_CATEGORY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$emoji $title")
            .setContentText(body)
            .setSubText(category.replaceFirstChar { it.uppercase() })
            .setStyle(
                if (bitmap != null)
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?)
                        .setSummaryText(body)
                else
                    NotificationCompat.BigTextStyle().bigText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(NotificationsPrefs.GROUP_NEWS)
            .addAction(android.R.drawable.ic_menu_view, "Read", pendingIntent)

        if (bitmap != null) builder.setLargeIcon(bitmap)

        postGrouped(context, builder, NotificationsPrefs.GROUP_NEWS,
            NotificationsPrefs.CHANNEL_CATEGORY, "Latest Stories")
    }

    /**
     * Post a comment-reply notification.
     */
    fun postCommentReply(
        context: Context,
        replierName: String,
        snippet: String,
        articleUrl: String? = null
    ) {
        if (isQuietHours(context)) return
        val pendingIntent = articleDeepLinkIntent(context, articleUrl, null)

        val builder = NotificationCompat.Builder(context, NotificationsPrefs.CHANNEL_COMMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("💬 $replierName replied to you")
            .setContentText(snippet)
            .setStyle(NotificationCompat.BigTextStyle().bigText(snippet))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(NotificationsPrefs.GROUP_COMMENTS)

        postGrouped(context, builder, NotificationsPrefs.GROUP_COMMENTS,
            NotificationsPrefs.CHANNEL_COMMENTS, "New Replies")
    }

    /**
     * Post the daily morning digest notification.
     */
    fun postDailyDigest(context: Context, headlines: List<String>) {
        val pendingIntent = articleDeepLinkIntent(context, null, null)
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("☀️ Your Morning Digest")
            .setSummaryText("Top ${headlines.size} stories")
        headlines.take(5).forEach { inboxStyle.addLine(it) }

        val notif = NotificationCompat.Builder(context, NotificationsPrefs.CHANNEL_DIGEST)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("☀️ Morning News Digest")
            .setContentText("${headlines.size} top stories for you")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(90001, notif)
    }

    // ── Grouping helper ───────────────────────────────────────────────────────

    private fun postGrouped(
        context: Context,
        builder: NotificationCompat.Builder,
        groupKey: String,
        channelId: String,
        groupSummaryTitle: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = System.currentTimeMillis().toInt()
        manager.notify(id, builder.build())

        // Summary notification required for grouping on Android 7+
        val summary = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(groupSummaryTitle)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        manager.notify(groupKey.hashCode(), summary)
    }

    // ── Image loader ──────────────────────────────────────────────────────────

    private suspend fun loadBitmap(context: Context, url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val loader  = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
                val result  = loader.execute(request)
                (result as? SuccessResult)?.drawable?.let { (it as BitmapDrawable).bitmap }
            } catch (_: Exception) { null }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun categoryEmoji(category: String) = when (category.lowercase()) {
        "sports"        -> "🏆"
        "technology"    -> "💻"
        "business"      -> "💼"
        "health"        -> "🏥"
        "world"         -> "🌍"
        "entertainment" -> "🎬"
        else            -> "📰"
    }
}
