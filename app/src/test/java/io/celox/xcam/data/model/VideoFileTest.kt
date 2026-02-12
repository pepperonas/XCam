package io.celox.xcam.data.model

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VideoFileTest {

    private fun createVideoFile(
        name: String = "test.mp4",
        size: Long = 1024L * 1024L * 50, // 50 MB
        duration: Long = 120_000L, // 2 minutes
        timestamp: Long = System.currentTimeMillis()
    ) = VideoFile(
        file = File("/tmp/$name"),
        name = name,
        size = size,
        duration = duration,
        timestamp = timestamp,
        thumbnailPath = "/tmp/$name"
    )

    @Test
    fun `sizeInMB calculates correctly for various sizes`() {
        val file1MB = createVideoFile(size = 1024L * 1024L)
        assertEquals(1.0f, file1MB.sizeInMB, 0.01f)

        val file500MB = createVideoFile(size = 1024L * 1024L * 500)
        assertEquals(500.0f, file500MB.sizeInMB, 0.01f)

        val fileZero = createVideoFile(size = 0L)
        assertEquals(0.0f, fileZero.sizeInMB, 0.01f)
    }

    @Test
    fun `formattedDuration formats minutes and seconds correctly`() {
        // 2:00
        val twoMinutes = createVideoFile(duration = 120_000L)
        assertEquals("2:00", twoMinutes.formattedDuration)

        // 0:30
        val thirtySeconds = createVideoFile(duration = 30_000L)
        assertEquals("0:30", thirtySeconds.formattedDuration)

        // 10:05
        val tenMinutes = createVideoFile(duration = 605_000L)
        assertEquals("10:05", tenMinutes.formattedDuration)
    }

    @Test
    fun `formattedDuration formats hours correctly`() {
        // 1:30:00
        val ninetyMinutes = createVideoFile(duration = 5_400_000L)
        assertEquals("1:30:00", ninetyMinutes.formattedDuration)

        // 2:05:30
        val twoHoursFiveMinutes = createVideoFile(duration = 7_530_000L)
        assertEquals("2:05:30", twoHoursFiveMinutes.formattedDuration)
    }

    @Test
    fun `formattedDuration returns empty for zero or negative duration`() {
        val zeroDuration = createVideoFile(duration = 0L)
        assertEquals("", zeroDuration.formattedDuration)

        val negativeDuration = createVideoFile(duration = -1000L)
        assertEquals("", negativeDuration.formattedDuration)
    }

    @Test
    fun `data class equality works correctly`() {
        val file = File("/tmp/test.mp4")
        val video1 = VideoFile(file, "test.mp4", 1000L, 5000L, 1000L, null)
        val video2 = VideoFile(file, "test.mp4", 1000L, 5000L, 1000L, null)
        assertEquals(video1, video2)
    }

    @Test
    fun `data class copy works correctly`() {
        val original = createVideoFile(name = "original.mp4", duration = 60_000L)
        val copy = original.copy(duration = 120_000L)
        assertEquals(60_000L, original.duration)
        assertEquals(120_000L, copy.duration)
        assertEquals(original.name, copy.name)
    }

    @Test
    fun `thumbnailPath can be null`() {
        val video = VideoFile(
            file = File("/tmp/test.mp4"),
            name = "test.mp4",
            size = 1024L,
            timestamp = System.currentTimeMillis(),
            thumbnailPath = null
        )
        assertNull(video.thumbnailPath)
    }
}
