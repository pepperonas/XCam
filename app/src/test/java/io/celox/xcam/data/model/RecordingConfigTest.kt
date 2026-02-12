package io.celox.xcam.data.model

import org.junit.Assert.*
import org.junit.Test

class RecordingConfigTest {

    @Test
    fun `default config has sensible values`() {
        val config = RecordingConfig()
        assertEquals(VideoQuality.HD_1080P, config.videoQuality)
        assertTrue(config.enableAudio)
        assertEquals(0, config.maxDurationMinutes) // unlimited
        assertTrue(config.stopAtLowBattery)
        assertEquals(10, config.lowBatteryThreshold)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = RecordingConfig()
        val modified = original.copy(videoQuality = VideoQuality.UHD_4K)
        assertEquals(VideoQuality.UHD_4K, modified.videoQuality)
        assertEquals(original.enableAudio, modified.enableAudio)
        assertEquals(original.maxDurationMinutes, modified.maxDurationMinutes)
        assertEquals(original.stopAtLowBattery, modified.stopAtLowBattery)
        assertEquals(original.lowBatteryThreshold, modified.lowBatteryThreshold)
    }

    @Test
    fun `maxDuration zero means unlimited`() {
        val config = RecordingConfig(maxDurationMinutes = 0)
        assertEquals(0, config.maxDurationMinutes)
    }

    @Test
    fun `custom config values are preserved`() {
        val config = RecordingConfig(
            videoQuality = VideoQuality.HD_720P,
            enableAudio = false,
            maxDurationMinutes = 30,
            stopAtLowBattery = false,
            lowBatteryThreshold = 20
        )
        assertEquals(VideoQuality.HD_720P, config.videoQuality)
        assertFalse(config.enableAudio)
        assertEquals(30, config.maxDurationMinutes)
        assertFalse(config.stopAtLowBattery)
        assertEquals(20, config.lowBatteryThreshold)
    }

    @Test
    fun `equality works for identical configs`() {
        val config1 = RecordingConfig(videoQuality = VideoQuality.HD_1080P, enableAudio = true)
        val config2 = RecordingConfig(videoQuality = VideoQuality.HD_1080P, enableAudio = true)
        assertEquals(config1, config2)
    }

    @Test
    fun `inequality for different configs`() {
        val config1 = RecordingConfig(enableAudio = true)
        val config2 = RecordingConfig(enableAudio = false)
        assertNotEquals(config1, config2)
    }
}
