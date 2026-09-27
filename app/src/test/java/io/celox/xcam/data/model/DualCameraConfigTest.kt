package io.celox.xcam.data.model

import androidx.camera.core.CameraSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DualCameraConfigTest {
    private val dual = RecordingConfig(cameraLens = CameraLens.DUAL_SELECTOR)

    @Test
    fun `the dual value collides with no CameraX lens constant`() {
        val cameraX =
            listOf(
                CameraSelector.LENS_FACING_UNKNOWN,
                CameraSelector.LENS_FACING_FRONT,
                CameraSelector.LENS_FACING_BACK,
                CameraSelector.LENS_FACING_EXTERNAL,
            )
        assertFalse(CameraLens.DUAL_SELECTOR in cameraX)
        assertEquals(CameraLens.BOTH, CameraLens.fromSelector(CameraLens.DUAL_SELECTOR))
    }

    @Test
    fun `both is offered only where the device supports it, and last`() {
        assertEquals(listOf(CameraLens.BACK, CameraLens.FRONT), CameraLens.options(dualSupported = false))
        assertEquals(listOf(CameraLens.BACK, CameraLens.FRONT, CameraLens.BOTH), CameraLens.options(dualSupported = true))
    }

    @Test
    fun `only the dual value is dual`() {
        assertTrue(dual.isDual)
        assertFalse(RecordingConfig().isDual)
        assertFalse(RecordingConfig(cameraLens = CameraSelector.LENS_FACING_FRONT).isDual)
    }

    @Test
    fun `a dual recording is capped at 720p, whatever was chosen`() {
        VideoQuality.entries.forEach {
            assertEquals(VideoQuality.HD_720P, dual.copy(videoQuality = it).effectiveQuality)
        }
    }

    @Test
    fun `a single-camera recording keeps its quality`() {
        VideoQuality.entries.forEach {
            assertEquals(it, RecordingConfig(videoQuality = it).effectiveQuality)
            assertEquals(it, RecordingConfig(cameraLens = CameraSelector.LENS_FACING_FRONT, videoQuality = it).effectiveQuality)
        }
    }

    @Test
    fun `the chosen quality is kept in the config, so switching back to one camera restores it`() {
        val config = dual.copy(videoQuality = VideoQuality.UHD_4K)
        assertEquals(VideoQuality.UHD_4K, config.videoQuality)
        assertEquals(VideoQuality.UHD_4K, config.copy(cameraLens = CameraSelector.LENS_FACING_BACK).effectiveQuality)
    }

    @Test
    fun `a saved dual choice on an unsupporting device falls back to the back camera`() {
        val resolved = dual.copy(videoQuality = VideoQuality.UHD_4K, maxDurationMinutes = 15).resolvedFor(dualSupported = false)
        assertEquals(CameraSelector.LENS_FACING_BACK, resolved.cameraLens)
        assertEquals(VideoQuality.UHD_4K, resolved.effectiveQuality)
        assertEquals(15, resolved.maxDurationMinutes)
    }

    @Test
    fun `resolving changes nothing where it is not needed`() {
        assertEquals(dual, dual.resolvedFor(dualSupported = true))
        val front = RecordingConfig(cameraLens = CameraSelector.LENS_FACING_FRONT)
        assertEquals(front, front.resolvedFor(dualSupported = false))
        assertEquals(front, front.resolvedFor(dualSupported = true))
    }
}
