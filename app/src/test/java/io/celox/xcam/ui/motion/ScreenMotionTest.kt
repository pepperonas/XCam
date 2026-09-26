package io.celox.xcam.ui.motion

import io.celox.xcam.ui.navigation.Destination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenMotionTest {
    @Test
    fun `moving right in the bar is forward`() {
        assertTrue(ScreenMotion.isForward(0, 2))
        assertFalse(ScreenMotion.isForward(2, 0))
    }

    @Test
    fun `routes outside the bar count as forward`() {
        assertTrue(ScreenMotion.isForward(-1, 0))
        assertTrue(ScreenMotion.isForward(1, -1))
    }

    @Test
    fun `slide offsets are a small fraction, signed`() {
        assertEquals(60, ScreenMotion.slideOffset(1000, ScreenMotion.TAB_SLIDE_FRACTION, 1))
        assertEquals(-60, ScreenMotion.slideOffset(1000, ScreenMotion.TAB_SLIDE_FRACTION, -1))
    }

    @Test
    fun `tab order is record, videos, settings and player is not a tab`() {
        assertEquals(listOf("record", "videos", "settings"), Destination.entries.map { it.route })
        assertEquals(-1, Destination.tabIndexOf("player/{id}"))
    }
}
