package io.celox.xcam.data.model

import org.junit.Assert.*
import org.junit.Test

class RecordingStateTest {

    @Test
    fun `Idle is singleton instance`() {
        val state1 = RecordingState.Idle
        val state2 = RecordingState.Idle
        assertSame(state1, state2)
    }

    @Test
    fun `Starting is singleton instance`() {
        val state1 = RecordingState.Starting
        val state2 = RecordingState.Starting
        assertSame(state1, state2)
    }

    @Test
    fun `Stopping is singleton instance`() {
        val state1 = RecordingState.Stopping
        val state2 = RecordingState.Stopping
        assertSame(state1, state2)
    }

    @Test
    fun `Recording holds startTime and outputPath`() {
        val startTime = System.currentTimeMillis()
        val path = "/storage/emulated/0/Movies/XCam/VID_test.mp4"
        val state = RecordingState.Recording(startTime, path)
        assertEquals(startTime, state.startTime)
        assertEquals(path, state.outputPath)
    }

    @Test
    fun `Error holds message`() {
        val error = RecordingState.Error("Camera initialization failed")
        assertEquals("Camera initialization failed", error.message)
    }

    @Test
    fun `states are distinguishable via is-check`() {
        val states: List<RecordingState> = listOf(
            RecordingState.Idle,
            RecordingState.Starting,
            RecordingState.Recording(0L, ""),
            RecordingState.Stopping,
            RecordingState.Error("err")
        )

        assertTrue(states[0] is RecordingState.Idle)
        assertTrue(states[1] is RecordingState.Starting)
        assertTrue(states[2] is RecordingState.Recording)
        assertTrue(states[3] is RecordingState.Stopping)
        assertTrue(states[4] is RecordingState.Error)
    }

    @Test
    fun `Recording equality based on data`() {
        val r1 = RecordingState.Recording(1000L, "/path/a.mp4")
        val r2 = RecordingState.Recording(1000L, "/path/a.mp4")
        val r3 = RecordingState.Recording(2000L, "/path/b.mp4")
        assertEquals(r1, r2)
        assertNotEquals(r1, r3)
    }

    @Test
    fun `Error equality based on message`() {
        val e1 = RecordingState.Error("timeout")
        val e2 = RecordingState.Error("timeout")
        val e3 = RecordingState.Error("permission denied")
        assertEquals(e1, e2)
        assertNotEquals(e1, e3)
    }
}
