package com.abpvt.newsapp.notifications

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules a daily morning digest notification using WorkManager.
 * Fires once per day at [targetHour]:00 (default 8 AM).
 */
object DailyDigestScheduler {

    private const val WORK_TAG = "daily_digest_work"

    fun schedule(context: Context, targetHour: Int = 8) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            // If time already passed today, schedule for tomorrow
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<DigestWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}

/**
 * WorkManager worker that posts the digest notification.
 * In a real app this would fetch headlines from the API.
 * Here we post a well-formatted sample digest.
 */
class DigestWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(
            NotificationsPrefs.PREFS_NAME, Context.MODE_PRIVATE
        )
        if (!prefs.getBoolean(NotificationsPrefs.KEY_NOTIF_DIGEST, true)) return Result.success()

        // Sample headlines — replace with actual API call in production
        val headlines = listOf(
            "Top markets rally as inflation cools",
            "India set to launch new space mission",
            "AI breakthrough in medical diagnostics",
            "Champions League: Quarter-finals set",
            "New climate deal reached at summit"
        )
        NotificationHelper.postDailyDigest(applicationContext, headlines)
        return Result.success()
    }
}
