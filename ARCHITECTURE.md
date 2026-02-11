# XCam Architecture Documentation

## Übersicht

XCam verwendet eine moderne Android-Architektur mit MVVM-Pattern, Jetpack Compose für die UI, CameraX für die Kamera und Media3 ExoPlayer für die Video-Wiedergabe.

```
┌──────────────────────────────────────────────────────────┐
│                 UI Layer (Compose)                        │
│  ┌───────────┐ ┌──────┐ ┌────────┐ ┌──────┐ ┌────────┐ │
│  │Onboarding │ │ Main │ │Settings│ │Videos│ │ Player │ │
│  │  Screen   │ │Screen│ │ Screen │ │Screen│ │ Screen │ │
│  └───────────┘ └──────┘ └────────┘ └──────┘ └────────┘ │
└───────────────────────┬──────────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────────┐
│                ViewModel Layer                            │
│             ┌─────────────────────┐                      │
│             │ RecordingViewModel  │                      │
│             └─────────────────────┘                      │
└──────────┬────────────────────────────┬──────────────────┘
           │                            │
┌──────────▼──────────┐  ┌─────────────▼──────────────────┐
│   Service Layer     │  │       Data Layer                │
│ ┌─────────────────┐ │  │ ┌──────────────┐ ┌───────────┐ │
│ │RecordingService │ │  │ │Preferences   │ │  Models   │ │
│ └─────────────────┘ │  │ │  Manager     │ │           │ │
│ ┌─────────────────┐ │  │ └──────────────┘ └───────────┘ │
│ │ ActionReceiver  │ │  │ ┌──────────────┐               │
│ └─────────────────┘ │  │ │MediaStore API│               │
└─────────────────────┘  │ └──────────────┘               │
                         └────────────────────────────────┘
```

## Navigation

```
                    ┌─────────────┐
                    │   Splash    │
                    │   Screen    │
                    └──────┬──────┘
                           │
              hasOnboarding?│
              ┌─────────No─┤─Yes──────┐
              ▼                       ▼
     ┌────────────────┐       ┌──────────┐
     │  Onboarding    │       │   Main   │◄───┐
     │  (4 Pages)     │──────►│  Screen  │    │
     └────────────────┘       └────┬─────┘    │
                                   │          │
                    ┌──────────────┼──────┐   │
                    ▼              ▼       │   │
            ┌──────────┐   ┌──────────┐  │   │
            │ Settings │   │  Videos  │  │   │
            │  Screen  │   │  Screen  │  │   │
            └──────────┘   └────┬─────┘  │   │
                                │        │   │
                                ▼        │   │
                         ┌──────────┐    │   │
                         │  Video   │    │   │
                         │  Player  │────┘   │
                         └──────────┘        │
                                             │
                         Alle Screens ───────┘
                         (popBackStack)
```

**Routen:** `"onboarding"`, `"main"`, `"settings"`, `"videos"`, `"player/{videoIndex}"`

**Transitions:** 300ms Slide + Fade Animationen, Pop-Transitions gespiegelt.

## Komponenten-Übersicht

### 1. Data Layer

#### PreferencesManager.kt
DataStore-Wrapper für persistente Einstellungen.

```kotlin
class PreferencesManager(context: Context) {
    val hasCompletedOnboarding: Flow<Boolean>  // Beobachtbar
    suspend fun setOnboardingCompleted()       // Einmalig beim Abschluss
}
```

#### RecordingConfig.kt
Datenklasse für Recording-Konfiguration.

```kotlin
data class RecordingConfig(
    val cameraLens: Int,            // LENS_FACING_BACK oder FRONT
    val videoQuality: VideoQuality,  // HD_720P, HD_1080P, UHD_4K
    val enableAudio: Boolean,
    val maxDurationMinutes: Int,     // 0 = unbegrenzt
    val stopAtLowBattery: Boolean,
    val lowBatteryThreshold: Int     // Standard: 10%
)
```

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

State Machine:
```
Idle → Starting → Recording → Stopping → Idle
         ↓           ↓
       Error       Error
```

#### VideoFile.kt
Datenklasse für Video-Dateien.

```kotlin
data class VideoFile(
    val file: File,
    val name: String,
    val size: Long,
    val duration: Long,          // Millisekunden, extrahiert via MediaMetadataRetriever
    val timestamp: Long,
    val thumbnailPath: String?   // Dateipfad für Coil VideoFrameDecoder
) {
    val sizeInMB: Float          // Berechnete Größe in MB
    val formattedDuration: String // "H:MM:SS" oder "M:SS"
}
```

### 2. Service Layer

#### RecordingService.kt
**Typ:** Foreground Service (extends `LifecycleService`)

**Lifecycle:**
```
START_RECORDING intent
    ↓
onCreate() → acquireWakeLock()
    ↓
onStartCommand() → startForeground(notification mit ic_notification)
    ↓
startRecordingVideo() → CameraX binden
    ↓
startRecordingToFile() → recording.start()
    ↓
[Recording läuft, Notification-Timer jede Sekunde]
    ↓
STOP_RECORDING intent
    ↓
stopRecordingVideo() → recording.stop()
    ↓
onDestroy() → releaseWakeLock()
```

**Statische API:**
```kotlin
companion object {
    var isRecording: Boolean              // Globaler Status-Flag
    fun startRecording(context, config)   // Service starten
    fun stopRecording(context)            // Service stoppen
}
```

#### RecordingActionReceiver.kt
BroadcastReceiver für Notification-Stop-Button.

```kotlin
override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
        ACTION_STOP_RECORDING -> RecordingService.stopRecording(context)
    }
}
```

### 3. ViewModel Layer

#### RecordingViewModel.kt
**Typ:** `AndroidViewModel` — einziges ViewModel, geteilt zwischen allen Screens.

**StateFlows:**
```kotlin
val recordingState: StateFlow<RecordingState>     // UI-Zustand
val recordingConfig: StateFlow<RecordingConfig>   // Einstellungen
val videoFiles: StateFlow<List<VideoFile>>        // Video-Liste mit Dauer + Thumbnails
val hasCompletedOnboarding: StateFlow<Boolean>    // Onboarding-Status
```

**Public API:**
```kotlin
// Recording
fun startRecording()
fun stopRecording()
fun isRecording(): Boolean

// Konfiguration
fun updateCameraLens(lens: Int)
fun updateVideoQuality(quality: VideoQuality)
fun updateEnableAudio(enabled: Boolean)
fun updateMaxDuration(minutes: Int)
fun updateStopAtLowBattery(enabled: Boolean)

// Video-Management
fun loadVideoFiles()                // Scannt /Movies/XCam/, extrahiert Dauer
fun deleteVideo(videoFile: VideoFile)

// Onboarding
fun completeOnboarding()            // Persistiert in DataStore
```

**Video-Laden:** Scannt `/Movies/XCam/` auf IO-Dispatcher, extrahiert Dauer via `MediaMetadataRetriever`, setzt `thumbnailPath` auf Dateipfad (Coil `VideoFrameDecoder` generiert Thumbnail).

### 4. UI Layer

#### Theme

**Color.kt — Dark & Amber Palette:**
- Primär: `Amber80`, `Amber60`, `Amber40` (Primary), `Amber30`, `Amber20`
- Recording: `RecordingAmber`, `RecordingAmberGlow`, `RecordingAmberPulse`
- Recording-Dot: `RecordingRed` (universell erkennbar)
- Oberflächen: `DarkBackground` (#0A0A0A), `DarkSurface` (#121212), `DarkSurfaceVariant` (#1E1E1E)
- Glassmorphismus: `SurfaceGlass` (10% weiß), `SurfaceGlassStroke` (20% weiß)
- Semantisch: `SuccessGreen`, `WarningAmber`, `ErrorRed`, `InfoBlue`

**Theme.kt:**
- `XCamDarkColorScheme` und `XCamLightColorScheme` mit Amber-Akzenten
- Dynamische Farben deaktiviert (immer Custom Theme)

**Type.kt — Inter Font:**
- `InterFontFamily` mit 4 Gewichten (Regular, Medium, SemiBold, Bold)
- Alle Material 3 Typography-Styles verwenden Inter

#### Components.kt

| Komponente | Beschreibung |
|-----------|-------------|
| `AnimatedRecordingIndicator` | Pulsierender Amber-Punkt mit Glow + rotierender Gradient-Bogen bei Recording, Breathe-Animation bei Idle |
| `AnimatedRecordButton` | 72dp FAB mit Spring-Animation, Haptic Feedback |
| `GlassmorphicCard` | Semi-transparente Karte mit `SurfaceGlass` Hintergrund + `SurfaceGlassStroke` Border |
| `GradientCard` | Delegiert zu `GlassmorphicCard` |
| `StatusChip` | Recording-Status-Badge mit Alpha-Pulsierung |
| `ShimmerEffect` | Lade-Shimmer für Thumbnails |
| `EmptyState` | Zentrierte Nachricht für leere Listen |
| `ConfigDisplayRow` | Label-Value-Paar |

#### Screens

**OnboardingScreen.kt:**
- `HorizontalPager` mit 4 Seiten und Amber Page-Indicator-Dots
- Spring-animierte Seitentransitionen mit Scale-Effekt
- Auto-Advance nach erteilter Berechtigung
- Berechtigungs-Granted-Indikator (grüner Badge)

**MainScreen.kt:**
- Transparente TopAppBar
- Radialer Ambient-Gradient-Hintergrund (`Amber40` bei 3% Alpha)
- Konzentrische Ringe (180dp/160dp) hinter Recording-Indikator
- Elapsed-Time-Display (Monospace, `RecordingAmber`)
- GlassmorphicCard für Konfigurationsanzeige

**SettingsScreen.kt:**
- Alle Einstellungen in GlassmorphicCards
- SegmentedButtons für Kamera und Qualität
- Weicher Amber-Hinweis für Legal Disclaimer
- "About XCam"-Sektion mit Versionsnummer

**VideosScreen.kt:**
- `AsyncImage` (Coil) mit `VideoFrameDecoder` für echte Thumbnails (80dp)
- Play-Icon-Overlay und Dauer-Badge auf Thumbnails
- Tap navigiert zu `player/{index}`
- Glassmorphe Karten mit staggered Entrance-Animationen

**VideoPlayerScreen.kt:**
- Vollbild `PlayerView` via `AndroidView` mit Media3 ExoPlayer
- Auto-hiding Controls (3s Timeout, Toggle per Tap)
- Zentraler Play/Pause-Button (72dp, halbtransparenter Kreis)
- Amber Seek Bar + Zeitanzeige (Monospace)
- Proper `DisposableEffect` für ExoPlayer-Lifecycle

#### Icons (CustomIcons.kt)

Eigene `ImageVector`-Icons statt Material Extended Icons Library (~10-15 MB Ersparnis):

| Icon | Verwendung |
|------|-----------|
| `VideocamCustom` | Onboarding, allgemein |
| `FiberManualRecordCustom` | Recording-Indikator |
| `PlayArrowCustom` / `PauseCustom` | Player Controls |
| `StopCustom` | Recording Stop |
| `ArrowBackCustom` | Navigation zurück |
| `SettingsCustom` | Einstellungen |
| `VideoLibraryCustom` / `VideoFileCustom` | Video-Liste |
| `DeleteCustom` | Video löschen |
| `WarningCustom` | Berechtigungen |
| `ShareCustom` | Video teilen |
| `CameraLensCustom` / `MicrophoneCustom` / `NotificationBellCustom` / `ShieldCustom` | Onboarding |
| `InfoCustom` | About-Sektion |
| `ChevronRightCustom` / `FullscreenCustom` | Navigation/UI |

### 5. Utility Layer

#### Constants.kt
```kotlin
object Constants {
    const val NOTIFICATION_CHANNEL_ID = "recording_channel"
    const val NOTIFICATION_ID = 1001
    const val ACTION_START_RECORDING = "io.celox.xcam.ACTION_START_RECORDING"
    const val ACTION_STOP_RECORDING = "io.celox.xcam.ACTION_STOP_RECORDING"
    const val VIDEO_DIRECTORY = "XCam"
    const val VIDEO_FILE_PREFIX = "VID_"
    const val WAKE_LOCK_TAG = "XCam::RecordingWakeLock"
    // ...
}
```

#### PermissionUtils.kt
```kotlin
object PermissionUtils {
    fun getRequiredPermissions(): List<String>     // CAMERA, RECORD_AUDIO, POST_NOTIFICATIONS
    fun hasAllPermissions(context: Context): Boolean
    fun getMissingPermissions(context: Context): List<String>
}
```

## Datenfluss

### Recording starten

```
User drückt "Start Recording"
    ↓
RecordingViewModel.startRecording()
    ↓
RecordingService.startRecording(context, config) → Intent
    ↓
Service: startForeground() + CameraX binden + recording.start()
    ↓
ViewModel: recordingState → Recording(startTime, path)
    ↓
MainScreen: Amber-Glow + Timer + konz. Ringe anzeigen
```

### Recording stoppen

```
User drückt "Stop" (Notification oder App)
    ↓
RecordingService.stopRecording() → Intent / RecordingActionReceiver
    ↓
Service: recording.stop() → Video in MediaStore
    ↓
Service: stopSelf() + releaseWakeLock()
    ↓
ViewModel: recordingState → Idle, loadVideoFiles()
    ↓
VideosScreen: Neue Aufnahme mit Thumbnail sichtbar
```

### Video abspielen

```
User tippt Video-Thumbnail in VideosScreen
    ↓
Navigate to "player/{videoIndex}"
    ↓
VideoPlayerScreen: ExoPlayer.Builder(context).build()
    ↓
MediaItem.fromUri(videoFile.file) → prepare() → playWhenReady
    ↓
Player.Listener → isPlaying, duration tracking
    ↓
DisposableEffect onDispose → exoPlayer.release()
```

## Threading

| Thread | Operationen |
|--------|------------|
| **UI/Main** | Compose Rendering, StateFlow Collection, User-Input |
| **Main** | Service Lifecycle, CameraX Callbacks (MainExecutor), Notification Updates |
| **IO (Coroutines)** | Video-Dateien scannen, MediaMetadataRetriever, Datei-Löschung, DataStore |
| **CameraX Thread Pool** | Video-Encoding, Frame-Processing, File I/O |

## Abhängigkeiten

| Bibliothek | Version | Zweck |
|-----------|---------|-------|
| Jetpack Compose + Material 3 | BOM | UI Framework |
| Compose Foundation | 1.6.0 | HorizontalPager |
| Navigation Compose | 2.7.5 | Screen-Navigation |
| CameraX | 1.3.0 | Kamera + Video-Recording |
| Media3 ExoPlayer | 1.2.0 | Video-Wiedergabe |
| Coil | 2.5.0 | Bild-Laden + Video-Thumbnails |
| Core SplashScreen | 1.0.1 | Nativer Splash Screen |
| DataStore Preferences | 1.0.0 | Persistente Einstellungen |
| Lifecycle | 2.6.2 | ViewModel, Service |
| Accompanist Permissions | 0.32.0 | Runtime Permissions |
| Coroutines | 1.7.3 | Asynchrone Operationen |

## CI/CD

GitHub Actions Workflow (`.github/workflows/build-apk.yml`):
- **Trigger:** Git-Tag `v*` oder manuell (workflow_dispatch)
- **Build:** `assembleReleaseDebug` (minifiziert mit R8, Debug-Signatur)
- **Output:** `XCam-{version}-arm64-v8a.apk`
- **Release:** Automatische GitHub Release-Erstellung mit APK-Anhang

---

**Version:** 2.0
**Last Updated:** 2025
