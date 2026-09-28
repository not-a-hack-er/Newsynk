package com.abpvt.newsapp.data.remote

import android.content.Context
import com.abpvt.newsapp.utils.Constants
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCheckInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!Constants.USE_SECURE_BACKEND || FirebaseApp.getApps(context).isEmpty()) {
            return chain.proceed(chain.request())
        }
        val token = runCatching {
            Tasks.await(FirebaseAppCheck.getInstance().getAppCheckToken(false)).token
        }.getOrNull()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) header("X-Firebase-AppCheck", token)
        }.build()
        return chain.proceed(request)
    }
}
