package io.celox.xcam.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.celox.xcam.MainActivity
import io.celox.xcam.R
import io.celox.xcam.data.RecordingRepository
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.RecordingState
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.receiver.RecordingActionReceiver
import io.celox.xcam.util.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Records video with the screen off: a camera|microphone foreground service holding a partial wake
 * lock, bound to its own lifecycle so CameraX keeps running without any UI.
 *
 * It is also the only writer of [RecordingRepository]: every state the UI shows comes from a real
 * CameraX event here, never from a guess on the UI side.
 */
class RecordingService : LifecycleService() {
    private var recording: Recording? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var finalizeTimeout: Job? = null
    private lateinit var config: RecordingConfig

    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val TAG = "RecordingService"

        /** How long to wait for CameraX's Finalize after stop() before giving up and shutting down. */
        private const val FINALIZE_TIMEOUT_MS = 5_000L

        fun startRecording(
            context: Context,
            config: RecordingConfig,
        ) {
            if (!RecordingRepository.onStartRequested()) return
            val intent =
                Intent(context, RecordingService::class.java).apply {
                    action = Constants.ACTION_START_RECORDING
                    putExtra(Constants.EXTRA_CAMERA_LENS, config.cameraLens)
                    putExtra(Constants.EXTRA_VIDEO_QUALITY, config.videoQuality.name)
                    putExtra(Constants.EXTRA_ENABLE_AUDIO, config.enableAudio)
                    putExtra(Constants.EXTRA_MAX_DURATION_MS, config.maxDurationMillis)
                }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: IllegalStateException) {
                // Background-start restriction: the service never ran, so nothing else will settle the state.
                RecordingRepository.onError(RecordingState.Error(RecordingState.Reason.CAMERA_UNAVAILABLE, e.message))
            }
        }

        fun stopRecording(context: Context) {
            val intent =
                Intent(context, RecordingService::class.java).apply {
                    action = Constants.ACTION_STOP_RECORDING
                }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Constants.ACTION_START_RECORDING -> {
                config =
                    RecordingConfig(
                        cameraLens = intent.getIntExtra(Constants.EXTRA_CAMERA_LENS, CameraSelector.LENS_FACING_BACK),
                        videoQuality = VideoQuality.fromName(intent.getStringExtra(Constants.EXTRA_VIDEO_QUALITY)),
                        enableAudio = intent.getBooleanExtra(Constants.EXTRA_ENABLE_AUDIO, true),
                        maxDurationMinutes =
                        (intent.getLongExtra(Constants.EXTRA_MAX_DURATION_MS, 0L) / 60_000L).toInt(),
                    )

                try {
                    startForeground(
                        Constants.NOTIFICATION_ID,
                        buildNotification(startTime = null),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
                    )
                } catch (e: Exception) {
                    // e.g. a missing permission or a background-start restriction.
                    Log.e(TAG, "Cannot enter foreground", e)
                    fail(RecordingState.Reason.CAMERA_UNAVAILABLE, e.message)
                    return START_NOT_STICKY
                }
                acquireWakeLock()
                startRecordingVideo()
            }
            Constants.ACTION_STOP_RECORDING -> stopRecordingVideo()
            // A sticky restart after the process died has no recording to resume.
            else -> stopSelf()
        }

        // Not sticky: a system restart could not resume the old recording anyway and would only
        // re-open the camera with nobody asking for it.
        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.WAKE_LOCK_TAG).apply {
                acquire(TimeUnit.HOURS.toMillis(24)) // Max 24 hours
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun startRecordingVideo() {
        lifecycleScope.launch {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(this@RecordingService).await()

                val recorder =
                    Recorder.Builder()
                        .setQualitySelector(
                            QualitySelector.from(
                                when (config.videoQuality) {
                                    VideoQuality.HD_720P -> Quality.HD
                                    VideoQuality.HD_1080P -> Quality.FHD
                                    VideoQuality.UHD_4K -> Quality.UHD
                                },
                                // Fall back instead of failing when a lens cannot do the chosen size.
                                androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD),
                            ),
                        ).build()
                val videoCapture = VideoCapture.withOutput(recorder)
                val cameraSelector = CameraSelector.Builder().requireLensFacing(config.cameraLens).build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@RecordingService, cameraSelector, videoCapture)

                startRecordingToFile(recorder)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting recording", e)
                fail(RecordingState.Reason.CAMERA_UNAVAILABLE, e.message)
            }
        }
    }

    // Permissions are verified by the UI before the service is started.
    @SuppressLint("MissingPermission")
    private fun startRecordingToFile(recorder: Recorder) {
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, Constants.VIDEO_MIME_TYPE)
                put(MediaStore.Video.Media.RELATIVE_PATH, Constants.RELATIVE_VIDEO_PATH)
            }

        val outputOptions =
            MediaStoreOutputOptions
                .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                // CameraX enforces the limit itself and finalizes with ERROR_DURATION_LIMIT_REACHED.
                .apply { if (config.maxDurationMillis > 0) setDurationLimitMillis(config.maxDurationMillis) }
                .build()

        val pending = recorder.prepareRecording(this, outputOptions)
        recording =
            (if (config.enableAudio) pending.withAudioEnabled() else pending)
                .start(ContextCompat.getMainExecutor(this)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            val start = System.currentTimeMillis()
                            RecordingRepository.onRecordingStarted(start, config.maxDurationMillis)
                            notificationManager.notify(Constants.NOTIFICATION_ID, buildNotification(start))
                        }
                        is VideoRecordEvent.Finalize -> onFinalized(event)
                        else -> Unit
                    }
                }
    }

    private fun onFinalized(event: VideoRecordEvent.Finalize) {
        finalizeTimeout?.cancel()
        val error =
            when (event.error) {
                VideoRecordEvent.Finalize.ERROR_NONE,
                // Reaching the user's own limit is a normal end, not a failure.
                VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED,
                -> null
                VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE ->
                    RecordingState.Error(RecordingState.Reason.NO_SPACE, event.cause?.message)
                else -> RecordingState.Error(RecordingState.Reason.RECORDING_FAILED, event.cause?.message)
            }
        if (error != null) Log.e(TAG, "Recording error ${event.error}", event.cause)
        recording = null
        RecordingRepository.onFinalized(error)
        shutdown()
    }

    private fun stopRecordingVideo() {
        RecordingRepository.onStopRequested()
        val active = recording
        if (active == null) {
            // Still binding the camera: nothing to finalize.
            RecordingRepository.onFinalized(null)
            shutdown()
            return
        }
        active.stop()
        // Normally Finalize arrives within milliseconds; never hang in the foreground if it doesn't.
        finalizeTimeout =
            lifecycleScope.launch {
                delay(FINALIZE_TIMEOUT_MS)
                Log.w(TAG, "No Finalize after stop(); shutting down")
                RecordingRepository.onFinalized(null)
                shutdown()
            }
    }

    private fun fail(
        reason: RecordingState.Reason,
        detail: String?,
    ) {
        recording?.close()
        recording = null
        RecordingRepository.onError(RecordingState.Error(reason, detail))
        shutdown()
    }

    private fun shutdown() {
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * [startTime] null = still starting. Once recording, the system chronometer counts up from it,
     * so the notification never has to be rebuilt every second.
     */
    private fun buildNotification(startTime: Long?): Notification {
        val openApp =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val stop =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(this, RecordingActionReceiver::class.java).setAction(Constants.ACTION_STOP_RECORDING),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(if (startTime == null) R.string.notification_starting else R.string.notification_recording))
            .setContentText(getString(R.string.notification_text))
            .setColor(ContextCompat.getColor(this, R.color.brand_primary))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openApp)
            .apply {
                if (startTime != null) {
                    setWhen(startTime)
                    setShowWhen(true)
                    setUsesChronometer(true)
                }
            }
            .addAction(R.drawable.ic_notification_stop, getString(R.string.action_stop), stop)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        finalizeTimeout?.cancel()
        recording?.close()
        recording = null
        releaseWakeLock()
        RecordingRepository.onServiceDestroyed()
        super.onDestroy()
    }
}
