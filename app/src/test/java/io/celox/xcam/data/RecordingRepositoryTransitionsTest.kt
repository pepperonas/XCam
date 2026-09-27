package io.celox.xcam.data

import io.celox.xcam.data.model.RecordingState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Transitions beyond the happy path: what the service can report in unusual orders. */
class RecordingRepositoryTransitionsTest {
    private val repo = RecordingRepository

    @Before
    @After
    fun reset() {
        repo.onServiceDestroyed()
        repo.clearError()
    }

    @Test
    fun `a stop while the camera is still starting goes to stopping`() {
        repo.onStartRequested()
        repo.onStopRequested()
        assertEquals(RecordingState.Stopping, repo.state.value)
    }

    @Test
    fun `stop requested twice stays stopping`() {
        repo.onStartRequested()
        repo.onRecordingStarted(1L, 0L)
        repo.onStopRequested()
        repo.onStopRequested()
        assertEquals(RecordingState.Stopping, repo.state.value)
    }

    @Test
    fun `stop does not hide an error`() {
        val error = RecordingState.Error(RecordingState.Reason.RECORDING_FAILED)
        repo.onError(error)
        repo.onStopRequested()
        assertEquals(error, repo.state.value)
    }

    @Test
    fun `clearError leaves every non-error state alone`() {
        repo.onStartRequested()
        repo.clearError()
        assertEquals(RecordingState.Starting, repo.state.value)
        repo.onRecordingStarted(5L, 60_000L)
        repo.clearError()
        assertEquals(RecordingState.Recording(5L, 60_000L), repo.state.value)
    }

    @Test
    fun `an error while recording replaces the recording state`() {
        repo.onStartRequested()
        repo.onRecordingStarted(1L, 0L)
        val error = RecordingState.Error(RecordingState.Reason.CAMERA_UNAVAILABLE, "gone")
        repo.onError(error)
        assertEquals(error, repo.state.value)
        assertFalse(repo.state.value.isBusy)
    }

    @Test
    fun `the limit and start time reported by the service are kept exactly`() {
        repo.onStartRequested()
        repo.onRecordingStarted(startTime = 1_700_000_000_000L, maxDurationMillis = 5 * 60_000L)
        val state = repo.state.value as RecordingState.Recording
        assertEquals(1_700_000_000_000L, state.startTime)
        assertEquals(300_000L, state.maxDurationMillis)
    }

    @Test
    fun `the finalized counter only ever grows, one per finalize`() {
        val start = repo.finalized.value
        repeat(3) {
            repo.onStartRequested()
            repo.onRecordingStarted(it.toLong(), 0L)
            repo.onFinalized(null)
        }
        assertEquals(start + 3, repo.finalized.value)
    }

    @Test
    fun `a finalize without a stop request still ends idle`() {
        // Duration limit reached: CameraX finalizes on its own, there was no stop request.
        repo.onStartRequested()
        repo.onRecordingStarted(1L, 60_000L)
        repo.onFinalized(null)
        assertEquals(RecordingState.Idle, repo.state.value)
        assertTrue(repo.onStartRequested())
    }

    @Test
    fun `a dismissed error lets the next start through`() {
        repo.onError(RecordingState.Error(RecordingState.Reason.NO_SPACE))
        repo.clearError()
        assertEquals(RecordingState.Idle, repo.state.value)
        assertTrue(repo.onStartRequested())
    }
}
