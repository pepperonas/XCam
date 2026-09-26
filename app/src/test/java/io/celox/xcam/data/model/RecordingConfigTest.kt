package io.celox.xcam.data.model

import androidx.camera.core.CameraSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingConfigTest {
    @Test
    fun `default config records 1080p with audio from the back camera, unlimited`() {
        val config = RecordingConfig()
        assertEquals(CameraSelector.LENS_FACING_BACK, config.cameraLens)
        assertEquals(VideoQuality.HD_1080P, config.videoQuality)
        assertTrue(config.enableAudio)
        assertEquals(0, config.maxDurationMinutes)
    }

    @Test
    fun `maxDurationMillis converts minutes`() {
        assertEquals(0L, RecordingConfig(maxDurationMinutes = 0).maxDurationMillis)
        assertEquals(15 * 60_000L, RecordingConfig(maxDurationMinutes = 15).maxDurationMillis)
    }

    @Test
    fun `duration options start with unlimited and ascend`() {
        val options = RecordingConfig.MAX_DURATION_OPTIONS
        assertEquals(0, options.first())
        assertEquals(options.sorted(), options)
        assertEquals(options.size, options.toSet().size)
    }

    @Test
    fun `the default duration is one of the offered options`() {
        assertTrue(RecordingConfig().maxDurationMinutes in RecordingConfig.MAX_DURATION_OPTIONS)
    }
}
