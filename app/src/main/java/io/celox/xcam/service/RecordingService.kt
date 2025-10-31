package io.celox.xcam.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.celox.xcam.MainActivity
import io.celox.xcam.R
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.receiver.RecordingActionReceiver
import io.celox.xcam.util.Constants
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecordingService : LifecycleService() {

    private var recording: Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var recordingJob: Job? = null
    private var recordingStartTime: Long = 0
    private var maxDurationMillis: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var config: RecordingConfig

    companion object {
        private const val TAG = "RecordingService"

        fun startRecording(context: Context, config: RecordingConfig) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = Constants.ACTION_START_RECORDING
                putExtra(Constants.EXTRA_CAMERA_LENS, config.cameraLens)
                putExtra(Constants.EXTRA_VIDEO_QUALITY, config.videoQuality.ordinal)
                putExtra(Constants.EXTRA_ENABLE_AUDIO, config.enableAudio)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = Constants.ACTION_STOP_RECORDING
            }
            context.startService(intent)
        }

        var isRecording = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Constants.ACTION_START_RECORDING -> {
                config = RecordingConfig(
                    cameraLens = intent.getIntExtra(Constants.EXTRA_CAMERA_LENS, CameraSelector.LENS_FACING_BACK),
                    videoQuality = VideoQuality.values()[intent.getIntExtra(Constants.EXTRA_VIDEO_QUALITY, 1)],
                    enableAudio = intent.getBooleanExtra(Constants.EXTRA_ENABLE_AUDIO, true)
                )
                startForeground(Constants.NOTIFICATION_ID, createNotification("Initializing..."))
                startRecordingVideo()
            }
            Constants.ACTION_STOP_RECORDING -> {
                stopRecordingVideo()
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            Constants.WAKE_LOCK_TAG
        ).apply {
            acquire(TimeUnit.HOURS.toMillis(24)) // Max 24 hours
        }
        Log.d(TAG, "Wake lock acquired")
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "Wake lock released")
            }
        }
    }

    private fun startRecordingVideo() {
        lifecycleScope.launch {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(this@RecordingService).get()

                // Setup recorder
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            when (config.videoQuality) {
                                VideoQuality.HD_720P -> Quality.HD
                                VideoQuality.HD_1080P -> Quality.FHD
                                VideoQuality.UHD_4K -> Quality.UHD
                            }
                        )
                    )
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                // Select camera
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(config.cameraLens)
                    .build()

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind to lifecycle
                cameraProvider.bindToLifecycle(
                    this@RecordingService,
                    cameraSelector,
                    videoCapture
                )

                // Start recording
                startRecordingToFile()

            } catch (e: Exception) {
                Log.e(TAG, "Error starting recording", e)
                stopSelf()
            }
        }
    }

    private fun startRecordingToFile() {
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/${Constants.VIDEO_DIRECTORY}")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        val recorder = videoCapture?.output as? Recorder
        if (recorder == null) {
            Log.e(TAG, "Recorder is null")
            stopSelf()
            return
        }

        recording = if (config.enableAudio) {
            recorder.prepareRecording(this, mediaStoreOutputOptions)
                .withAudioEnabled()
        } else {
            recorder.prepareRecording(this, mediaStoreOutputOptions)
        }.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    recordingStartTime = System.currentTimeMillis()
                    isRecording = true
                    updateNotification("Recording... 00:00")
                    startRecordingTimer()
                    Log.d(TAG, "Recording started")
                }
                is VideoRecordEvent.Finalize -> {
                    isRecording = false
                    if (recordEvent.hasError()) {
                        Log.e(TAG, "Recording error: ${recordEvent.cause?.message}")
                    } else {
                        Log.d(TAG, "Recording saved to: ${recordEvent.outputResults.outputUri}")
                    }
                    stopSelf()
                }
                is VideoRecordEvent.Status -> {
                    // Update recording stats
                }
            }
        }
    }

    private fun startRecordingTimer() {
        recordingJob = lifecycleScope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = System.currentTimeMillis() - recordingStartTime
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60
                updateNotification(String.format("Recording... %02d:%02d", minutes, seconds))

                // Check max duration
                if (maxDurationMillis > 0 && elapsed >= maxDurationMillis) {
                    stopRecordingVideo()
                    break
                }
            }
        }
    }

    private fun stopRecordingVideo() {
        recordingJob?.cancel()
        recording?.stop()
        recording = null
        videoCapture = null
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows recording status"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, RecordingActionReceiver::class.java).apply {
            action = Constants.ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("XCam")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(mainPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(contentText: String) {
        notificationManager.notify(Constants.NOTIFICATION_ID, createNotification(contentText))
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        recordingJob?.cancel()
        releaseWakeLock()
        Log.d(TAG, "Service destroyed")
    }
}
