package io.celox.xcam.data

import androidx.camera.core.CameraSelector
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.ThemeMode
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.data.update.AppUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Every stored value the Settings screen can produce must decode back to itself. */
class ConfigPrefsExhaustiveTest {
    private fun decode(
        lens: Int? = null,
        quality: String? = null,
        maxMinutes: Int? = null,
        theme: String? = null,
        knownVersion: String? = null,
        knownNotes: String? = null,
    ) = ConfigPrefs.decode(null, lens, quality, null, maxMinutes, theme, null, null, knownVersion, knownNotes)

    @Test
    fun `every video quality round-trips by name`() {
        VideoQuality.entries.forEach { assertEquals(it, decode(quality = it.name).recording.videoQuality) }
    }

    @Test
    fun `quality names are case sensitive, lower case falls back`() {
        assertEquals(VideoQuality.HD_1080P, decode(quality = "uhd_4k").recording.videoQuality)
    }

    @Test
    fun `both camera lenses round-trip`() {
        listOf(CameraSelector.LENS_FACING_BACK, CameraSelector.LENS_FACING_FRONT).forEach {
            assertEquals(it, decode(lens = it).recording.cameraLens)
        }
    }

    @Test
    fun `the both-cameras choice round-trips`() {
        assertEquals(io.celox.xcam.data.model.CameraLens.DUAL_SELECTOR, decode(lens = io.celox.xcam.data.model.CameraLens.DUAL_SELECTOR).recording.cameraLens)
    }

    @Test
    fun `an external or unknown lens falls back to the back camera`() {
        listOf(CameraSelector.LENS_FACING_EXTERNAL, -1, Int.MAX_VALUE).forEach {
            assertEquals(CameraSelector.LENS_FACING_BACK, decode(lens = it).recording.cameraLens)
        }
    }

    @Test
    fun `every offered duration round-trips, anything else falls back to unlimited`() {
        RecordingConfig.MAX_DURATION_OPTIONS.forEach { assertEquals(it, decode(maxMinutes = it).recording.maxDurationMinutes) }
        listOf(-5, 1, 10, 45, 61, 1440).forEach { assertEquals(0, decode(maxMinutes = it).recording.maxDurationMinutes) }
    }

    @Test
    fun `every theme mode round-trips by name`() {
        ThemeMode.entries.forEach { assertEquals(it, decode(theme = it.name).themeMode) }
        assertEquals(ThemeMode.SYSTEM, decode(theme = "dark").themeMode)
    }

    @Test
    fun `a known release needs both a version and an https link`() {
        assertNull(decode(knownVersion = "v3.1.0").knownUpdate)
        assertNull(decode(knownNotes = "https://x-cam.celox.io").knownUpdate)
        assertNull(decode(knownVersion = "  ", knownNotes = "https://x-cam.celox.io").knownUpdate)
        assertNull(decode(knownVersion = "v3.1.0", knownNotes = "http://x-cam.celox.io").knownUpdate)
        assertEquals(
            AppUpdate("v3.1.0", "https://x-cam.celox.io"),
            decode(knownVersion = "v3.1.0", knownNotes = "https://x-cam.celox.io").knownUpdate,
        )
    }

    @Test
    fun `explicit false values are kept, not replaced by defaults`() {
        val s = ConfigPrefs.decode(false, null, null, false, null, null, false, false)
        assertEquals(false, s.onboardingCompleted)
        assertEquals(false, s.recording.enableAudio)
        assertEquals(false, s.dynamicColor)
        assertEquals(false, s.updateChecks)
    }
}
