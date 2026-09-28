package com.abpvt.newsapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.abpvt.newsapp.notifications.DailyDigestScheduler
import com.abpvt.newsapp.notifications.NotificationHelper
import com.abpvt.newsapp.notifications.NotificationsPrefs
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NewsApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Provide the custom Configuration with HiltWorkerFactory so WorkManager
    // can inject dependencies (@HiltWorker) into CoroutineWorker subclasses.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)?.let {
            AppCheckInitializer.install()
            val privacyPrefs = getSharedPreferences(NotificationsPrefs.PREFS_NAME, MODE_PRIVATE)
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(
                privacyPrefs.getBoolean(NotificationsPrefs.KEY_ANALYTICS_CONSENT, false)
            )
        }

        // Create all notification channels
        NotificationHelper.createChannel(this)

        // Schedule daily digest if enabled (default: true)
        val prefs = getSharedPreferences(NotificationsPrefs.PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(NotificationsPrefs.KEY_NOTIF_DIGEST, true)) {
            DailyDigestScheduler.schedule(
                this,
                prefs.getInt(NotificationsPrefs.KEY_DIGEST_HOUR, 8)
            )
        }
    }
}
