# Changelog

All notable changes to **XCam** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) from 3.0.0 on.

**This file is the release notes.** The release workflow cuts the `## [x.y.z]` section for the tag
being built out of this file and publishes it as the GitHub release text (`scripts/release-notes.sh`);
a tag without a section here fails the workflow.

## [Unreleased]

## [3.0.0] - 2026-09-26

A new design from the ground up — Material 3 Expressive with spring physics — and a recording state
the app no longer guesses.

### ⚠️ New signing key
- From 3.0.0 on, XCam is signed with its own release key (certificate SHA-256
  `78:F1:63:F0:BF:CF:F5:7E:2E:7D:32:12:DE:E8:AE:DB:1D:3B:FD:F4:8C:E4:C1:51:2B:F7:80:19:55:A1:CF:38`).
  Earlier releases were signed with a debug key, so **3.0.0 cannot be installed over 2.x**: uninstall
  the old version once. Your recordings stay in *Movies/XCam*; every later update installs normally.

### Added
- **Material 3 Expressive design**: spring-based motion everywhere (`MotionScheme.expressive`), a
  record button that *is* its state — a slowly turning nine-lobed shape that morphs into a stop square —
  a wavy progress ring while recording, rolling timer digits, staggered entrances, press physics and
  semantic haptics.
- **Light and dark theme** (System / Light / Dark) and optional **wallpaper colours** (Material You).
- **Navigation bar** with Record, Videos and Settings.
- **Stop automatically** after 5, 15, 30 or 60 minutes — the limit is enforced by CameraX itself and
  shown as a filling ring around the button.
- **Share** recordings (one or several) and **delete** them, with multi-select by long press.
- Recordings grouped by day (Today, Yesterday, date), with thumbnails, duration and size.
- The latest recording on the Record screen, one tap into the player.
- Player: share and delete, immersive full screen, playback position kept across rotation.
- **German translation**; the app follows the system language or the per-app language setting.
- A new app icon with a themed (monochrome) layer for Android 13+.
- A product website, **[x-cam.celox.io](https://x-cam.celox.io)** (also xcam.celox.io), linked in Settings → About.
- Optional access to your videos, offered in the Videos tab: after a reinstall, Android no longer lets
  an app see the recordings an earlier installation made — *Allow* lists them again.

### Changed
- The on-screen state now comes from the recording service's real CameraX events. Stopping from the
  notification, reaching the time limit or a camera failure now show up correctly in the app — before,
  the app assumed "recording" one second after the tap and "stopped" half a second after it.
- Settings (camera, quality, audio, time limit, theme) are saved and survive a restart.
- Recordings are read from MediaStore: faster (no per-file metadata scan) and the base for sharing.
- The notification counts up with the system chronometer instead of being rebuilt every second.
- If a camera cannot record the chosen quality, XCam falls back to the next lower one instead of
  failing.

### Removed
- The "stop at low battery" switch — it was never wired to anything.

### Fixed
- The grant-permission button no longer says "Start recording".
- A recording that could not start no longer shows as running.

## [2.0] - 2026-02-11

- Complete UI/UX overhaul, unit tests, CI pipeline.

## [1.6] - 2025-10-31

- Audio keeps recording while the screen is locked; smaller APK with custom vector icons.
