---
name: get-x-cam
description: Download, verify and install the newest XCam APK — the free, open-source Android app that records video with the screen off. Use when someone asks for the app, its latest version, a download link, or how to check the APK is genuine.
license: MIT
---

# Get XCam

Free, open-source (MIT) Android app that records video in the background with the screen off. Tap once, lock the phone, and a foreground service keeps recording; stop it in the app, from the notification or automatically after 5–60 minutes. Recordings are saved as MP4 to Movies/XCam. The app requests no internet permission. Android 13+, 64-bit ARM, distributed as a signed APK.

## 1. Find the newest release

`GET https://x-cam.celox.io/latest.json` returns `version`, `published`, `notes` and `assets[]`, each with `target`,
`name`, `url`, `size` (bytes) and `sha256`. It is refreshed from GitHub Releases every 15 minutes. On the
page itself, browsers with WebMCP expose the same data as the tools `get_latest_release`,
`get_download_url` and `get_checksums`.

## 2. Download

- Stable link, always the newest file for the visitor's platform: <https://x-cam.celox.io/download>
- APK: <https://x-cam.celox.io/download/android> — Android 13+ · 64-bit ARM

## 3. Verify

- The file's SHA-256 must equal the matching `assets[].sha256` in `latest.json`.
- Signing certificate SHA-256: `78f163f0bfcff57e2e7d3212dee8aedb1d3bfdf48ce4c1512bf7801955a1cf38`
- Every release from 3.0.0 on is signed with this key. Compare with `apksigner verify --print-certs`.

## 4. Install

1. One file for every 64-bit phone with Android 13 or newer — there is nothing to pick.
2. Open the file. Android asks once to allow installs from your browser or file manager.
3. A short introduction asks for the camera — microphone and notifications are optional. Then tap the button.

## Limits

- Android 13 or newer on 64-bit ARM only; no iOS, no desktop.
- No live preview, no photo mode, no cloud upload — XCam only records to the phone.
- Recording people without their consent is illegal in many countries; the user is responsible for lawful use.

More: [product page](https://x-cam.celox.io/) · [Markdown version](https://x-cam.celox.io/index.md) · [changelog](https://x-cam.celox.io/changelog.md) · [source](https://github.com/pepperonas/XCam)
