package io.celox.xcam.data.model

import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import io.celox.xcam.R

data class RecordingConfig(
    val cameraLens: Int = CameraSelector.LENS_FACING_BACK,
    val videoQuality: VideoQuality = VideoQuality.HD_1080P,
    val enableAudio: Boolean = true,
    /** 0 = unlimited. */
    val maxDurationMinutes: Int = 0,
) {
    val maxDurationMillis: Long get() = maxDurationMinutes * 60_000L

    companion object {
        /** The choices offered in Settings; 0 = unlimited. */
        val MAX_DURATION_OPTIONS = listOf(0, 5, 15, 30, 60)
    }
}

enum class VideoQuality(
    @StringRes val labelRes: Int,
    @StringRes val shortLabelRes: Int,
    val width: Int,
    val height: Int,
) {
    HD_720P(R.string.quality_720_long, R.string.quality_720, 1280, 720),
    HD_1080P(R.string.quality_1080_long, R.string.quality_1080, 1920, 1080),
    UHD_4K(R.string.quality_4k_long, R.string.quality_4k, 3840, 2160),
    ;

    companion object {
        /** Tolerant lookup for persisted/intent values: unknown names fall back to 1080p. */
        fun fromName(name: String?): VideoQuality = entries.firstOrNull { it.name == name } ?: HD_1080P
    }
}

enum class CameraLens(@StringRes val labelRes: Int, val selector: Int) {
    BACK(R.string.lens_back, CameraSelector.LENS_FACING_BACK),
    FRONT(R.string.lens_front, CameraSelector.LENS_FACING_FRONT),
    ;

    companion object {
        fun fromSelector(selector: Int): CameraLens = entries.firstOrNull { it.selector == selector } ?: BACK
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }
