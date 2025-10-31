package io.celox.xcam.data.model

import java.io.File

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
}
