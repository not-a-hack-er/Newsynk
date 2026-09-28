package com.abpvt.newsapp.ui.news

import androidx.lifecycle.ViewModel
import com.abpvt.newsapp.data.repository.PersonalizationRepository
import com.abpvt.newsapp.data.repository.ReaderPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReaderPreferencesViewModel @Inject constructor(
    private val preferences: PersonalizationRepository
) : ViewModel() {
    val palette = preferences.readerPalette
    val fontScale = preferences.readerFontScale
    val lineHeight = preferences.readerLineHeight

    fun setPalette(value: ReaderPalette) = preferences.setReaderPalette(value)
    fun setFontScale(value: Float) = preferences.setReaderFontScale(value)
    fun setLineHeight(value: Float) = preferences.setReaderLineHeight(value)
}
