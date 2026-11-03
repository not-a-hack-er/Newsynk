package com.abpvt.newsapp.data.repository

import com.abpvt.newsapp.data.model.Article
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Keep undated and old provider results out of the news feed. */
internal object ArticleFreshness {
    private val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    fun recent(articles: List<Article>, now: Long = System.currentTimeMillis()): List<Article> {
        val cutoff = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = now
            add(Calendar.MONTH, -3)
        }.timeInMillis
        return articles.filter { article ->
            val published = formats.firstNotNullOfOrNull { pattern ->
                val parser = SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val position = ParsePosition(0)
                parser.parse(article.publishedAt, position)
                    ?.takeIf { position.index == article.publishedAt.length }
                    ?.time
            }
            published != null && published in cutoff..(now + 24L * 60 * 60 * 1000)
        }
    }
}
