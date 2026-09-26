package io.celox.xcam.data.model

import android.net.Uri
import io.celox.xcam.util.formatDuration

/** One recording, as MediaStore knows it. [uri] is the only handle needed to play, share or delete it. */
data class VideoFile(
    val id: Long,
    val uri: Uri,
    val name: String,
    val size: Long,
    /** Milliseconds; 0 when MediaStore has not indexed it yet. */
    val duration: Long = 0,
    /** Wall-clock millis the recording was taken. */
    val timestamp: Long,
) {
    val sizeInMB: Float
        get() = size / (1024f * 1024f)

    val formattedDuration: String
        get() = formatDuration(duration)
}
