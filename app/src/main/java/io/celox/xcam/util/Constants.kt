package io.celox.xcam.util

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "recording_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Recording Service"
    const val NOTIFICATION_ID = 1001

    const val ACTION_START_RECORDING = "io.celox.xcam.ACTION_START_RECORDING"
    const val ACTION_STOP_RECORDING = "io.celox.xcam.ACTION_STOP_RECORDING"
    const val ACTION_PAUSE_RECORDING = "io.celox.xcam.ACTION_PAUSE_RECORDING"

    const val EXTRA_CAMERA_LENS = "camera_lens"
    const val EXTRA_VIDEO_QUALITY = "video_quality"
    const val EXTRA_ENABLE_AUDIO = "enable_audio"

    const val VIDEO_DIRECTORY = "XCam"
    const val VIDEO_FILE_PREFIX = "VID_"
    const val VIDEO_FILE_EXTENSION = ".mp4"

    const val PREFERENCES_NAME = "xcam_preferences"

    // Wake Lock
    const val WAKE_LOCK_TAG = "XCam::RecordingWakeLock"
}
