package io.celox.xcam.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TimeFormatTest {
    @Test
    fun `clip durations drop the leading zero minute`() {
        assertEquals("0:30", formatDuration(30_000))
        assertEquals("2:00", formatDuration(120_000))
        assertEquals("10:05", formatDuration(605_000))
        assertEquals("1:30:00", formatDuration(5_400_000))
    }

    @Test
    fun `unknown clip duration is empty`() {
        assertEquals("", formatDuration(0))
        assertEquals("", formatDuration(-5))
    }

    @Test
    fun `the running timer is zero-padded and never negative`() {
        assertEquals("00:00", formatElapsed(0))
        assertEquals("00:00", formatElapsed(-1000))
        assertEquals("00:59", formatElapsed(59_999))
        assertEquals("10:00", formatElapsed(600_000))
        assertEquals("1:00:01", formatElapsed(3_601_000))
    }

    @Test
    fun `timers use latin digits whatever the default locale`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))
            assertEquals("01:05", formatElapsed(65_000))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
