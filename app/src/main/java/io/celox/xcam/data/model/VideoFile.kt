package io.celox.xcam.data.model

import java.io.File
import java.util.concurrent.TimeUnit

data class VideoFile(
    val file: File,
    val name: String,
    val size: Long,
    val duration: Long = 0,
    val timestamp: Long,
    val thumbnailPath: String? = null
) {
    val sizeInMB: Float
        get() = size / (1024f * 1024f)

    val formattedDuration: String
        get() {
            if (duration <= 0) return ""
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
}
