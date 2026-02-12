package io.celox.xcam.util

import org.junit.Assert.*
import org.junit.Test

class ConstantsTest {

    @Test
    fun `notification channel ID is not empty`() {
        assertTrue(Constants.NOTIFICATION_CHANNEL_ID.isNotBlank())
    }

    @Test
    fun `notification ID is positive`() {
        assertTrue(Constants.NOTIFICATION_ID > 0)
    }

    @Test
    fun `actions have correct package prefix`() {
        assertTrue(Constants.ACTION_START_RECORDING.startsWith("io.celox.xcam."))
        assertTrue(Constants.ACTION_STOP_RECORDING.startsWith("io.celox.xcam."))
        assertTrue(Constants.ACTION_PAUSE_RECORDING.startsWith("io.celox.xcam."))
    }

    @Test
    fun `actions are unique`() {
        val actions = setOf(
            Constants.ACTION_START_RECORDING,
            Constants.ACTION_STOP_RECORDING,
            Constants.ACTION_PAUSE_RECORDING
        )
        assertEquals(3, actions.size)
    }

    @Test
    fun `video directory is XCam`() {
        assertEquals("XCam", Constants.VIDEO_DIRECTORY)
    }

    @Test
    fun `video file extension is mp4`() {
        assertEquals(".mp4", Constants.VIDEO_FILE_EXTENSION)
    }

    @Test
    fun `video file prefix is VID_`() {
        assertEquals("VID_", Constants.VIDEO_FILE_PREFIX)
    }

    @Test
    fun `wake lock tag contains app name`() {
        assertTrue(Constants.WAKE_LOCK_TAG.contains("XCam"))
    }

    @Test
    fun `extras are not empty`() {
        assertTrue(Constants.EXTRA_CAMERA_LENS.isNotBlank())
        assertTrue(Constants.EXTRA_VIDEO_QUALITY.isNotBlank())
        assertTrue(Constants.EXTRA_ENABLE_AUDIO.isNotBlank())
    }
}
