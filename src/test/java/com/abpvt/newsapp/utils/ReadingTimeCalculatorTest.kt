package com.abpvt.newsapp.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingTimeCalculatorTest {

    @Test
    fun `empty content has a one minute minimum`() {
        assertEquals(1, ReadingTimeCalculator.calculateMinutes("", null, null))
    }

    @Test
    fun `short article is a one minute read`() {
        val title = List(50) { "word" }.joinToString(" ")
        assertEquals(1, ReadingTimeCalculator.calculateMinutes(title, null, null))
    }

    @Test
    fun `article over fifty words receives a useful two minute minimum`() {
        val content = List(51) { "word" }.joinToString(" ")
        assertEquals(2, ReadingTimeCalculator.calculateMinutes("", null, content))
    }

    @Test
    fun `long article scales by word count`() {
        val content = List(600) { "word" }.joinToString(" ")
        assertEquals(3, ReadingTimeCalculator.calculateMinutes("", null, content))
    }

    @Test
    fun `all supplied fields contribute to the estimate`() {
        val words = List(25) { "word" }.joinToString(" ")
        assertEquals(2, ReadingTimeCalculator.calculateMinutes(words, words, words))
    }
}
