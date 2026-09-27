package io.celox.xcam.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatEdgeTest {
    @Test
    fun `sub-second parts are truncated, never rounded up`() {
        assertEquals("0:00", formatDuration(999))
        assertEquals("0:01", formatDuration(1_999))
        assertEquals("00:00", formatElapsed(999))
    }

    @Test
    fun `the step to one hour`() {
        assertEquals("59:59", formatDuration(3_599_999))
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("59:59", formatElapsed(3_599_999))
        assertEquals("1:00:00", formatElapsed(3_600_000))
    }

    @Test
    fun `long recordings keep hours unpadded and minutes padded`() {
        assertEquals("10:05:09", formatElapsed(10 * 3_600_000L + 5 * 60_000L + 9_000L))
        assertEquals("25:00:00", formatDuration(25 * 3_600_000L))
    }

    @Test
    fun `the running timer is always five characters below an hour`() {
        for (ms in listOf(0L, 1_000L, 9_000L, 61_000L, 599_000L, 3_599_000L)) {
            assertEquals("width of $ms", 5, formatElapsed(ms).length)
        }
    }

    @Test
    fun `a duration of one millisecond is shown, not treated as unknown`() {
        assertEquals("0:00", formatDuration(1))
    }
}
