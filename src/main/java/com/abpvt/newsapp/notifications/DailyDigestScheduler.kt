package com.abpvt.newsapp.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.abpvt.newsapp.data.repository.NewsRepository
import com.abpvt.newsapp.data.repository.PersonalizationRepository
import com.abpvt.newsapp.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}

/**
 * CoroutineWorker that fetches real top headlines from [NewsRepository]
 * and posts them as the morning digest notification.
 *
 * Uses @HiltWorker + @AssistedInject so Hilt can inject [NewsRepository]
 * without needing a manual factory.
 */
@HiltWorker
class DigestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val newsRepository: NewsRepository,
    private val personalizationRepository: PersonalizationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(
            NotificationsPrefs.PREFS_NAME, Context.MODE_PRIVATE
        )
        if (!prefs.getBoolean(NotificationsPrefs.KEY_NOTIF_DIGEST, true)) return Result.success()

        // Fetch real headlines from the API
        val headlines: List<String> = try {
            when (val res = newsRepository.getPersonalizedNews(
                personalizationRepository.followedTopicsSnapshot(),
                page = 1
            )) {
                is Resource.Success -> {
                    res.data
                        ?.take(5)
                        ?.mapNotNull { it.title.takeIf { t -> t.isNotBlank() } }
                        ?: fallbackHeadlines()
                }
                else -> fallbackHeadlines()
            }
        } catch (e: Exception) {
            fallbackHeadlines()
        }

        NotificationHelper.postDailyDigest(applicationContext, headlines)
        return Result.success()
    }

    private fun fallbackHeadlines(): List<String> = listOf(
        "Good morning! Your daily news digest is ready.",
        "Open Newsynk to catch up on today's top stories."
    )
}
