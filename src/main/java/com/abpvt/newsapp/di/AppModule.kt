package com.abpvt.newsapp.di

import com.abpvt.newsapp.data.remote.CurrentsApiService
import com.abpvt.newsapp.data.remote.GNewsApiService
import com.abpvt.newsapp.data.remote.NewsApiService
import com.abpvt.newsapp.data.remote.BackendNewsApiService
import com.abpvt.newsapp.data.remote.AppCheckInterceptor
import com.abpvt.newsapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module that provides all network-layer singletons.
 *
 * Why @Named? Each API has a different base URL, so we need three separate
 * Retrofit instances. @Named lets Hilt distinguish between them.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ── Retrofit instances (one per base URL) ──────────────────────────────

    @Provides
    @Singleton
    @Named("guardian")
    fun provideGuardianRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.GUARDIAN_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("gnews")
    fun provideGNewsRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.GNEWS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("currents")
    fun provideCurrentsRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.CURRENTS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("backend")
    fun provideBackendRetrofit(appCheckInterceptor: AppCheckInterceptor): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BACKEND_BASE_URL)
            .client(okhttp3.OkHttpClient.Builder().addInterceptor(appCheckInterceptor).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ── API service interfaces ─────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideGuardianService(@Named("guardian") retrofit: Retrofit): NewsApiService =
        retrofit.create(NewsApiService::class.java)

    @Provides
    @Singleton
    fun provideGNewsService(@Named("gnews") retrofit: Retrofit): GNewsApiService =
        retrofit.create(GNewsApiService::class.java)

    @Provides
    @Singleton
    fun provideCurrentsService(@Named("currents") retrofit: Retrofit): CurrentsApiService =
        retrofit.create(CurrentsApiService::class.java)

    @Provides
    @Singleton
    fun provideBackendService(@Named("backend") retrofit: Retrofit): BackendNewsApiService =
        retrofit.create(BackendNewsApiService::class.java)
}
