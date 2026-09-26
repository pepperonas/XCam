package io.celox.xcam.data

import io.celox.xcam.data.model.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The single source of truth for the recording state, shared by [io.celox.xcam.service.RecordingService]
 * (which writes it) and the UI (which reads it). Both live in the same process, so a process-wide
 * [StateFlow] is enough — no binding, no broadcasts.
 *
 * Only the service reports what actually happened (CameraX `Start`/`Finalize` events, bind failures);
 * the UI never guesses. That is what makes a stop from the notification, the duration limit or a
 * camera failure show up correctly on screen.
 */
object RecordingRepository {
    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    /** Counts finished recordings, so observers can refresh the video list when one lands. */
    private val _finalized = MutableStateFlow(0)
    val finalized: StateFlow<Int> = _finalized.asStateFlow()

    /** The user asked to start. Ignored while a recording is already in progress. */
    fun onStartRequested(): Boolean {
        if (_state.value.isBusy) return false
        _state.value = RecordingState.Starting
        return true
    }

    fun onRecordingStarted(startTime: Long, maxDurationMillis: Long) {
        _state.value = RecordingState.Recording(startTime, maxDurationMillis)
    }

    /** The user asked to stop; the file is being finalized. */
    fun onStopRequested() {
        if (_state.value is RecordingState.Recording || _state.value is RecordingState.Starting) {
            _state.value = RecordingState.Stopping
        }
    }

    fun onFinalized(error: RecordingState.Error?) {
        _state.value = error ?: RecordingState.Idle
        _finalized.value += 1
    }

    fun onError(error: RecordingState.Error) {
        _state.value = error
    }

    /** The service is gone. Anything still "busy" can no longer finish, so settle to idle. */
    fun onServiceDestroyed() {
        if (_state.value.isBusy) _state.value = RecordingState.Idle
    }

    /** The user dismissed an error. */
    fun clearError() {
        if (_state.value is RecordingState.Error) _state.value = RecordingState.Idle
    }
}
