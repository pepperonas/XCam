package io.celox.xcam.data

import androidx.camera.core.CameraSelector
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.ThemeMode
import io.celox.xcam.data.model.VideoQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigPrefsTest {
    private fun decodeAllNull() = ConfigPrefs.decode(null, null, null, null, null, null, null)

    @Test
    fun `a fresh install gets the defaults`() {
        assertEquals(AppSettings(), decodeAllNull())
    }

    @Test
    fun `stored values come back as they were saved`() {
        val settings =
            ConfigPrefs.decode(
                onboarding = true,
                lens = CameraSelector.LENS_FACING_FRONT,
                quality = VideoQuality.UHD_4K.name,
                audio = false,
                maxMinutes = 30,
                theme = ThemeMode.DARK.name,
                dynamic = true,
            )
        assertEquals(
            AppSettings(
                onboardingCompleted = true,
                recording = RecordingConfig(CameraSelector.LENS_FACING_FRONT, VideoQuality.UHD_4K, false, 30),
                themeMode = ThemeMode.DARK,
                dynamicColor = true,
            ),
            settings,
        )
    }

    @Test
    fun `unknown or out-of-range values fall back instead of crashing`() {
        val settings =
            ConfigPrefs.decode(
                onboarding = null,
                lens = 42,
                quality = "HD_8K",
                audio = null,
                maxMinutes = 7,
                theme = "SEPIA",
                dynamic = null,
            )
        assertEquals(decodeAllNull(), settings)
    }
}
