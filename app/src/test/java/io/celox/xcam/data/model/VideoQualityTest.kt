package io.celox.xcam.data.model

import org.junit.Assert.*
import org.junit.Test

class VideoQualityTest {

    @Test
    fun `HD_720P has correct properties`() {
        val quality = VideoQuality.HD_720P
        assertEquals("720p HD", quality.displayName)
        assertEquals(1280, quality.width)
        assertEquals(720, quality.height)
    }

    @Test
    fun `HD_1080P has correct properties`() {
        val quality = VideoQuality.HD_1080P
        assertEquals("1080p Full HD", quality.displayName)
        assertEquals(1920, quality.width)
        assertEquals(1080, quality.height)
    }

    @Test
    fun `UHD_4K has correct properties`() {
        val quality = VideoQuality.UHD_4K
        assertEquals("4K Ultra HD", quality.displayName)
        assertEquals(3840, quality.width)
        assertEquals(2160, quality.height)
    }

    @Test
    fun `all quality levels are present`() {
        val values = VideoQuality.entries
        assertEquals(3, values.size)
    }

    @Test
    fun `quality values are ordered by resolution ascending`() {
        val values = VideoQuality.entries
        assertTrue(values[0].width < values[1].width)
        assertTrue(values[1].width < values[2].width)
        assertTrue(values[0].height < values[1].height)
        assertTrue(values[1].height < values[2].height)
    }

    @Test
    fun `valueOf returns correct enum`() {
        assertEquals(VideoQuality.HD_720P, VideoQuality.valueOf("HD_720P"))
        assertEquals(VideoQuality.HD_1080P, VideoQuality.valueOf("HD_1080P"))
        assertEquals(VideoQuality.UHD_4K, VideoQuality.valueOf("UHD_4K"))
    }
}
