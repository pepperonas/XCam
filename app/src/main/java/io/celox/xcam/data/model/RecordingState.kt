package io.celox.xcam.data.model

sealed class RecordingState {
    data object Idle : RecordingState()

    data object Starting : RecordingState()

    /** [startTime] is wall-clock millis of CameraX's `VideoRecordEvent.Start`, not of the tap. */
    data class Recording(val startTime: Long, val maxDurationMillis: Long = 0L) : RecordingState()

    data object Stopping : RecordingState()

    data class Error(val reason: Reason, val detail: String? = null) : RecordingState()

    enum class Reason { CAMERA_UNAVAILABLE, RECORDING_FAILED, NO_SPACE }

    val isBusy: Boolean get() = this is Starting || this is Recording || this is Stopping
}
