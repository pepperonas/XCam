package io.celox.xcam.data.model

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VideoFileTest {
    private fun video(
        size: Long = 0,
        duration: Long = 0,
    ) = VideoFile(1, Uri.parse("content://media/external/video/media/1"), "a.mp4", size, duration, 0L)

    @Test
    fun `sizeInMB uses binary megabytes`() {
        assertEquals(1.0f, video(size = 1024L * 1024L).sizeInMB, 0.001f)
        assertEquals(500.0f, video(size = 1024L * 1024L * 500).sizeInMB, 0.001f)
        assertEquals(0f, video(size = 0).sizeInMB, 0.001f)
    }

    @Test
    fun `formattedDuration delegates to the clip format`() {
        assertEquals("2:00", video(duration = 120_000).formattedDuration)
        assertEquals("", video(duration = 0).formattedDuration)
    }
}
