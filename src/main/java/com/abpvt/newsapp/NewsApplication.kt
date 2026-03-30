package com.abpvt.newsapp

import android.app.Application
import com.abpvt.newsapp.notifications.DailyDigestScheduler
import com.abpvt.newsapp.notifications.NotificationHelper
import com.abpvt.newsapp.notifications.NotificationsPrefs
import com.google.firebase.FirebaseApp

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Create all 4 notification channels
        NotificationHelper.createChannel(this)

        // Schedule daily digest if enabled (default: true)
        val prefs = getSharedPreferences(NotificationsPrefs.PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(NotificationsPrefs.KEY_NOTIF_DIGEST, true)) {
            DailyDigestScheduler.schedule(this)
        }
    }
}
