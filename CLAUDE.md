# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XCam is a native Android app (Kotlin) for background video recording with the screen off. Targets Android 13+ (API 33+), ARM64 only. Package: `io.celox.xcam`.

## Build & Test Commands

```bash
# Build
./gradlew assembleDebug           # Debug APK (no minification)
./gradlew assembleReleaseDebug    # Release-like APK with debug signing (R8 minified)
./gradlew assembleRelease         # Release APK (requires signing config)
./gradlew installDebug            # Build and install on connected device

# Test
./gradlew testDebugUnitTest       # Run all 43 unit tests
./gradlew testDebugUnitTest --tests "io.celox.xcam.data.model.VideoFileTest"        # Run single test class
./gradlew testDebugUnitTest --tests "io.celox.xcam.data.model.VideoFileTest.sizeInMB*"  # Run single test method

# Lint
./gradlew lintDebug               # Android lint (CI enforces 0 errors)

# Combined (matches CI pipeline)
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Test reports: `app/build/reports/tests/testDebugUnitTest/index.html`
Lint report: `app/build/reports/lint-results-debug.html`

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`): runs on push to `main` and PRs. Three jobs: Unit Tests, Lint Check, Build APK (depends on test+lint). Release workflow (`.github/workflows/build-apk.yml`) triggers on `v*` tags.

## Architecture

MVVM with Jetpack Compose, no DI framework. Single shared `RecordingViewModel` (AndroidViewModel) created in `MainActivity` and passed to all screens.

### Key State Flows

- `recordingState: StateFlow<RecordingState>` — sealed class: Idle, Starting, Recording(startTime, outputPath), Stopping, Error(message)
- `recordingConfig: StateFlow<RecordingConfig>` — camera lens, quality, audio, duration, battery settings
- `videoFiles: StateFlow<List<VideoFile>>` — scanned from `/Movies/XCam/` with duration metadata
- `hasCompletedOnboarding: StateFlow<Boolean>` — backed by DataStore via `PreferencesManager`

### Navigation

String routes in `MainActivity.XCamApp()`: `"onboarding"` → `"main"` → `"settings"` / `"videos"` → `"player/{videoIndex}"`. Start destination is dynamic based on onboarding state.

### Recording Pipeline

`RecordingViewModel.startRecording()` → `RecordingService.startRecording(context, config)` (static helper, starts foreground service via Intent) → CameraX `VideoCapture`/`Recorder` → saves to MediaStore `Movies/XCam/`. Service is a `LifecycleService` with wake lock + notification with stop action. Config is passed through Intent extras. Recording state is communicated via `RecordingService.isRecording` companion object flag (polled by ViewModel).

### UI Layer

- Dark theme with amber/orange accents (`ui/theme/`)
- Custom vector icons in `ui/icons/CustomIcons.kt` (replaces Material Extended Icons, saves ~10-15 MB)
- Reusable components in `ui/components/Components.kt`: GlassmorphicCard, AnimatedRecordButton, AnimatedRecordingIndicator, StatusChip, ShimmerEffect
- Video playback: Media3 ExoPlayer in `VideoPlayerScreen`
- Video thumbnails: Coil with `VideoFrameDecoder` in `VideosScreen`
- Inter font family bundled in `res/font/` (4 weights)

### Dependencies

Version catalog at `gradle/libs.versions.toml` for core deps (Compose BOM, core-ktx, JUnit). Many deps declared inline in `app/build.gradle.kts`: CameraX 1.3, Media3 1.2, Coil 2.5, Navigation Compose 2.7, Accompanist Permissions 0.32, DataStore 1.0, Compose Foundation 1.6, SplashScreen 1.0.

## Build Variants

- **debug**: No minification
- **releaseDebug**: R8 minified + shrunk, debug signing (test release behavior without signing config)
- **release**: R8 minified + shrunk, requires release signing config

## Lint Suppressions

- `@SuppressLint("MissingPermission")` on `RecordingService.startRecordingToFile()` — permissions are verified before service starts
- `@Suppress("UnsafeOptInUsageError")` on `PlayerView.setShowBuffering()` in `VideoPlayerScreen` — Media3 unstable API

## Key Constants

All intent actions, extras, notification IDs, and storage paths centralized in `util/Constants.kt`. Videos stored at `Movies/XCam/*.mp4`.
