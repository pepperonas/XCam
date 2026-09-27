# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XCam is a native Android app (Kotlin, Jetpack Compose) for background video recording with the screen off. Android 13+ (minSdk 33), ARM64 only. Package: `io.celox.xcam`. Product page: https://x-cam.celox.io (alias xcam.celox.io), built from `website/`.

## Build & Test Commands

```bash
./gradlew assembleDebug           # Debug APK (no minification)
./gradlew assembleRelease         # Signed release APK (keystore.properties + release.jks in the root, or KEYSTORE_* env)
./gradlew assembleReleaseDebug    # R8 build with debug signing
./gradlew installDebug

./gradlew testDebugUnitTest       # 208 unit tests (JVM + Robolectric)
./gradlew testDebugUnitTest --tests "io.celox.xcam.data.RecordingRepositoryTest"
./gradlew testDebugUnitTest --tests "io.celox.xcam.util.TimeFormatTest.the*"

./gradlew lintDebug               # CI enforces 0 errors
```

- **R8 needs a 6 GB Gradle heap** (`gradle.properties`); with 4 GB `minifyReleaseWithR8` dies with "GC is thrashing". With 2+ workers R8 and `lintVitalAnalyzeRelease` run in parallel and exhaust even 6 GB (`OutOfMemoryError`) — use `--max-workers=1`. When the Mac is swapping heavily (other sessions), even that can fail; the authoritative signed APK is the one the release workflow builds on GitHub, so a local release build is optional.
- **No Compose BOM:** Compose `1.11.0-beta02` + material3 `1.5.0-alpha18` are pinned in `gradle/libs.versions.toml` (same set as flipper-the-ripper) because the M3 Expressive APIs only exist in the alpha line. `ExperimentalMaterial3ExpressiveApi`/`ExperimentalMaterial3Api` are opted in module-wide via `freeCompilerArgs` — no `@OptIn` at call sites. material3 1.5 no longer pulls in `material-icons-core`: icons come from `ui/icons/XIcons.kt` (Material path data; outlined variants are strokes of the filled path).
- Remaining lint warnings are version notices for the deliberately pinned alphas.

## Releases & signing

- Tag `vX.Y.Z` → `.github/workflows/release.yml`: checks tag == `versionName`, cuts the `## [X.Y.Z]` section from `CHANGELOG.md` as release notes (`scripts/release-notes.sh`; a missing section fails the run), runs tests, builds the signed APK, **verifies the certificate SHA-256** (`78f163f0…cf38`) and publishes `xcam-vX.Y.Z.apk` + `SHA256SUMS.txt`.
- Signing key: alias `xcam`, backup and passwords in the private repo `pepperonas/keystore` (`x-cam-keystore/`). Local `release.jks`/`keystore.properties` are gitignored. CI secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- 3.0.0 switched from the debug key to this release key: 2.x installs cannot be updated in place (documented in CHANGELOG/README/website).
- `ci.yml` (push/PR to main): unit tests, lint, debug build.

## Architecture

MVVM with Compose, no DI. One `RecordingViewModel` (AndroidViewModel), obtained in `MainActivity` via `viewModels()` and passed down.

### Recording state — the service is the single source of truth

`data/RecordingRepository` is a process-wide object with `StateFlow<RecordingState>` (+ a `finalized` counter). **Only `RecordingService` writes it**, from real CameraX events: `Starting` on request (`onStartRequested` also refuses a second start), `Recording(startTime, maxDurationMillis)` on `VideoRecordEvent.Start`, `Idle`/`Error(reason)` on `Finalize` (`ERROR_DURATION_LIMIT_REACHED` counts as a normal end), `Idle` in `onDestroy`. The UI never guesses — do not reintroduce timed state changes in the ViewModel.

- Stop flow: `stopRecordingVideo()` calls `recording.stop()` and waits for `Finalize` (5 s fallback) before `stopForeground`/`stopSelf`.
- Max duration is enforced by CameraX (`MediaStoreOutputOptions.setDurationLimitMillis`), passed via `EXTRA_MAX_DURATION_MS`.
- Notification uses the system chronometer (`setUsesChronometer`), not a per-second rebuild. Stop action → `RecordingActionReceiver`.
- **Both cameras (CameraX 1.5 concurrent camera):** `cameraLens == CameraLens.DUAL_SELECTOR` (100, no CameraX constant). `DualCamera.isSupported` = `FEATURE_CAMERA_CONCURRENT` + a front/back pair in `availableConcurrentCameraInfos`; the ViewModel checks once, `RecordingConfig.resolvedFor()` turns a saved dual choice into the back camera where unsupported, and `CameraLens.options()` hides *Both*. CameraX composes both cameras into ONE video only when the shared `UseCaseGroup` is exactly **Preview + VideoCapture** (read from the 1.5.3 bytecode; without Preview it binds them separately) — the service has no screen, so `DiscardingPreview` feeds the Preview into an `ImageReader` that closes every frame. `effectiveQuality` caps dual at 720p (Android's guarantee for concurrent streams). Confirmed on a Galaxy S24 with 3.2.0 (2026-09-27, by the user: both cameras in one video, screen off). The emulator has only one camera, so dual can only be tested on a device.
- `startRecording(audioAllowed)` drops audio when RECORD_AUDIO is missing (CameraX `withAudioEnabled()` would throw).
- `QualitySelector` has a fallback to lower quality.

### Data

- **Settings:** `PreferencesManager` (DataStore) → `AppSettings` (onboarding, `RecordingConfig`, `ThemeMode`, dynamic colour). Decoding lives in the pure `ConfigPrefs.decode` (unknown values fall back to defaults; unit-tested). `viewModel.settings` is `null` until DataStore answered — the splash screen stays up until then (`setKeepOnScreenCondition`).
- **Videos:** `VideoRepository` queries MediaStore (`RELATIVE_PATH LIKE 'Movies/XCam/%'`), observes changes with a `ContentObserver`, deletes via `contentResolver.delete` with a `createDeleteRequest` fallback (emitted as `UiEvent.ConfirmDelete`). Without `READ_MEDIA_VIDEO` MediaStore only returns files this install created — after a reinstall older recordings are hidden, so the Videos tab offers the optional media permission (`Permissions.media`). Sharing uses the content URIs directly (`util/Share.kt`, no FileProvider).
- `groupByDay` (pure, clock as parameter) builds the Today/Yesterday/date sections.

### Update notifications (since 3.1.0)

`data/update/`: `UpdateChecker` (HttpURLConnection, `x-cam.celox.io/latest.json` first — no GitHub rate limit — then `api.github.com/…/releases/latest`), pure `ReleaseParser` (org.json → Robolectric tests; only https notes links) and `AppVersions` (`isNewer`, `shouldNotify` = enabled && newer && newer than the last notified release), `UpdateNotifier` (channel `app_updates`, tap → product page, action → release notes; not recorded as sent when it could not be shown), `UpdateWatcher` (one check shared by app start [`checkIfDue`, 12 h throttle] and a periodic WorkManager `Worker` every 12 h with network; `schedule` uses KEEP). `XCamApplication` schedules or cancels by the setting; `AppSettings.updateChecks` (default on) + `knownUpdate` in DataStore; the Record screen shows `UpdateBanner` while `knownUpdate` is newer than `BuildConfig.VERSION_NAME`. This is the app's **only** network access (INTERNET permission since 3.1.0) — keep website/README/FAQ wording in sync if that ever changes. End-to-end test: install a debug build with a lower `versionName`, launch → notification from the live site.

### UI

- **Navigation / back (fixed in 3.1.0):** onboarding is **not** a NavHost destination — `XCamApp` switches between `OnboardingScreen` and `MainShell` with an `AnimatedContent` on `settings.onboardingCompleted`. As the graph's start it broke `navigateToTab`'s `popUpTo(findStartDestination())` after it was popped (tabs piled up on the back stack). Record is the fixed root: back = player → tab → Record → exit. The player's `onBack` is `dropUnlessResumed { … }` (a delete also makes the video disappear, which calls `onBack` a second time) and `openPlayer` only navigates from the resumed entry (double tap). Onboarding has a `BackHandler` for the previous page; `android:enableOnBackInvokedCallback="true"` gives predictive back, NavHost seeks the pop transitions. Verify with `adb shell input keyevent KEYCODE_BACK` per screen; `uiautomator dump` fails while a video plays — use screenshots there.
- `ui/XCamApp.kt`: outer `Scaffold` (`contentWindowInsets = WindowInsets(0)`) with an M3 Expressive `ShortNavigationBar` (Record · Videos · Settings, hidden on player/onboarding) and a `NavHost` padded + `consumeWindowInsets`. Tab changes go through `navigateToTab()`. Routes in `ui/navigation/Destination.kt`; player is `player/{id}` (MediaStore id).
- Theme: `ui/theme/Theme.kt` uses `MaterialExpressiveTheme` + `MotionScheme.expressive()`. `Color.kt` is **generated** by `tools/color-scheme.mjs` from seed `#E5484D` (accents from SchemeVibrant, neutrals from SchemeTonalSpot) — regenerate, don't hand-edit. `Shape.kt`/`Dimens.kt` = spacing/shape scales.
- Motion: all animation specs come from `MaterialTheme.motionScheme` (spatial springs move, effects springs fade — effects never overshoot). `ui/motion/`: `rememberReduceMotion()` (every custom animation checks it), `springEntrance`, `ScreenTransitions` (tab fade-through / child rise, ported from flipper), `MorphShape` + `rememberMorph` (graphics-shapes `Morph`). **Inside `transitionSpec` lambdas `MaterialTheme` is not accessible** — read `val motion = MaterialTheme.motionScheme` in the composable first.
- Signature component: `RecordHero` in `ui/record/RecordScreen.kt` morphs `MaterialShapes.Cookie9Sided` ↔ `Square`, with a `CircularWavyProgressIndicator` ring (determinate with a limit). Timer uses `RollingText` (per-character `AnimatedContent`, keyed from the right).
- `ui/components`: `SegmentedToggle`, `springPressed`, `ExpressiveLoadingIndicator`, `Haptics` (semantic constants; API-34 constants fall back to `CLOCK_TICK` on 33), `SectionCard`, `VideoThumbnail` (Coil `VideoFrameDecoder`, loader in `XCamApplication`).
- Strings: all UI text in `res/values/strings.xml` + `values-de`; `StringsParityTest` fails on a missing translation or mismatched format args. Per-app language via `xml/locales_config.xml`.

## App icon

Vector adaptive icon (`mipmap-anydpi-v26`) with a monochrome layer. Geometry (nine-lobed cookie with the camera + record dot as an evenOdd hole) is generated by `tools/icon_geometry.py` — the same path feeds launcher foreground/monochrome, `ic_app_mark`, `ic_splash_icon`, `ic_notification` and `tools/website/art/mark.svg`. `LauncherIconTest` pins the safe-zone radius (≤ 33) and foreground == monochrome geometry. (Renaming the folder to `mipmap-anydpi` broke AAPT resolution — keep `-v26`.)

## Tests

`app/src/test`: model/state/config tests, `RecordingRepositoryTest`, `ConfigPrefsTest`, `VideoGroupsTest` + `VideoFileTest` (Robolectric, need `android.net.Uri`), `TimeFormatTest`, `ThemeTokensTest` (WCAG AA on every on-colour pair, both schemes), `ScreenMotionTest`, `LauncherIconTest`, `StringsParityTest` (file-based, run from the module dir). Each was mutation-checked once when written.

## Website

`website/` is generated by the product-page kit (`~/claude/_templates/apps/product-page`): edit `tools/website/make_site_json.py` (5 languages side by side) → `python3 tools/website/make_site_json.py` → `build.py website/site.json website --hero docs/banner.jpg --screens docs/screenshots/mockups.png --icon tools/website/art/mark.svg --force` → `build.py --check website`. Hero = the README banner `docs/banner.jpg` (2914×1440 — keep `hero` width/height in make_site_json.py in step when it changes, and bump `asset_version` so the immutable `?v=` assets refresh); the screenshot strip `docs/screenshots/mockups.png` comes from `tools/mockups.py` (raw emulator captures, dark theme, EN). The server timer updates version/checksums from GitHub Releases by itself — no deploy per release. Deploy: `website/deploy.sh` (see `website/README.md`).

## Key Constants

Intent actions/extras, notification id, `RELATIVE_VIDEO_PATH` (`Movies/XCam/`, trailing slash matters for the LIKE query) in `util/Constants.kt`.

## README is tested

`app/src/test/java/io/celox/xcam/docs/` pins the docs to the build: `ReadmeBadgesTest` (version = `versionName`, unit-test badge = number of `@Test`, lines-of-code and test-code badges within ±10 % of the Kotlin line counts, SDK/JDK/ABI badges, APK size when a local release APK exists, PayPal link, website links, every `docs/` image exists), `ReleaseFactsTest` (signing certificate identical in release.yml, README, site.json and CHANGELOG; CHANGELOG has a dated section for `versionName`), `NetworkPolicyTest` (the only hosts in `src/main` are x-cam.celox.io and GitHub, no `http://`, no location/contacts permissions). After adding tests or code, update the badges in `README.md` — the suite tells you the right numbers.
