package io.celox.xcam.viewmodel

import android.app.Application
import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.celox.xcam.data.AppSettings
import io.celox.xcam.data.PreferencesManager
import io.celox.xcam.data.RecordingRepository
import io.celox.xcam.data.VideoRepository
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.RecordingState
import io.celox.xcam.data.model.ThemeMode
import io.celox.xcam.data.model.VideoFile
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.service.RecordingService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The one ViewModel the whole app shares. It holds no recording logic of its own: the state comes
 * from [RecordingRepository] (written by the service), settings from DataStore, videos from MediaStore.
 */
class RecordingViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesManager(application)
    private val videos = VideoRepository(application)

    val recordingState: StateFlow<RecordingState> = RecordingRepository.state

    /** Null until DataStore has answered — the splash screen stays up until then. */
    val settings: StateFlow<AppSettings?> =
        preferences.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val recordingConfig: StateFlow<RecordingConfig> =
        preferences.settings
            .map { it.recording }
            .stateIn(viewModelScope, SharingStarted.Eagerly, RecordingConfig())

    private val _videoFiles = MutableStateFlow<List<VideoFile>>(emptyList())
    val videoFiles: StateFlow<List<VideoFile>> = _videoFiles.asStateFlow()

    /** True until the first MediaStore query returned, so the empty state never flashes. */
    private val _videosLoading = MutableStateFlow(true)
    val videosLoading: StateFlow<Boolean> = _videosLoading.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    sealed interface UiEvent {
        data class Deleted(val count: Int) : UiEvent

        /** Deleting needs the system's confirmation (items the app no longer owns). */
        data class ConfirmDelete(val intentSender: IntentSender) : UiEvent
    }

    init {
        loadVideoFiles()
        // Refresh when MediaStore changes or a recording was finalized (the observer can lag behind).
        viewModelScope.launch {
            merge(videos.changes(), RecordingRepository.finalized.drop(1).map { }).collect { loadVideoFiles() }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { preferences.setOnboardingCompleted() }
    }

    /**
     * Starts with the saved config — but never asks CameraX for audio without the microphone
     * permission: `withAudioEnabled()` would throw and the recording would fail to start.
     */
    fun startRecording(audioAllowed: Boolean) {
        val config = recordingConfig.value
        RecordingService.startRecording(getApplication(), config.copy(enableAudio = config.enableAudio && audioAllowed))
    }

    fun stopRecording() {
        RecordingService.stopRecording(getApplication())
    }

    fun dismissError() = RecordingRepository.clearError()

    private fun updateConfig(transform: (RecordingConfig) -> RecordingConfig) {
        val next = transform(recordingConfig.value)
        viewModelScope.launch { preferences.setRecordingConfig(next) }
    }

    fun updateCameraLens(lens: Int) = updateConfig { it.copy(cameraLens = lens) }

    fun updateVideoQuality(quality: VideoQuality) = updateConfig { it.copy(videoQuality = quality) }

    fun updateEnableAudio(enabled: Boolean) = updateConfig { it.copy(enableAudio = enabled) }

    fun updateMaxDuration(minutes: Int) = updateConfig { it.copy(maxDurationMinutes = minutes) }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch { preferences.setDynamicColor(enabled) }
    }

    fun loadVideoFiles() {
        viewModelScope.launch {
            _videoFiles.value = runCatching { videos.load() }.getOrDefault(_videoFiles.value)
            _videosLoading.value = false
        }
    }

    fun deleteVideos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            when (val result = videos.delete(uris)) {
                is VideoRepository.DeleteResult.Deleted -> _events.send(UiEvent.Deleted(result.count))
                is VideoRepository.DeleteResult.NeedsConsent -> _events.send(UiEvent.ConfirmDelete(result.intentSender))
            }
            loadVideoFiles()
        }
    }
}
