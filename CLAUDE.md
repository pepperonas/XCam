# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XCam is a native Android app (Kotlin) for background video recording with the screen off. It targets Android 13+ (API 33+) on ARM64 devices only. Package: `io.celox.xcam`. Version 2.0.

## Build Commands

```bash
./gradlew assembleDebug           # Build debug APK
./gradlew assembleReleaseDebug    # Build release-like APK with debug signing (minified + shrunk)
./gradlew assembleRelease         # Build release APK (requires signing config)
./gradlew installDebug            # Install debug APK on connected device
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests on device
```

The project uses a version catalog at `gradle/libs.versions.toml` for dependency management. Some dependencies (CameraX, Navigation, Lifecycle, Accompanist, DataStore, Coroutines, Coil, Media3) are declared inline in `app/build.gradle.kts`.

APK splits are configured for `arm64-v8a` only (no universal APK).

## Architecture

MVVM pattern with Jetpack Compose UI, no dependency injection framework.

### Data Flow

```
MainActivity (NavHost, permission handling, splash screen)
    ├─> OnboardingScreen (first launch, permission flow)
    └─> Screens (MainScreen, SettingsScreen, VideosScreen, VideoPlayerScreen)
            └─> RecordingViewModel (AndroidViewModel, shared across all screens)
                    ├─> RecordingService (LifecycleService, foreground service)
                    ├─> PreferencesManager (DataStore, onboarding state)
                    └─> StateFlows: recordingState, recordingConfig, videoFiles, hasCompletedOnboarding
```

**RecordingViewModel** is the single shared ViewModel created in `MainActivity` and passed to all screens. It holds all state as `MutableStateFlow`/`StateFlow`:
- `recordingState: StateFlow<RecordingState>` - sealed class: Idle, Starting, Recording, Stopping, Error
- `recordingConfig: StateFlow<RecordingConfig>` - data class with camera lens, quality, audio, duration, battery settings
- `videoFiles: StateFlow<List<VideoFile>>` - scanned from `/Movies/XCam/` directory with duration metadata
- `hasCompletedOnboarding: StateFlow<Boolean>` - first-launch onboarding state

**RecordingService** is a `LifecycleService` (not plain Service) that:
- Uses CameraX `VideoCapture`/`Recorder` API for recording
- Saves to MediaStore (`Movies/XCam/`)
- Manages wake lock, notification with stop action, and recording timer
- Communicates recording state via companion object `isRecording` static flag
- Started/stopped via static helper methods `startRecording(context, config)` / `stopRecording(context)`
- Config passed through Intent extras (not shared memory)

**PreferencesManager** wraps DataStore Preferences for persisting onboarding completion state.

**RecordingActionReceiver** is a BroadcastReceiver that handles the stop action from the notification.

### Navigation

String-based routes in `MainActivity.XCamApp()`: `"onboarding"`, `"main"`, `"settings"`, `"videos"`, `"player/{videoIndex}"`. Custom enter/exit/pop transitions with slide + fade animations (300ms). Dynamic start destination based on onboarding state.

### UI

- Jetpack Compose with Material 3
- Dark theme with amber/orange accents (defined in `ui/theme/`)
- Inter font family bundled in `res/font/` (4 weights)
- Custom vector icons in `ui/icons/CustomIcons.kt` (replaces Material Extended Icons to save ~10-15 MB APK size)
- Reusable components in `ui/components/Components.kt` (GlassmorphicCard, AnimatedRecordButton, StatusChip, ShimmerEffect)
- Splash screen via AndroidX SplashScreen API
- 4-page onboarding flow with HorizontalPager
- In-app video player using Media3 ExoPlayer
- Video thumbnails via Coil with VideoFrameDecoder
- Adaptive app icon (stealth eye design) in `res/mipmap-anydpi-v26/`

### Key Constants

All intent actions, extras, notification IDs, and storage paths are centralized in `util/Constants.kt`.

## Build Variants

- **debug**: No minification
- **releaseDebug**: Minified + shrunk with R8, uses debug signing (for testing release behavior)
- **release**: Minified + shrunk with R8, requires release signing config

## Language

README and code comments are in German. Code (variable names, class names) is in English.
