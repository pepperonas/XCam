package io.celox.xcam.ui

/** What the user has allowed. Refreshed by MainActivity on every resume. */
data class Permissions(
    val camera: Boolean = false,
    val audio: Boolean = false,
    val notifications: Boolean = false,
    /** READ_MEDIA_VIDEO (or a partial selection on Android 14+): also lists recordings of an earlier install. */
    val media: Boolean = false,
)

/** The permission requests the UI can trigger; implemented by MainActivity's launchers. */
class PermissionActions(
    val requestCamera: () -> Unit,
    val requestAudioAndNotifications: () -> Unit,
    val requestMedia: () -> Unit,
    /** For permanently denied permissions: the system dialog no longer appears. */
    val openAppSettings: () -> Unit,
)
