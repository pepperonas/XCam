# XCam - Background Video Recorder

Eine native Android-App für moderne Android-Geräte (Android 13+), die Videoaufnahmen mit ausgeschaltetem Display ermöglicht.

## Download

Die neueste Version als installierbare APK gibt es auf der [**Releases-Seite**](https://github.com/pepperonas/XCam/releases).

## Features

### Kernfunktionalität
- Videoaufnahme mit ausgeschaltetem Display
- Aufnahme läuft im Hintergrund als Foreground Service
- Permanente Notification mit Stop-Button und Dauer-Anzeige
- Start/Stop über Notification oder App-Interface
- Wake Lock um das System während der Aufnahme wach zu halten

### Aufnahme-Einstellungen
- **Kamera-Auswahl:** Front- oder Rückkamera
- **Videoqualität:** 720p, 1080p, 4K
- **Audio:** Ein/Aus
- **Automatischer Stop:** Bei niedrigem Akku (<10%)

### UI & Design
- **Premium Dark & Amber Theme** mit glassmorphen Karten und dezenten Gradienten
- Material Design 3 mit Jetpack Compose
- **Inter Font** in vier Schriftstärken (Regular, Medium, SemiBold, Bold)
- **Animierter Recording-Indikator** mit pulsierendem Glow-Effekt und rotierendem Bogen
- **Konzentrische Ringe** hinter dem Recording-Indikator
- **Elapsed-Time-Display** (Monospace) bei laufender Aufnahme
- Smooth Navigation-Transitions zwischen allen Screens (Slide + Fade, 300ms)
- Haptic Feedback bei allen Interaktionen

### Splash Screen & Onboarding
- **Nativer Splash Screen** (Android 12+ API) mit Amber-Eye-Icon
- **4-Seiten-Onboarding** beim ersten Start:
  1. Willkommen mit App-Beschreibung
  2. Kamera-Berechtigung anfordern
  3. Audio- und Benachrichtigungs-Berechtigung anfordern
  4. Fertig-Screen mit "Get Started"
- Automatisches Weiterblättern nach erteilter Berechtigung
- Onboarding-Status wird in DataStore persistiert

### In-App Video Player
- **Vollbild-Wiedergabe** mit Media3 ExoPlayer
- Auto-hiding Controls (ein/aus per Tap, automatisch nach 3 Sekunden)
- Play/Pause, Seek Bar, verstrichene/Gesamtzeit
- Share-Button

### Video-Verwaltung
- **Echte Video-Thumbnails** via Coil + VideoFrameDecoder
- Play-Icon-Overlay und Dauer-Badge auf Thumbnails
- Tap auf Video öffnet den Player
- Löschen mit Bestätigungs-Dialog

### App-Icon
- **Stealth-Eye Adaptive Icon** (stilisiertes Auge mit Kameralinsen-Iris)
- Amber/Orange auf dunklem Hintergrund
- Monochromes Notification-Icon

## Technische Details

### Architektur
- **Sprache:** Kotlin
- **UI:** Jetpack Compose mit Material 3
- **Architektur-Pattern:** MVVM
- **Kamera-API:** CameraX (VideoCapture/Recorder)
- **Video-Wiedergabe:** Media3 ExoPlayer
- **Bild-Laden:** Coil mit VideoFrameDecoder
- **Persistenz:** DataStore Preferences
- **Unterstützte Geräte:** Android 13+ (API 33+), ARM64

### Hauptkomponenten

#### RecordingService
Foreground Service (`LifecycleService`) für die Videoaufnahme:
- CameraX Integration mit VideoCapture/Recorder
- Wake Lock Management (bis 24h)
- Foreground Notification mit Stop-Button und Timer
- Automatische Speicherung in MediaStore (`/Movies/XCam/`)
- Monochromes Notification-Icon

**Datei:** `service/RecordingService.kt`

#### RecordingViewModel
Zentrales AndroidViewModel, geteilt zwischen allen Screens:
- `recordingState: StateFlow<RecordingState>` (Idle, Starting, Recording, Stopping, Error)
- `recordingConfig: StateFlow<RecordingConfig>` (Kamera, Qualität, Audio, Dauer, Akku)
- `videoFiles: StateFlow<List<VideoFile>>` (mit Dauer via MediaMetadataRetriever)
- `hasCompletedOnboarding: StateFlow<Boolean>` (Onboarding-Status)
- Video-Dauer-Extraktion via `MediaMetadataRetriever`

**Datei:** `viewmodel/RecordingViewModel.kt`

#### PreferencesManager
DataStore-Wrapper für persistente Einstellungen:
- Onboarding-Completion-Status

**Datei:** `data/PreferencesManager.kt`

#### UI Screens
- **OnboardingScreen:** 4-Seiten HorizontalPager mit Berechtigungs-Flow
- **MainScreen:** Haupt-Interface mit animiertem Recording-Indikator und Config-Card
- **SettingsScreen:** Glassmorphe Karten, About-Sektion mit Versions-Info
- **VideosScreen:** Thumbnail-Liste mit Dauer-Badges und Tap-to-Play
- **VideoPlayerScreen:** Vollbild ExoPlayer mit Auto-hiding Controls

**Dateien:** `ui/screens/*.kt`

#### Custom Icons
Eigene Vector-Icons statt Material Extended Icons Library (~10-15 MB APK-Ersparnis):
- Recording/Playback: Play, Pause, Stop, Videocam, FiberManualRecord
- Navigation: ArrowBack, Settings, VideoLibrary, ChevronRight
- Onboarding: CameraLens, Microphone, NotificationBell, Shield
- Sonstige: Delete, Warning, Share, Fullscreen, Info, VideoFile

**Datei:** `ui/icons/CustomIcons.kt`

### Berechtigungen
- `CAMERA` — Kamera-Zugriff
- `RECORD_AUDIO` — Audio-Aufnahme
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_CAMERA` / `FOREGROUND_SERVICE_MICROPHONE` — Background Service
- `POST_NOTIFICATIONS` — Benachrichtigungen (Android 13+)
- `WAKE_LOCK` — Gerät wach halten

## Installation

### Voraussetzungen
- Android Studio Hedgehog oder neuer
- Android SDK 34
- JDK 17

### Build

```bash
# Debug Build
./gradlew assembleDebug

# Optimierter Build mit Debug-Signatur (R8 minified)
./gradlew assembleReleaseDebug

# Release Build (benötigt Signing Config)
./gradlew assembleRelease

# Auf verbundenes Gerät installieren
./gradlew installDebug
```

### Release erstellen

Ein Git-Tag im Format `v*` (z.B. `v2.0`) pushen — der GitHub Actions Workflow baut automatisch ein APK und erstellt ein Release:

```bash
git tag -a v2.1 -m "v2.1 - Beschreibung"
git push origin v2.1
```

Das APK ist dann unter [Releases](https://github.com/pepperonas/XCam/releases) zum Download verfügbar.

## Verwendung

### Erster Start
1. App öffnen — Splash Screen wird angezeigt
2. Onboarding durchlaufen und Berechtigungen gewähren (Kamera, Mikrofon, Benachrichtigungen)
3. "Get Started" drücken

### Aufnahme starten
1. "Start Recording" auf dem Hauptbildschirm drücken
2. Display kann während der Aufnahme ausgeschaltet werden
3. Notification zeigt Recording-Status und Dauer

### Aufnahme stoppen
- Über den "Stop"-Button in der Notification
- Oder über die App

### Videos ansehen
1. Video-Icon in der Top Bar drücken
2. Video-Thumbnail antippen → Vollbild-Player öffnet sich
3. Controls: Tap zum Ein-/Ausblenden, Seek Bar zum Springen

### Videos verwalten
- Löschen über das Papierkorb-Icon mit Bestätigungs-Dialog

## Projektstruktur

```
app/src/main/java/io/celox/xcam/
├── data/
│   ├── PreferencesManager.kt        # DataStore Wrapper
│   └── model/
│       ├── RecordingConfig.kt        # Aufnahme-Konfiguration
│       ├── RecordingState.kt         # State-Definitionen (Sealed Class)
│       └── VideoFile.kt              # Video-Datei-Modell mit formattedDuration
├── service/
│   └── RecordingService.kt           # Foreground Service für Aufnahme
├── receiver/
│   └── RecordingActionReceiver.kt    # Broadcast Receiver für Notification
├── viewmodel/
│   └── RecordingViewModel.kt         # Zentrales ViewModel mit Onboarding + Thumbnails
├── ui/
│   ├── components/
│   │   └── Components.kt             # GlassmorphicCard, AnimatedRecordButton, ShimmerEffect, etc.
│   ├── icons/
│   │   └── CustomIcons.kt            # ~20 Custom Vector Icons
│   ├── screens/
│   │   ├── OnboardingScreen.kt       # 4-Seiten Onboarding mit Berechtigungs-Flow
│   │   ├── MainScreen.kt             # Hauptbildschirm mit Ambient-Gradient
│   │   ├── SettingsScreen.kt         # Glassmorphe Einstellungen + About
│   │   ├── VideosScreen.kt           # Thumbnail-Liste mit Tap-to-Play
│   │   └── VideoPlayerScreen.kt      # Vollbild ExoPlayer
│   └── theme/
│       ├── Color.kt                  # Dark & Amber Farbpalette
│       ├── Theme.kt                  # Material 3 Theme (Dark + Light)
│       └── Type.kt                   # Inter Font Typography System
├── util/
│   ├── Constants.kt                  # App-Konstanten
│   └── PermissionUtils.kt            # Permission-Helper
└── MainActivity.kt                   # Entry Point mit Splash, Navigation, Permissions

app/src/main/res/
├── drawable/
│   ├── ic_launcher_foreground.xml    # Adaptive Icon Foreground (Stealth Eye)
│   ├── ic_launcher_background.xml    # Adaptive Icon Background (Dark)
│   ├── ic_notification.xml           # Monochromes Notification-Icon
│   └── ic_splash_icon.xml            # Splash Screen Icon
├── font/
│   ├── inter_regular.ttf
│   ├── inter_medium.ttf
│   ├── inter_semibold.ttf
│   └── inter_bold.ttf
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml               # Adaptive Icon Config
│   └── ic_launcher_round.xml         # Adaptive Round Icon Config
├── mipmap-*/
│   └── ic_launcher.png               # Legacy Fallback Icons
└── values/
    ├── colors.xml                    # XML-Farbwerte
    ├── strings.xml                   # Alle UI-Strings (Englisch)
    └── themes.xml                    # Dark Theme + Splash Theme
```

## Wichtige Hinweise

### Rechtlicher Hinweis
**WICHTIG:** Diese App darf nur mit Einwilligung aller aufgenommenen Personen verwendet werden. Heimliche Aufnahmen können illegal sein und strafrechtlich verfolgt werden. Der Nutzer trägt die volle Verantwortung für die rechtmäßige Verwendung.

### Akku-Management
- Die App verwendet einen Wake Lock während der Aufnahme
- Automatischer Stop bei niedrigem Akkustand (10%)
- Empfehlung: Gerät während längerer Aufnahmen an Ladegerät anschließen

### Speicherort
Videos werden gespeichert unter:
```
/storage/emulated/0/Movies/XCam/
```

### Geräte-Optimierungen (Samsung)
Für zuverlässige Background-Aufnahmen:
1. Einstellungen → Apps → XCam → Akku → Unbegrenzt
2. Sicherstellen, dass App im Hintergrund laufen darf

## Performance

### Akkuverbrauch (ca.)
| Qualität | Verbrauch/Stunde |
|----------|-----------------|
| 720p     | ~10-15%         |
| 1080p    | ~15-20%         |
| 4K       | ~25-35%         |

### Speicherplatz (ca.)
| Qualität | Größe/Stunde |
|----------|-------------|
| 720p     | ~500 MB     |
| 1080p    | ~1-2 GB     |
| 4K       | ~4-8 GB     |

## Troubleshooting

### Aufnahme startet nicht
- Alle Berechtigungen gewährt? (Einstellungen → Apps → XCam → Berechtigungen)
- App-Cache leeren: Einstellungen → Apps → XCam → Speicher → Cache leeren

### Notification verschwindet
- System-Benachrichtigungen für XCam aktivieren
- App von Batterie-Optimierung ausnehmen

### Onboarding erneut anzeigen
- App-Daten löschen: Einstellungen → Apps → XCam → Speicher → Daten löschen

## Lizenz

MIT License

Copyright (c) 2025 Martin Pfeffer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Entwickler

**Martin Pfeffer**
- E-Mail: martin.pfeffer@celox.io
- Webseite: https://celox.io

---

**Version:** 2.0
**Unterstützt:** Android 13+ (API 33+) auf ARM64-Geräten
