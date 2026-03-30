package com.abpvt.newsapp.notifications

/**
 * Shared constants for all notification topics, preference keys, and channel IDs.
 */
object NotificationsPrefs {

    // ── SharedPreferences file ────────────────────────────────────────────────
    const val PREFS_NAME = "newsapp_prefs"

    // ── Profile / avatar keys ─────────────────────────────────────────────────
    const val KEY_AVATAR_COLOR     = "avatar_color"
    const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"

    // ── Category notification toggle keys ────────────────────────────────────
    const val KEY_NOTIF_BREAKING     = "notif_breaking"
    const val KEY_NOTIF_SPORTS       = "notif_sports"
    const val KEY_NOTIF_TECH         = "notif_technology"
    const val KEY_NOTIF_BUSINESS     = "notif_business"
    const val KEY_NOTIF_HEALTH       = "notif_health"
    const val KEY_NOTIF_WORLD        = "notif_world"
    const val KEY_NOTIF_ENTERTAINMENT = "notif_entertainment"
    const val KEY_NOTIF_COMMENTS     = "notif_comments"   // comment-reply alerts
    const val KEY_NOTIF_DIGEST       = "notif_daily_digest"

    // ── Quiet hours ───────────────────────────────────────────────────────────
    const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
    const val KEY_QUIET_START_HOUR    = "quiet_start_hour"   // 0-23
    const val KEY_QUIET_END_HOUR      = "quiet_end_hour"     // 0-23

    // ── FCM topic names ───────────────────────────────────────────────────────
    const val TOPIC_BREAKING_NEWS   = "breaking_news"
    const val TOPIC_SPORTS          = "sports"
    const val TOPIC_TECH            = "technology"
    const val TOPIC_BUSINESS        = "business"
    const val TOPIC_HEALTH          = "health"
    const val TOPIC_WORLD           = "world"
    const val TOPIC_ENTERTAINMENT   = "entertainment"
    const val TOPIC_COMMENTS        = "comment_replies"

    // ── Notification channel IDs ──────────────────────────────────────────────
    const val CHANNEL_BREAKING      = "breaking_news"
    const val CHANNEL_CATEGORY      = "category_news"
    const val CHANNEL_COMMENTS      = "comment_replies"
    const val CHANNEL_DIGEST        = "daily_digest"

    // ── Notification group keys ───────────────────────────────────────────────
    const val GROUP_NEWS            = "com.abpvt.newsapp.NEWS_GROUP"
    const val GROUP_COMMENTS        = "com.abpvt.newsapp.COMMENTS_GROUP"

    // ── Deep-link extras ─────────────────────────────────────────────────────
    const val EXTRA_ARTICLE_URL     = "article_url"
    const val EXTRA_ARTICLE_TITLE   = "article_title"
}
