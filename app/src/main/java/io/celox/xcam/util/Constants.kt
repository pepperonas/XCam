package io.celox.xcam.util

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "recording_channel"
    const val NOTIFICATION_ID = 1001

    const val ACTION_START_RECORDING = "io.celox.xcam.ACTION_START_RECORDING"
    const val ACTION_STOP_RECORDING = "io.celox.xcam.ACTION_STOP_RECORDING"

    const val EXTRA_CAMERA_LENS = "camera_lens"
    const val EXTRA_VIDEO_QUALITY = "video_quality"
    const val EXTRA_ENABLE_AUDIO = "enable_audio"
    const val EXTRA_MAX_DURATION_MS = "max_duration_ms"

    const val VIDEO_DIRECTORY = "XCam"

    /** MediaStore RELATIVE_PATH of the recordings (MediaStore stores it with a trailing slash). */
    const val RELATIVE_VIDEO_PATH = "Movies/$VIDEO_DIRECTORY/"
    const val VIDEO_MIME_TYPE = "video/mp4"

    const val PREFERENCES_NAME = "xcam_preferences"

    // Wake Lock
    const val WAKE_LOCK_TAG = "XCam::RecordingWakeLock"
}
