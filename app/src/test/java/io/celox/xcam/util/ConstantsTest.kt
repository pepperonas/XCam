package io.celox.xcam.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstantsTest {
    @Test
    fun `actions are namespaced and unique`() {
        val actions = listOf(Constants.ACTION_START_RECORDING, Constants.ACTION_STOP_RECORDING)
        assertTrue(actions.all { it.startsWith("io.celox.xcam.") })
        assertEquals(actions.size, actions.toSet().size)
    }

    @Test
    fun `extras are unique`() {
        val extras =
            listOf(
                Constants.EXTRA_CAMERA_LENS,
                Constants.EXTRA_VIDEO_QUALITY,
                Constants.EXTRA_ENABLE_AUDIO,
                Constants.EXTRA_MAX_DURATION_MS,
            )
        assertEquals(extras.size, extras.toSet().size)
    }

    @Test
    fun `recordings live in Movies XCam, with the trailing slash MediaStore stores`() {
        // The video list queries RELATIVE_PATH LIKE '<path>%'; without the slash, "Movies/XCamera/"
        // of another app would match too.
        assertEquals("Movies/XCam/", Constants.RELATIVE_VIDEO_PATH)
        assertTrue(Constants.RELATIVE_VIDEO_PATH.endsWith("/"))
    }

    @Test
    fun `notification id is positive`() {
        assertTrue(Constants.NOTIFICATION_ID > 0)
    }
}
