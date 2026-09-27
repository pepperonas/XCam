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

class ConstantsMoreTest {
    @Test
    fun `recordings are mp4 and the path is built from the directory name`() {
        assertEquals("video/mp4", Constants.VIDEO_MIME_TYPE)
        assertEquals("Movies/${Constants.VIDEO_DIRECTORY}/", Constants.RELATIVE_VIDEO_PATH)
    }

    @Test
    fun `the wake lock tag follows the app-prefixed convention`() {
        assertTrue(Constants.WAKE_LOCK_TAG.startsWith("XCam::"))
    }

    @Test
    fun `the preferences file keeps its original name, or settings would be lost on update`() {
        assertEquals("xcam_preferences", Constants.PREFERENCES_NAME)
    }
}
