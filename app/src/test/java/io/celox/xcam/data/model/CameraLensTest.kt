package io.celox.xcam.data.model

import androidx.camera.core.CameraSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraLensTest {
    @Test
    fun `each lens maps to its CameraX selector`() {
        assertEquals(CameraSelector.LENS_FACING_BACK, CameraLens.BACK.selector)
        assertEquals(CameraSelector.LENS_FACING_FRONT, CameraLens.FRONT.selector)
    }

    @Test
    fun `fromSelector round-trips every lens`() {
        CameraLens.entries.forEach { assertEquals(it, CameraLens.fromSelector(it.selector)) }
    }

    @Test
    fun `an unknown selector falls back to the back camera`() {
        assertEquals(CameraLens.BACK, CameraLens.fromSelector(CameraSelector.LENS_FACING_EXTERNAL))
        assertEquals(CameraLens.BACK, CameraLens.fromSelector(-42))
    }

    @Test
    fun `lens labels are distinct resources`() {
        assertNotEquals(CameraLens.BACK.labelRes, CameraLens.FRONT.labelRes)
    }

    @Test
    fun `the default recording lens is one of the offered lenses`() {
        assertTrue(CameraLens.entries.any { it.selector == RecordingConfig().cameraLens })
    }

    @Test
    fun `every quality is 16 to 9 and has distinct labels`() {
        VideoQuality.entries.forEach { assertEquals(16 * it.height, 9 * it.width) }
        assertEquals(VideoQuality.entries.size, VideoQuality.entries.map { it.labelRes }.toSet().size)
        assertEquals(VideoQuality.entries.size, VideoQuality.entries.map { it.shortLabelRes }.toSet().size)
    }

    @Test
    fun `theme modes are system, light and dark, system first as the default`() {
        assertEquals(listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK), ThemeMode.entries)
    }
}
