package com.abpvt.newsapp.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ReaderPalette { SYSTEM, LIGHT, DARK, SEPIA }

@Singleton
class PersonalizationRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _followedTopics = MutableStateFlow(
        prefs.getStringSet(KEY_TOPICS, DEFAULT_TOPICS)?.toSet() ?: DEFAULT_TOPICS
    )
    val followedTopics: StateFlow<Set<String>> = _followedTopics

    private val _onboardingComplete = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete

    private val _readerPalette = MutableStateFlow(
        runCatching { ReaderPalette.valueOf(prefs.getString(KEY_READER_PALETTE, null) ?: "SYSTEM") }
            .getOrDefault(ReaderPalette.SYSTEM)
    )
    val readerPalette: StateFlow<ReaderPalette> = _readerPalette

    private val _readerFontScale = MutableStateFlow(prefs.getFloat(KEY_FONT_SCALE, 1f))
    val readerFontScale: StateFlow<Float> = _readerFontScale

    private val _readerLineHeight = MutableStateFlow(prefs.getFloat(KEY_LINE_HEIGHT, 1.45f))
    val readerLineHeight: StateFlow<Float> = _readerLineHeight

    private val _recentSearches = MutableStateFlow(loadRecentSearches())
    val recentSearches: StateFlow<List<String>> = _recentSearches

    fun toggleTopic(topic: String) {
        val updated = _followedTopics.value.toMutableSet().apply {
            if (!add(topic)) remove(topic)
        }.ifEmpty { DEFAULT_TOPICS }
        _followedTopics.value = updated
        prefs.edit().putStringSet(KEY_TOPICS, updated).apply()
    }

    fun completeOnboarding() {
        _onboardingComplete.value = true
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    fun setReaderPalette(palette: ReaderPalette) {
        _readerPalette.value = palette
        prefs.edit().putString(KEY_READER_PALETTE, palette.name).apply()
    }

    fun setReaderFontScale(scale: Float) {
        val safe = scale.coerceIn(0.85f, 1.35f)
        _readerFontScale.value = safe
        prefs.edit().putFloat(KEY_FONT_SCALE, safe).apply()
    }

    fun setReaderLineHeight(multiplier: Float) {
        val safe = multiplier.coerceIn(1.2f, 1.8f)
        _readerLineHeight.value = safe
        prefs.edit().putFloat(KEY_LINE_HEIGHT, safe).apply()
    }

    fun rememberSearch(query: String) {
        val cleaned = query.trim()
        if (cleaned.length < 2) return
        val updated = (listOf(cleaned) + _recentSearches.value)
            .distinctBy { it.lowercase() }
            .take(6)
        _recentSearches.value = updated
        prefs.edit().putString(KEY_RECENT_SEARCHES, updated.joinToString(SEPARATOR)).apply()
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }

    fun followedTopicsSnapshot(): Set<String> = _followedTopics.value

    private fun loadRecentSearches(): List<String> = prefs.getString(KEY_RECENT_SEARCHES, null)
        ?.split(SEPARATOR)
        ?.filter { it.isNotBlank() }
        .orEmpty()

    companion object {
        val AVAILABLE_TOPICS = listOf(
            "Technology", "Business", "World", "Health", "Sports", "Entertainment"
        )
        private val DEFAULT_TOPICS = setOf("Technology", "World", "Business")
        private const val PREFS_NAME = "newsynk_personalization"
        private const val KEY_TOPICS = "followed_topics"
        private const val KEY_ONBOARDING = "personalization_onboarding_complete"
        private const val KEY_READER_PALETTE = "reader_palette"
        private const val KEY_FONT_SCALE = "reader_font_scale"
        private const val KEY_LINE_HEIGHT = "reader_line_height"
        private const val KEY_RECENT_SEARCHES = "recent_searches"
        private const val SEPARATOR = "\u001F"
    }
}
