<!--# block name="none" --><!--# endblock -->
# XCam — Record Video with the Screen Off (Android)

> Free, open-source (MIT) Android app that records video in the background with the screen off. Tap once, lock the phone, and a foreground service keeps recording; stop it in the app, from the notification or automatically after 5–60 minutes. Recordings are saved as MP4 to Movies/XCam. Its only network access is an optional update check against x-cam.celox.io. Android 13+, 64-bit ARM, distributed as a signed APK.

This is the Markdown version of https://x-cam.celox.io/ for agents and text tools. A short summary with every link lives at https://x-cam.celox.io/llms.txt.

## Download

- **Newest release:** https://x-cam.celox.io/download (picks the file for your platform; always the current release)
- **Current version:** <!--# include virtual="/ssi/version.txt" stub="none" --> · released <!--# include virtual="/ssi/date.txt" stub="none" -->
- **Release data as JSON:** https://x-cam.celox.io/latest.json
- **Requirements:** APK: Android 13+ · 64-bit ARM

Files in the current release:

<!--# include virtual="/ssi/files.md" stub="none" -->

## Features

- **Keeps recording, screen off** — Recording runs in a foreground service with its own wake lock. Lock the phone, put it in your pocket — the camera keeps going until you stop it.
- **One button that is its state** — Idle, the record button is a soft nine-lobed shape; tap it and it morphs into a stop square. What you see is what the camera reports — not a guess.
- **Stop from anywhere — or never** — End the recording in the app or straight from the notification, or let it stop by itself after 5, 15, 30 or 60 minutes. A ring around the button fills as the time runs.
- **Straight into your gallery** — Every recording is an MP4 in *Movies/XCam*. The app lists them by day with thumbnail, length and size — share or delete one or several at once.
- **Front, back — or both at once** — Record with the back or the front camera — or, on phones that allow it, with both: the back camera fills the video, the front one sits in a corner. 720p, 1080p or 4K (both cameras: up to 720p), with or without sound.
- **Nothing leaves your phone** — Recordings are never uploaded. The only connection XCam makes is a check for new versions at x-cam.celox.io, and you can switch it off. No account, no ads, no analytics — open source.

## Install

1. **Download the APK** — One file for every 64-bit phone with Android 13 or newer — there is nothing to pick.
2. **Allow the install** — Open the file. Android asks once to allow installs from your browser or file manager.
3. **Allow the camera, record** — A short introduction asks for the camera — microphone and notifications are optional. Then tap the button.

## Verify

<!--# include virtual="/ssi/checksums.md" stub="none" -->
- Signing certificate SHA-256: `78f163f0bfcff57e2e7d3212dee8aedb1d3bfdf48ce4c1512bf7801955a1cf38`
- Every release from 3.0.0 on is signed with this key. Compare with `apksigner verify --print-certs`.

## FAQ

**Is XCam free?** Yes. It is free and open source under the MIT licence, with no ads, no in-app purchases and no account.

**Does it really record with the screen off?** Yes. The recording runs in a camera-and-microphone foreground service that holds a wake lock, so it continues when the phone is locked. Android shows a notification while it runs — you can stop the recording from there.

**What do I need?** A phone with Android 13 or newer and a 64-bit ARM processor — practically every phone sold since 2022. The camera permission is required; microphone and notifications are optional.

**How do I update?** Download the newest APK from this page and install it over the app — every release from 3.0.0 on is signed with the same key. From 3.1.0 on, XCam tells you itself when a new version is out. Only from version 2.x you have to uninstall once.

**Where are my recordings?** In *Movies/XCam* on your phone, as MP4 files — visible in your gallery and in the app's Videos tab. Nothing is uploaded anywhere.

**Is the file genuine?** Compare the SHA-256 checksum and the signing certificate above with the downloaded file. The APK is built and signed by GitHub Actions from the public source code.

## Limits

- Android 13 or newer on 64-bit ARM only; no iOS, no desktop.
- No live preview, no photo mode, no cloud upload — XCam only records to the phone.
- Recording people without their consent is illegal in many countries; the user is responsible for lawful use.

## Links

- Source code: https://github.com/pepperonas/XCam
- Changelog: https://x-cam.celox.io/changelog.md
- Licence (MIT): https://github.com/pepperonas/XCam/blob/main/LICENSE
- Support the project: https://www.paypal.com/donate/?business=martin.pfeffer@celox.io&currency_code=EUR&item_name=XCam
- Author: Martin Pfeffer, https://celox.io — Imprint https://celox.io/impressum/ · Privacy https://celox.io/datenschutz/
