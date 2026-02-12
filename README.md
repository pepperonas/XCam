<div align="center">

# XCam

**Background Video Recorder for Android**

[![CI](https://github.com/pepperonas/XCam/actions/workflows/ci.yml/badge.svg)](https://github.com/pepperonas/XCam/actions/workflows/ci.yml)
[![Release](https://github.com/pepperonas/XCam/actions/workflows/build-apk.yml/badge.svg)](https://github.com/pepperonas/XCam/actions/workflows/build-apk.yml)
[![GitHub release](https://img.shields.io/github/v/release/pepperonas/XCam?include_prereleases&style=flat&color=F59E0B)](https://github.com/pepperonas/XCam/releases)
[![API](https://img.shields.io/badge/API-33%2B-brightgreen?style=flat)](https://android-arsenal.com/api?level=33)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat)](LICENSE)

Record video with the screen off. A native Android app for discreet background recording — built with Kotlin, Jetpack Compose, and CameraX.

[**Download APK**](https://github.com/pepperonas/XCam/releases/latest) · [Architecture](ARCHITECTURE.md) · [Setup Guide](SETUP.md)

</div>

---

## Features

<table>
<tr>
<td width="50%">

### Recording
- Background video recording with screen off
- Front & rear camera support
- 720p / 1080p / 4K quality
- Optional audio recording
- Auto-stop at low battery (<10%)
- Wake lock keeps device active
- Foreground notification with timer & stop button

</td>
<td width="50%">

### User Experience
- Dark & Amber premium theme
- Glassmorphic card design
- Animated recording indicator with glow effect
- Inter font family (4 weights)
- 4-page onboarding with permission flow
- Native splash screen (Android 12+ API)
- Haptic feedback on all interactions

</td>
</tr>
<tr>
<td width="50%">

### Video Management
- Real video thumbnails via Coil
- Duration badges on thumbnails
- Tap-to-play with built-in player
- Full-screen ExoPlayer playback
- Auto-hiding player controls
- Seek bar with time display
- Delete with confirmation dialog

</td>
<td width="50%">

### Technical
- MVVM architecture
- Jetpack Compose + Material 3
- CameraX VideoCapture API
- Media3 ExoPlayer
- DataStore for preferences
- Custom vector icons (~15 MB savings)
- Adaptive icon (stealth eye design)

</td>
</tr>
</table>

## Quick Start

### Install from Release

1. Download the latest APK from the [**Releases page**](https://github.com/pepperonas/XCam/releases/latest)
2. Install on your Android 13+ device (ARM64)
3. Follow the onboarding flow to grant permissions
4. Tap **Start Recording** and lock your screen

### Build from Source

```bash
git clone https://github.com/pepperonas/XCam.git
cd XCam
./gradlew assembleDebug
./gradlew installDebug
```

> **Requirements:** Android Studio Hedgehog+, JDK 17, Android SDK 34

## Architecture

```
MainActivity (Splash, NavHost, Permissions)
├── OnboardingScreen ─── 4-page HorizontalPager with permission flow
├── MainScreen ────────── Recording controls + animated indicator
├── SettingsScreen ────── Glassmorphic config cards
├── VideosScreen ──────── Thumbnail grid with tap-to-play
└── VideoPlayerScreen ─── Full-screen ExoPlayer

    RecordingViewModel (shared AndroidViewModel)
    ├── RecordingService ─── CameraX foreground service
    ├── PreferencesManager ── DataStore wrapper
    └── StateFlows: recordingState, recordingConfig, videoFiles
```

| Layer | Components |
|-------|-----------|
| **UI** | Jetpack Compose, Material 3, Custom Icons, Inter Font |
| **Navigation** | `onboarding` → `main` → `settings` / `videos` → `player/{id}` |
| **State** | `RecordingViewModel` with `StateFlow` (Idle → Starting → Recording → Stopping) |
| **Service** | `RecordingService` (LifecycleService, CameraX, WakeLock, Notification) |
| **Data** | `PreferencesManager` (DataStore), `VideoFile` (MediaMetadataRetriever) |

> Full architecture documentation: [ARCHITECTURE.md](ARCHITECTURE.md)

## Testing

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lintDebug

# Run all checks
./gradlew testDebugUnitTest lintDebug
```

**44 unit tests** covering:

| Suite | Tests | Coverage |
|-------|-------|----------|
| `VideoFileTest` | 7 | Size calculation, duration formatting, data class behavior |
| `RecordingStateTest` | 8 | Sealed class singletons, state equality, type checks |
| `RecordingConfigTest` | 6 | Default values, copy semantics, equality |
| `VideoQualityTest` | 6 | Enum properties, resolution ordering, valueOf |
| `ConstantsTest` | 9 | Action prefixes, uniqueness, non-empty values |
| `ColorTest` | 7 | Brightness ordering, alpha transparency, gradient consistency |
| `ExampleUnitTest` | 1 | Sanity check |

CI runs tests automatically on every push to `main` and on pull requests.

## CI/CD

| Workflow | Trigger | Steps |
|----------|---------|-------|
| **CI** | Push to `main`, PRs | Unit Tests → Lint → Build |
| **Release** | Git tag `v*` | Unit Tests → Build → GitHub Release with APK |

### Create a Release

```bash
# Bump versionCode + versionName in app/build.gradle.kts, then:
git tag -a v2.1 -m "v2.1 - Description"
git push origin v2.1
```

The workflow builds a minified APK (R8), attaches it to the GitHub Release, and generates release notes automatically.

## Project Structure

```
app/src/main/
├── java/io/celox/xcam/
│   ├── MainActivity.kt                 # Entry point, splash, navigation
│   ├── data/
│   │   ├── PreferencesManager.kt       # DataStore wrapper
│   │   └── model/
│   │       ├── RecordingConfig.kt      # Camera, quality, audio config
│   │       ├── RecordingState.kt       # Sealed class state machine
│   │       └── VideoFile.kt            # Video metadata + formatting
│   ├── service/
│   │   └── RecordingService.kt         # CameraX foreground service
│   ├── receiver/
│   │   └── RecordingActionReceiver.kt  # Notification stop handler
│   ├── viewmodel/
│   │   └── RecordingViewModel.kt       # Shared state management
│   ├── ui/
│   │   ├── components/Components.kt    # GlassmorphicCard, ShimmerEffect, ...
│   │   ├── icons/CustomIcons.kt        # 20 custom vector icons
│   │   ├── screens/
│   │   │   ├── OnboardingScreen.kt     # 4-page permission flow
│   │   │   ├── MainScreen.kt           # Recording UI
│   │   │   ├── SettingsScreen.kt       # Configuration
│   │   │   ├── VideosScreen.kt         # Thumbnail list
│   │   │   └── VideoPlayerScreen.kt    # ExoPlayer playback
│   │   └── theme/                      # Dark & Amber palette, Inter font
│   └── util/
│       ├── Constants.kt                # Centralized constants
│       └── PermissionUtils.kt          # Runtime permission helpers
└── res/
    ├── drawable/                        # Adaptive icon, splash, notification
    ├── font/                            # Inter TTF (4 weights)
    ├── mipmap-anydpi-v26/               # Adaptive icon config
    └── values/                          # Colors, strings, themes
```

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3 |
| Camera | CameraX 1.3 (camera2, lifecycle, video) |
| Video Playback | Media3 ExoPlayer 1.2 |
| Image Loading | Coil 2.5 (compose + video thumbnails) |
| Navigation | Navigation Compose 2.7 |
| State | Kotlin StateFlow / Coroutines 1.7 |
| Persistence | DataStore Preferences 1.0 |
| Splash | AndroidX SplashScreen 1.0 |
| Permissions | Accompanist Permissions 0.32 |
| Build | Gradle 8.13, AGP, R8 |
| CI/CD | GitHub Actions |
| Min SDK | 33 (Android 13) |
| Target SDK | 34 (Android 14) |
| ABI | arm64-v8a |

## Performance

| Quality | Battery / Hour | Storage / Hour |
|---------|---------------|----------------|
| 720p HD | ~10-15% | ~500 MB |
| 1080p Full HD | ~15-20% | ~1-2 GB |
| 4K Ultra HD | ~25-35% | ~4-8 GB |

## Legal Notice

> **This app must only be used with the consent of all recorded persons. Unauthorized recording may be illegal and subject to prosecution. The user bears full responsibility for lawful use.**

## License

```
MIT License

Copyright (c) 2025 Martin Pfeffer
```

See [LICENSE](LICENSE) for the full text.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Write tests for your changes
4. Ensure all tests pass (`./gradlew testDebugUnitTest`)
5. Commit your changes
6. Push and open a Pull Request

---

<div align="center">

**Built by [Martin Pfeffer](https://celox.io)** · [celox.io](https://celox.io) · martin.pfeffer@celox.io

</div>
