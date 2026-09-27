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

    /** Front and back camera at once, composed into one video (back full frame, front inset). */
    val isDual: Boolean get() = cameraLens == CameraLens.DUAL_SELECTOR

    /**
     * The quality actually requested from CameraX. Two cameras at once are only guaranteed up to
     * 720p per camera, so a dual recording is capped there instead of failing to bind.
     */
    val effectiveQuality: VideoQuality
        get() = if (isDual && videoQuality > VideoQuality.HD_720P) VideoQuality.HD_720P else videoQuality

    /** A saved "both cameras" choice on a device that cannot do it falls back to the back camera. */
    fun resolvedFor(dualSupported: Boolean): RecordingConfig =
        if (isDual && !dualSupported) copy(cameraLens = CameraSelector.LENS_FACING_BACK) else this

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

/** Top level, not in the companion: an enum entry cannot read its own companion while initializing. */
const val DUAL_LENS_SELECTOR = 100

enum class CameraLens(@StringRes val labelRes: Int, val selector: Int) {
    BACK(R.string.lens_back, CameraSelector.LENS_FACING_BACK),
    FRONT(R.string.lens_front, CameraSelector.LENS_FACING_FRONT),
    BOTH(R.string.lens_both, DUAL_LENS_SELECTOR),
    ;

    companion object {
        /** Stored in [RecordingConfig.cameraLens] for "both cameras"; no CameraX lens constant uses it. */
        const val DUAL_SELECTOR = DUAL_LENS_SELECTOR

        fun fromSelector(selector: Int): CameraLens = entries.firstOrNull { it.selector == selector } ?: BACK

        /** The choices to offer: "both" only where the device lets apps use two cameras at once. */
        fun options(dualSupported: Boolean): List<CameraLens> = entries.filter { it != BOTH || dualSupported }
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }
