package io.celox.xcam.data.model

import androidx.camera.core.CameraSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoQualityTest {
    @Test
    fun `qualities ascend by resolution`() {
        val values = VideoQuality.entries
        assertEquals(3, values.size)
        assertTrue(values.zipWithNext().all { (a, b) -> a.width < b.width && a.height < b.height })
    }

    @Test
    fun `resolutions are the standard sizes`() {
        assertEquals(1280 to 720, VideoQuality.HD_720P.width to VideoQuality.HD_720P.height)
        assertEquals(1920 to 1080, VideoQuality.HD_1080P.width to VideoQuality.HD_1080P.height)
        assertEquals(3840 to 2160, VideoQuality.UHD_4K.width to VideoQuality.UHD_4K.height)
    }

    @Test
    fun `fromName round-trips every quality`() {
        VideoQuality.entries.forEach { assertEquals(it, VideoQuality.fromName(it.name)) }
    }

    @Test
    fun `fromName falls back to 1080p for unknown or missing values`() {
        assertEquals(VideoQuality.HD_1080P, VideoQuality.fromName(null))
        assertEquals(VideoQuality.HD_1080P, VideoQuality.fromName("HD_480P"))
    }

    @Test
    fun `lens lookup maps selectors and falls back to back`() {
        assertEquals(CameraLens.FRONT, CameraLens.fromSelector(CameraSelector.LENS_FACING_FRONT))
        assertEquals(CameraLens.BACK, CameraLens.fromSelector(CameraSelector.LENS_FACING_BACK))
        assertEquals(CameraLens.BACK, CameraLens.fromSelector(99))
    }
}
