package io.celox.xcam.data.model

import androidx.camera.core.CameraSelector

data class RecordingConfig(
    val cameraLens: Int = CameraSelector.LENS_FACING_BACK,
    val videoQuality: VideoQuality = VideoQuality.HD_1080P,
    val enableAudio: Boolean = true,
    val maxDurationMinutes: Int = 0, // 0 = unlimited
    val stopAtLowBattery: Boolean = true,
    val lowBatteryThreshold: Int = 10
)

enum class VideoQuality(val displayName: String, val width: Int, val height: Int) {
    HD_720P("720p HD", 1280, 720),
    HD_1080P("1080p Full HD", 1920, 1080),
    UHD_4K("4K Ultra HD", 3840, 2160)
}

enum class CameraLens(val displayName: String, val selector: Int) {
    BACK("Back Camera", CameraSelector.LENS_FACING_BACK),
    FRONT("Front Camera", CameraSelector.LENS_FACING_FRONT)
}
