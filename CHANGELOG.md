# Changelog

All notable changes to **XCam** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) from 3.0.0 on.

**This file is the release notes.** The release workflow cuts the `## [x.y.z]` section for the tag
being built out of this file and publishes it as the GitHub release text (`scripts/release-notes.sh`);
a tag without a section here fails the workflow.

## [Unreleased]

## [3.1.0] - 2026-09-26

XCam now tells you when a new version is out, and back navigation behaves.

### Added
- **Update notifications.** Twice a day (only with a network) and at most every 12 hours on app start,
  XCam checks `x-cam.celox.io/latest.json` for a newer release, with GitHub as the fallback. A new
  version is announced once, in its own notification channel "App updates": a tap opens the website
  with the download, *What's new* opens the release notes. While it is newer than the installed build,
  the Record screen shows it too.
- A switch in **Settings → About → Update notifications** (on by default) turns the check off completely
  and cancels the background job.

### Fixed — back navigation
- **Back walked through the whole tab history after the first start.** Onboarding was the start of the
  navigation graph; once it was left, switching tabs never reset the stack, so back went Settings →
  Videos → Record → Videos … Onboarding now runs before the app instead of inside its navigation:
  back always goes player → tab → Record → out.
- **Deleting a video in the player** returned to Record instead of the list (the player closed twice);
  a quick double tap on the back arrow or on a recording could do the same. Navigation now only happens
  from the screen that is actually in front.
- **Back in the introduction** goes to the previous page instead of closing the app.
- **Predictive back** is on: the gesture shows the screen you are going back to (and, on Record, the
  home screen) before you let go.

### Changed
- XCam now has the `INTERNET` permission — used for nothing but this check. Recordings are never
  uploaded and no data about you is sent; the website, README and FAQ say so instead of
  "no internet access".
- Versions 3.0.0 and older have no update check, so they will not announce this release — this is the
  first version that does.

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
