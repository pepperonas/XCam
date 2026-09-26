<div align="center">

<a href="https://x-cam.celox.io"><img src="docs/banner.jpg" alt="XCam — record video with the screen off. Open the website." width="100%"></a>

# XCam

**Record video with the screen off — a free, open-source Android app.**

<p>
  <a href="https://x-cam.celox.io"><img alt="Website: x-cam.celox.io" height="56" src="https://img.shields.io/badge/%F0%9F%8C%90_Website-x--cam.celox.io-E5484D?style=for-the-badge"></a>
  &nbsp;
  <a href="https://x-cam.celox.io/download"><img alt="Download the newest APK" height="56" src="https://img.shields.io/badge/%E2%AC%87%EF%B8%8F_Download-newest_APK-2E9E5B?style=for-the-badge&logo=android&logoColor=white"></a>
</p>

<h3>👉 <a href="https://x-cam.celox.io">x-cam.celox.io</a> — features, install guide, FAQ and always the newest APK</h3>

[![version](https://img.shields.io/github/v/release/pepperonas/XCam?style=for-the-badge&color=E5484D&logo=android&logoColor=white&label=version)](https://github.com/pepperonas/XCam/releases/latest)
[![unit tests](https://img.shields.io/badge/unit%20tests-50-2E9E5B?style=for-the-badge&logo=junit5&logoColor=white)](app/src/test)
[![lines of code](https://img.shields.io/badge/lines%20of%20code-4.4k-4B6BDF?style=for-the-badge&logo=kotlin&logoColor=white)](app/src/main/java)

[![CI](https://img.shields.io/github/actions/workflow/status/pepperonas/XCam/ci.yml?branch=main&label=build&logo=github)](https://github.com/pepperonas/XCam/actions/workflows/ci.yml)
[![Release workflow](https://img.shields.io/github/actions/workflow/status/pepperonas/XCam/release.yml?label=release&logo=githubactions)](https://github.com/pepperonas/XCam/actions/workflows/release.yml)
[![APK size](https://img.shields.io/badge/APK-23.4%20MB-4B6BDF?logo=android&logoColor=white)](#-download)
[![ABI](https://img.shields.io/badge/ABI-arm64--v8a%20only-4B6BDF?logo=arm&logoColor=white)](#-download)
[![languages](https://img.shields.io/badge/languages-EN%20%C2%B7%20DE-0E7C86?logo=googletranslate&logoColor=white)](app/src/main/res)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.11-3DDC84?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3 Expressive](https://img.shields.io/badge/Material%203-Expressive-E5484D?logo=materialdesign&logoColor=white)](https://m3.material.io/blog/m3-expressive-motion-theming)
[![min SDK](https://img.shields.io/badge/min%20SDK-33-blue?logo=android&logoColor=white)](app/build.gradle.kts)
[![target SDK](https://img.shields.io/badge/target%20SDK-35-blue?logo=android&logoColor=white)](app/build.gradle.kts)
[![Keep a Changelog](https://img.shields.io/badge/changelog-Keep%20a%20Changelog-E05735?logo=keepachangelog&logoColor=white)](CHANGELOG.md)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow)](LICENSE)

</div>

---

<p align="center"><img src="docs/screenshots.jpg" alt="Four screens: the record button in the light theme, recording with the time-limit ring, the recordings list and the German settings" width="100%"></p>

## ✨ Features

- **Keeps recording with the screen off** — a camera + microphone foreground service with its own wake lock.
- **One button that is its state** — idle it is a slowly turning nine-lobed shape; tap it and it morphs
  (on a spring) into a stop square. The state on screen comes from CameraX's own events, not a guess.
- **Stop from anywhere** — in the app, from the notification, or automatically after 5, 15, 30 or 60
  minutes; a wavy ring around the button shows the time running.
- **Straight into your gallery** — MP4 files in `Movies/XCam`, listed by day with thumbnail, length and
  size; share or delete one or several at once; built-in player.
- **Your camera, your quality** — back or front, 720p / 1080p / 4K, with or without sound; steps down
  instead of failing if a lens cannot do the chosen size. Settings are saved.
- **Nothing leaves your phone** — XCam requests no internet permission. No account, no ads, no analytics.
- **Material 3 Expressive** — spring physics everywhere, light and dark theme, optional wallpaper colours,
  English and German.

## ⬇️ Download

**[x-cam.celox.io](https://x-cam.celox.io)** always offers the newest signed APK with its SHA-256
checksum — or grab it from [GitHub Releases](https://github.com/pepperonas/XCam/releases/latest).

- Android 13 or newer, 64-bit ARM.
- Signing certificate SHA-256:
  `78f163f0bfcff57e2e7d3212dee8aedb1d3bfdf48ce4c1512bf7801955a1cf38`
  (check with `apksigner verify --print-certs xcam-v3.0.0.apk`).
- **Coming from 2.x?** Version 3.0.0 is signed with a new key, so uninstall the old app once. Your
  recordings stay in `Movies/XCam`; in the Videos tab, *Allow* lets XCam list them again.

## 🛠️ Build

```bash
./gradlew assembleDebug           # debug APK
./gradlew testDebugUnitTest       # unit tests
./gradlew lintDebug               # Android lint (CI: 0 errors)
./gradlew assembleRelease         # signed release APK (needs keystore.properties + release.jks)
```

JDK 17. The Compose/Material 3 versions are pinned (no BOM) because the Expressive APIs live in the
material3 1.5.0 alpha line — see `gradle/libs.versions.toml`. R8 needs a 6 GB Gradle heap
(`gradle.properties`).

## 🚀 Releasing

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Add a `## [x.y.z] - date` section to [CHANGELOG.md](CHANGELOG.md) — it becomes the release notes.
3. `git tag vX.Y.Z && git push origin vX.Y.Z` — the release workflow runs the tests, builds and signs the
   APK, verifies the certificate and publishes it. The website picks up the new release within 15 minutes.

## 🧱 Architecture

MVVM with Jetpack Compose, no DI framework. `RecordingService` (a `LifecycleService`) is the only writer
of `RecordingRepository`, a process-wide `StateFlow` the UI observes; settings live in DataStore,
recordings in MediaStore. Details: [CLAUDE.md](CLAUDE.md) and [ARCHITECTURE.md](ARCHITECTURE.md).

## ⚖️ Use responsibly

Recording people without their consent is illegal in many countries. XCam is meant for your own security
and documentation — you are responsible for complying with the law where you record.

## 📄 License

[MIT](LICENSE) © Martin Pfeffer · [celox.io](https://celox.io)
