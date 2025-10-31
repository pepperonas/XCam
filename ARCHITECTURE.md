# XCam Architecture Documentation

## Übersicht

XCam verwendet eine moderne Android-Architektur mit MVVM-Pattern, Jetpack Compose für die UI und CameraX für die Kamera-Funktionalität.

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │  Main    │  │Settings  │  │ Videos ││
│  │  Screen  │  │  Screen  │  │ Screen ││
│  └──────────┘  └──────────┘  └────────┘│
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         ViewModel Layer                 │
│      ┌─────────────────────┐            │
│      │ RecordingViewModel  │            │
│      └─────────────────────┘            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Service Layer                   │
│      ┌─────────────────────┐            │
│      │  RecordingService   │            │
│      └─────────────────────┘            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Data Layer                      │
│  ┌────────────┐  ┌──────────────────┐   │
│  │   Models   │  │  MediaStore API  │   │
│  └────────────┘  └──────────────────┘   │
└─────────────────────────────────────────┘
```

## Komponenten-Übersicht

### 1. Data Layer

#### RecordingConfig.kt
Datenklasse für Recording-Konfiguration.

```kotlin
data class RecordingConfig(
    val cameraLens: Int,           // LENS_FACING_BACK oder FRONT
    val videoQuality: VideoQuality, // HD_720P, HD_1080P, UHD_4K
    val enableAudio: Boolean,
    val maxDurationMinutes: Int,
    val stopAtLowBattery: Boolean,
    val lowBatteryThreshold: Int
)
```

**Verwendung:**
- Speichert User-Präferenzen für Recording
- Wird an RecordingService übergeben
- Persistiert durch ViewModel

#### RecordingState.kt
Sealed Class für Recording-Status.

```kotlin
sealed class RecordingState {
    object Idle : RecordingState()
    object Starting : RecordingState()
    data class Recording(startTime: Long, outputPath: String)
    object Stopping : RecordingState()
    data class Error(message: String) : RecordingState()
}
```

**Verwendung:**
- State Management für UI
- Observable durch StateFlow im ViewModel
- Ermöglicht reaktive UI-Updates

#### VideoFile.kt
Datenklasse für Video-Dateien.

```kotlin
data class VideoFile(
    val file: File,
    val name: String,
    val size: Long,
    val duration: Long,
    val timestamp: Long,
    val thumbnailPath: String?
)
```

**Verwendung:**
- Repräsentiert aufgenommene Videos
- Wird in VideosScreen angezeigt
- Ermöglicht Video-Management (Löschen, etc.)

### 2. Service Layer

#### RecordingService.kt
**Typ:** Foreground Service (extends LifecycleService)

**Verantwortlichkeiten:**
1. CameraX initialisieren und konfigurieren
2. Video-Recording starten/stoppen
3. Foreground Notification managen
4. Wake Lock verwalten
5. Video in MediaStore speichern

**Lifecycle:**
```
START_RECORDING intent
    ↓
onCreate() → acquireWakeLock()
    ↓
onStartCommand() → startForeground()
    ↓
startRecordingVideo() → bindCameraX()
    ↓
startRecordingToFile() → recording.start()
    ↓
[Recording läuft...]
    ↓
STOP_RECORDING intent
    ↓
stopRecordingVideo() → recording.stop()
    ↓
onDestroy() → releaseWakeLock()
```

**Wichtige Methoden:**

```kotlin
// Service starten
companion object {
    fun startRecording(context: Context, config: RecordingConfig)
    fun stopRecording(context: Context)
}

// Interne Methoden
private fun startRecordingVideo()  // CameraX Setup
private fun startRecordingToFile() // Recording starten
private fun stopRecordingVideo()   // Recording beenden
private fun acquireWakeLock()      // Wake Lock holen
private fun releaseWakeLock()      // Wake Lock freigeben
private fun createNotification()   // Notification erstellen
```

**CameraX Integration:**
```kotlin
val recorder = Recorder.Builder()
    .setQualitySelector(...)
    .build()

videoCapture = VideoCapture.withOutput(recorder)

cameraProvider.bindToLifecycle(
    this,
    cameraSelector,
    videoCapture
)
```

**Notification Management:**
- Erstellt Foreground Notification mit Stop-Button
- Update-Frequenz: 1 Sekunde (zeigt Aufnahmedauer)
- Persistent während Recording
- Wird bei Stop automatisch entfernt

### 3. ViewModel Layer

#### RecordingViewModel.kt
**Typ:** AndroidViewModel

**Verantwortlichkeiten:**
1. Recording State managen
2. Konfiguration speichern/laden
3. Service-Kontrolle (start/stop)
4. Video-Liste verwalten
5. UI-State bereitstellen

**StateFlows:**
```kotlin
val recordingState: StateFlow<RecordingState>
val recordingConfig: StateFlow<RecordingConfig>
val videoFiles: StateFlow<List<VideoFile>>
```

**Public API:**
```kotlin
// Recording Control
fun startRecording()
fun stopRecording()
fun isRecording(): Boolean

// Configuration
fun updateCameraLens(lens: Int)
fun updateVideoQuality(quality: VideoQuality)
fun updateEnableAudio(enabled: Boolean)
fun updateMaxDuration(minutes: Int)
fun updateStopAtLowBattery(enabled: Boolean)

// Video Management
fun loadVideoFiles()
fun deleteVideo(videoFile: VideoFile)
```

**State Management:**
```
Idle → Starting → Recording → Stopping → Idle
         ↓           ↓
       Error       Error
```

### 4. UI Layer

#### MainActivity.kt
**Typ:** ComponentActivity

**Verantwortlichkeiten:**
1. Entry Point der App
2. Permission Management
3. Navigation Host
4. Theme Provider

**Permission Flow:**
```kotlin
onCreate()
    ↓
hasAllPermissions() → No → Request Permissions
    ↓                         ↓
   Yes                    Launcher Callback
    ↓                         ↓
  Allow Access ←──────────────┘
```

**Navigation:**
```kotlin
NavHost(startDestination = "main") {
    composable("main") { MainScreen(...) }
    composable("settings") { SettingsScreen(...) }
    composable("videos") { VideosScreen(...) }
}
```

#### MainScreen.kt
Haupt-Interface der App.

**Komponenten:**
- TopAppBar mit Navigation-Icons
- Recording Status Display
- Start/Stop Button
- Configuration Card
- Permission Request UI (wenn nötig)

**State Handling:**
```kotlin
val recordingState by viewModel.recordingState.collectAsState()

when (recordingState) {
    is Idle -> ShowStartButton()
    is Recording -> ShowStopButton()
    is Starting -> ShowLoading()
    is Stopping -> ShowLoading()
    is Error -> ShowError()
}
```

#### SettingsScreen.kt
Konfigurations-Interface.

**Settings:**
1. Kamera-Auswahl (Back/Front)
2. Video-Qualität (720p/1080p/4K)
3. Audio Ein/Aus
4. Batterie-Management
5. Legal Disclaimer

**UI Pattern:**
```kotlin
SettingSection(title = "Camera") {
    SegmentedButton(...)  // Auswahl-Buttons
}
```

#### VideosScreen.kt
Video-Verwaltung.

**Features:**
- LazyColumn mit Video-Liste
- Video-Item mit Details (Name, Größe, Datum)
- Delete-Button mit Bestätigungs-Dialog
- Empty State bei keinen Videos

**Video Loading:**
```kotlin
LaunchedEffect(Unit) {
    viewModel.loadVideoFiles()
}
```

### 5. Utility Layer

#### Constants.kt
Zentrale Konstanten-Verwaltung.

```kotlin
object Constants {
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "recording_channel"
    const val NOTIFICATION_ID = 1001

    // Actions
    const val ACTION_START_RECORDING = "..."
    const val ACTION_STOP_RECORDING = "..."

    // Storage
    const val VIDEO_DIRECTORY = "XCam"
    const val VIDEO_FILE_PREFIX = "VID_"
}
```

#### PermissionUtils.kt
Permission-Management Helper.

```kotlin
object PermissionUtils {
    fun getRequiredPermissions(): List<String>
    fun hasAllPermissions(context: Context): Boolean
    fun getMissingPermissions(context: Context): List<String>
}
```

### 6. Receiver Layer

#### RecordingActionReceiver.kt
**Typ:** BroadcastReceiver

**Zweck:**
- Empfängt Broadcast von Notification-Buttons
- Leitet Actions an RecordingService weiter

```kotlin
override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
        ACTION_STOP_RECORDING ->
            RecordingService.stopRecording(context)
    }
}
```

## Datenfluss

### Recording Starten

```
User drückt "Start Recording" in MainScreen
    ↓
MainScreen.onStartRecording()
    ↓
RecordingViewModel.startRecording()
    ↓
RecordingService.startRecording(context, config)
    ↓
Intent mit ACTION_START_RECORDING
    ↓
RecordingService.onStartCommand()
    ↓
startForeground() + startRecordingVideo()
    ↓
CameraX initialisieren + Recording starten
    ↓
VideoRecordEvent.Start callback
    ↓
Notification updaten mit Timer
    ↓
[Recording läuft im Hintergrund]
```

### Recording Stoppen

```
User drückt "Stop" in Notification
    ↓
RecordingActionReceiver.onReceive()
    ↓
RecordingService.stopRecording(context)
    ↓
Intent mit ACTION_STOP_RECORDING
    ↓
RecordingService.stopRecordingVideo()
    ↓
recording.stop()
    ↓
VideoRecordEvent.Finalize callback
    ↓
Video in MediaStore gespeichert
    ↓
stopSelf() + releaseWakeLock()
    ↓
Service destroyed
```

### Video Liste laden

```
VideosScreen wird geöffnet
    ↓
LaunchedEffect triggers
    ↓
RecordingViewModel.loadVideoFiles()
    ↓
Scanne /Movies/XCam/ Verzeichnis
    ↓
Erstelle VideoFile-Objekte
    ↓
Update videoFiles StateFlow
    ↓
UI beobachtet StateFlow
    ↓
LazyColumn zeigt Videos
```

## Threading Model

### UI Thread
- Alle Compose-UI-Updates
- StateFlow-Collection
- Button-Clicks

### Main Thread
- Service Lifecycle (onCreate, onStartCommand, etc.)
- CameraX Callbacks (MainExecutor)
- Notification Updates

### Background (Coroutines)
```kotlin
// In ViewModel
viewModelScope.launch {
    // Video-Datei-Operationen
    // Configuration laden/speichern
}

// In Service
lifecycleScope.launch {
    // Recording Timer
    // Prozess-Monitoring
}
```

### CameraX Thread Pool
- Video-Encoding
- Frame-Processing
- File I/O

## Memory Management

### Wake Lock
```kotlin
// Acquire
PowerManager.newWakeLock(
    PARTIAL_WAKE_LOCK,
    TAG
).acquire(24.hours)

// Release
if (wakeLock.isHeld) {
    wakeLock.release()
}
```

### CameraX Resources
```kotlin
// Cleanup
cameraProvider.unbindAll()
recording?.close()
videoCapture = null
```

## Error Handling

### Service Level
```kotlin
try {
    startRecordingVideo()
} catch (e: Exception) {
    Log.e(TAG, "Error starting recording", e)
    stopSelf()
}
```

### ViewModel Level
```kotlin
try {
    loadVideoFiles()
} catch (e: Exception) {
    _videoFiles.value = emptyList()
}
```

### UI Level
```kotlin
when (val state = recordingState.value) {
    is RecordingState.Error -> {
        ShowErrorDialog(state.message)
    }
}
```

## Best Practices

### 1. State Management
- ✅ Verwende StateFlow für reaktive UI
- ✅ Sealed Classes für type-safe States
- ✅ Zentrales ViewModel für Shared State

### 2. Service Management
- ✅ Foreground Service für sichtbare Background-Arbeit
- ✅ Wake Lock nur während Recording
- ✅ Proper cleanup in onDestroy()

### 3. CameraX
- ✅ unbindAll() vor neu-binden
- ✅ LifecycleService für automatisches Management
- ✅ Error Handling bei Camera-Initialisierung

### 4. Permissions
- ✅ Runtime Permission Requests
- ✅ Graceful Degradation bei fehlenden Permissions
- ✅ User-freundliche Permission-Erklärungen

### 5. UI/UX
- ✅ Loading States anzeigen
- ✅ Error States handhaben
- ✅ Confirmation Dialogs für destructive Actions

## Performance Considerations

### Startup Time
- Lazy Initialization wo möglich
- Vermeiden von blocking Operations im Main Thread

### Memory
- CameraX-Resources nach Verwendung freigeben
- Begrenzung der Video-Liste (Pagination bei sehr vielen Videos)

### Battery
- Wake Lock nur während Recording
- Quality Selector basierend auf User-Präferenz
- Stop bei niedrigem Akkustand

## Testing Strategy

### Unit Tests
- ViewModel Logik
- Permission Utils
- Data Models

### Integration Tests
- Service Lifecycle
- Camera Initialization
- File Storage

### UI Tests
- Navigation Flow
- Permission Requests
- Recording Start/Stop

---

**Version:** 1.0
**Last Updated:** 2024
