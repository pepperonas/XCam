package io.celox.xcam.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingStateTest {
    @Test
    fun `starting, recording and stopping are busy`() {
        assertTrue(RecordingState.Starting.isBusy)
        assertTrue(RecordingState.Recording(1L).isBusy)
        assertTrue(RecordingState.Stopping.isBusy)
    }

    @Test
    fun `idle and error are not busy, so a new recording can start`() {
        assertFalse(RecordingState.Idle.isBusy)
        assertFalse(RecordingState.Error(RecordingState.Reason.RECORDING_FAILED).isBusy)
    }

    @Test
    fun `recording carries the real start time and the limit`() {
        val state = RecordingState.Recording(startTime = 1234L, maxDurationMillis = 60_000L)
        assertEquals(1234L, state.startTime)
        assertEquals(60_000L, state.maxDurationMillis)
    }
}
