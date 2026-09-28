package com.abpvt.newsapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceTest {

    @Test
    fun `success exposes data without an error`() {
        val result = Resource.Success(listOf("one", "two"))

        assertEquals(listOf("one", "two"), result.data)
        assertNull(result.message)
    }

    @Test
    fun `error preserves message and fallback data`() {
        val result = Resource.Error("Network unavailable", listOf("cached"))

        assertEquals("Network unavailable", result.message)
        assertEquals(listOf("cached"), result.data)
    }

    @Test
    fun `loading has no data or message`() {
        val result: Resource<String> = Resource.Loading()

        assertTrue(result is Resource.Loading)
        assertNull(result.data)
        assertNull(result.message)
    }
}
