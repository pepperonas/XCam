package io.celox.xcam.service

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.celox.xcam.data.RecordingRepository
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.RecordingState
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.receiver.RecordingActionReceiver
import io.celox.xcam.util.Constants
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The intents the UI and the notification send to the service, without running the camera. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecordingServiceIntentsTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Before
    @After
    fun reset() {
        RecordingRepository.onServiceDestroyed()
        RecordingRepository.clearError()
        shadowOf(app).clearStartedServices()
    }

    @Test
    fun `start sends the whole config to the service and marks the state starting`() {
        val config = RecordingConfig(cameraLens = 0, videoQuality = VideoQuality.UHD_4K, enableAudio = false, maxDurationMinutes = 15)
        RecordingService.startRecording(app, config)
        val intent = shadowOf(app).nextStartedService
        assertEquals(RecordingService::class.java.name, intent.component?.className)
        assertEquals(Constants.ACTION_START_RECORDING, intent.action)
        assertEquals(0, intent.getIntExtra(Constants.EXTRA_CAMERA_LENS, -1))
        assertEquals("UHD_4K", intent.getStringExtra(Constants.EXTRA_VIDEO_QUALITY))
        assertEquals(false, intent.getBooleanExtra(Constants.EXTRA_ENABLE_AUDIO, true))
        assertEquals(15 * 60_000L, intent.getLongExtra(Constants.EXTRA_MAX_DURATION_MS, -1))
        assertEquals(RecordingState.Starting, RecordingRepository.state.value)
    }

    @Test
    fun `a second start while busy sends nothing`() {
        RecordingService.startRecording(app, RecordingConfig())
        shadowOf(app).clearStartedServices()
        RecordingService.startRecording(app, RecordingConfig())
        assertNull(shadowOf(app).nextStartedService)
    }

    @Test
    fun `stop sends the stop action to the service`() {
        RecordingService.stopRecording(app)
        val intent = shadowOf(app).nextStartedService
        assertEquals(Constants.ACTION_STOP_RECORDING, intent.action)
        assertEquals(RecordingService::class.java.name, intent.component?.className)
    }

    @Test
    fun `the notification's stop action reaches the service`() {
        RecordingActionReceiver().onReceive(app, Intent(Constants.ACTION_STOP_RECORDING))
        assertEquals(Constants.ACTION_STOP_RECORDING, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun `the receiver ignores any other action`() {
        RecordingActionReceiver().onReceive(app, Intent(Constants.ACTION_START_RECORDING))
        RecordingActionReceiver().onReceive(app, Intent("com.example.OTHER"))
        assertNull(shadowOf(app).nextStartedService)
    }
}
