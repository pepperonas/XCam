package io.celox.xcam.data

import io.celox.xcam.data.model.RecordingState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecordingRepositoryTest {
    private val repo = RecordingRepository

    // The repository is a process singleton: every test starts and ends idle.
    @Before
    @After
    fun reset() {
        repo.onServiceDestroyed()
        repo.clearError()
    }

    @Test
    fun `a full recording goes starting, recording, stopping, idle`() {
        assertTrue(repo.onStartRequested())
        assertEquals(RecordingState.Starting, repo.state.value)
        repo.onRecordingStarted(startTime = 100L, maxDurationMillis = 0L)
        assertEquals(RecordingState.Recording(100L, 0L), repo.state.value)
        repo.onStopRequested()
        assertEquals(RecordingState.Stopping, repo.state.value)
        repo.onFinalized(null)
        assertEquals(RecordingState.Idle, repo.state.value)
    }

    @Test
    fun `a second start while busy is refused`() {
        assertTrue(repo.onStartRequested())
        assertFalse(repo.onStartRequested())
        repo.onRecordingStarted(1L, 0L)
        assertFalse(repo.onStartRequested())
        assertEquals(RecordingState.Recording(1L, 0L), repo.state.value)
    }

    @Test
    fun `a failed finalize ends in the error, and every finalize is counted`() {
        val before = repo.finalized.value
        repo.onStartRequested()
        repo.onRecordingStarted(1L, 0L)
        val error = RecordingState.Error(RecordingState.Reason.NO_SPACE)
        repo.onFinalized(error)
        assertEquals(error, repo.state.value)
        assertEquals(before + 1, repo.finalized.value)
    }

    @Test
    fun `after an error a new recording may start`() {
        repo.onError(RecordingState.Error(RecordingState.Reason.CAMERA_UNAVAILABLE))
        assertTrue(repo.onStartRequested())
    }

    @Test
    fun `stop from idle changes nothing`() {
        repo.onStopRequested()
        assertEquals(RecordingState.Idle, repo.state.value)
    }

    @Test
    fun `a destroyed service settles a busy state to idle but keeps an error visible`() {
        repo.onStartRequested()
        repo.onServiceDestroyed()
        assertEquals(RecordingState.Idle, repo.state.value)

        val error = RecordingState.Error(RecordingState.Reason.RECORDING_FAILED)
        repo.onError(error)
        repo.onServiceDestroyed()
        assertEquals(error, repo.state.value)
    }
}
