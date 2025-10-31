package io.celox.xcam.viewmodel

import android.app.Application
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.RecordingState
import io.celox.xcam.data.model.VideoFile
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.service.RecordingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.os.Environment
import java.io.File

class RecordingViewModel(application: Application) : AndroidViewModel(application) {

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _recordingConfig = MutableStateFlow(RecordingConfig())
    val recordingConfig: StateFlow<RecordingConfig> = _recordingConfig.asStateFlow()

    private val _videoFiles = MutableStateFlow<List<VideoFile>>(emptyList())
    val videoFiles: StateFlow<List<VideoFile>> = _videoFiles.asStateFlow()

    init {
        loadVideoFiles()
    }

    fun startRecording() {
        if (_recordingState.value !is RecordingState.Idle) return

        _recordingState.value = RecordingState.Starting
        RecordingService.startRecording(getApplication(), _recordingConfig.value)

        viewModelScope.launch {
            // Simulate state change (in real app, listen to service broadcast)
            kotlinx.coroutines.delay(1000)
            _recordingState.value = RecordingState.Recording(
                startTime = System.currentTimeMillis(),
                outputPath = ""
            )
        }
    }

    fun stopRecording() {
        if (_recordingState.value !is RecordingState.Recording) return

        _recordingState.value = RecordingState.Stopping
        RecordingService.stopRecording(getApplication())

        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _recordingState.value = RecordingState.Idle
            loadVideoFiles()
        }
    }

    fun updateCameraLens(lens: Int) {
        _recordingConfig.value = _recordingConfig.value.copy(cameraLens = lens)
    }

    fun updateVideoQuality(quality: VideoQuality) {
        _recordingConfig.value = _recordingConfig.value.copy(videoQuality = quality)
    }

    fun updateEnableAudio(enabled: Boolean) {
        _recordingConfig.value = _recordingConfig.value.copy(enableAudio = enabled)
    }

    fun updateMaxDuration(minutes: Int) {
        _recordingConfig.value = _recordingConfig.value.copy(maxDurationMinutes = minutes)
    }

    fun updateStopAtLowBattery(enabled: Boolean) {
        _recordingConfig.value = _recordingConfig.value.copy(stopAtLowBattery = enabled)
    }

    fun loadVideoFiles() {
        viewModelScope.launch {
            try {
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val xCamDir = File(moviesDir, "XCam")

                if (!xCamDir.exists()) {
                    _videoFiles.value = emptyList()
                    return@launch
                }

                val files = xCamDir.listFiles()?.filter { it.extension == "mp4" }
                    ?.map { file ->
                        VideoFile(
                            file = file,
                            name = file.name,
                            size = file.length(),
                            timestamp = file.lastModified()
                        )
                    }?.sortedByDescending { it.timestamp } ?: emptyList()

                _videoFiles.value = files
            } catch (e: Exception) {
                _videoFiles.value = emptyList()
            }
        }
    }

    fun deleteVideo(videoFile: VideoFile) {
        viewModelScope.launch {
            try {
                videoFile.file.delete()
                loadVideoFiles()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun isRecording(): Boolean {
        return RecordingService.isRecording
    }
}
