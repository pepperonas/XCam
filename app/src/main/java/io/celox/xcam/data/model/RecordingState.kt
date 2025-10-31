package io.celox.xcam.data.model

sealed class RecordingState {
    object Idle : RecordingState()
    object Starting : RecordingState()
    data class Recording(
        val startTime: Long,
        val outputPath: String
    ) : RecordingState()
    object Stopping : RecordingState()
    data class Error(val message: String) : RecordingState()
}
