# XCam - Background Video Recorder

Eine native Android-App für moderne Android-Geräte (Android 13+), die Videoaufnahmen mit ausgeschaltetem Display ermöglicht.

## Features

### Kernfunktionalität
- ✅ Videoaufnahme mit ausgeschaltetem Display
- ✅ Aufnahme läuft im Hintergrund als Foreground Service
- ✅ Permanente Notification während der Aufnahme
- ✅ Start/Stop über Notification oder App-Interface
- ✅ Wake Lock um System wach zu halten

### Aufnahme-Einstellungen
- **Kamera-Auswahl:** Front-/Rückkamera
- **Videoqualität:** 720p, 1080p, 4K
- **Audio:** Ein/Aus
- **Automatischer Stop:** Bei niedrigem Akku (<10%)

### UI Features
- Material 3 Design mit Jetpack Compose
- Übersichtliche Haupt-Ansicht mit Recording-Status
- Einstellungs-Screen für Konfiguration
- Video-Liste mit allen aufgenommenen Videos
- Video-Verwaltung (Anzeigen, Löschen)

## Technische Details

### Architektur
- **Sprache:** Kotlin
- **UI:** Jetpack Compose mit Material 3
- **Architektur:** MVVM Pattern
- **Kamera-API:** CameraX
- **Unterstützte Geräte:** Android 13+ (API 33+)
- **Architektur:** ARM64 (64-bit)

### Hauptkomponenten

#### 1. RecordingService
Foreground Service für Video-Aufnahme:
- CameraX Integration
- Wake Lock Management
- Notification Management
- Automatische Speicherung in MediaStore

**Datei:** `service/RecordingService.kt`

#### 2. RecordingViewModel
Zentrales ViewModel für:
- Recording State Management
- Konfiguration der Aufnahme-Parameter
- Video-Datei-Verwaltung

**Datei:** `viewmodel/RecordingViewModel.kt`

#### 3. UI Screens
- **MainScreen:** Haupt-Interface mit Start/Stop-Button
- **SettingsScreen:** Konfiguration aller Parameter
- **VideosScreen:** Liste aller aufgenommenen Videos

**Dateien:** `ui/screens/*.kt`

### Berechtigungen
Die App benötigt folgende Berechtigungen:
- `CAMERA` - Für Kamera-Zugriff
- `RECORD_AUDIO` - Für Audio-Aufnahme
- `FOREGROUND_SERVICE` - Für Background-Service
- `FOREGROUND_SERVICE_CAMERA` - Für Kamera-Service
- `POST_NOTIFICATIONS` - Für Notifications (Android 13+)
- `WAKE_LOCK` - Um Gerät wach zu halten

## Installation

### Voraussetzungen
- Android Studio Hedgehog oder neuer
- Android SDK 34
- Kotlin Plugin

### Build-Schritte

1. **Projekt klonen/öffnen:**
   ```bash
   cd /path/to/XCam
   ```

2. **Gradle Sync ausführen:**
   - In Android Studio: File → Sync Project with Gradle Files

3. **App bauen:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Auf Gerät installieren:**
   ```bash
   ./gradlew installDebug
   ```

   Oder über Android Studio: Run → Run 'app'

## Verwendung

### Erste Schritte
1. App öffnen
2. Berechtigungen gewähren (Kamera, Mikrofon, Notifications)
3. Gewünschte Einstellungen konfigurieren
4. "Start Recording" drücken

### Recording starten
- Über den "Start Recording" Button in der App
- Display kann während der Aufnahme ausgeschaltet werden
- Notification zeigt Recording-Status und Dauer

### Recording stoppen
- Über den "Stop" Button in der Notification
- Oder über die App

### Videos verwalten
- Über das Video-Icon in der Top Bar zur Video-Liste navigieren
- Videos löschen über das Papierkorb-Icon

## Projektstruktur

```
app/src/main/java/io/celox/xcam/
├── data/
│   └── model/
│       ├── RecordingConfig.kt    # Konfigurationsmodell
│       ├── RecordingState.kt     # State-Definitionen
│       └── VideoFile.kt          # Video-Datei-Modell
├── service/
│   └── RecordingService.kt       # Haupt-Service für Aufnahme
├── receiver/
│   └── RecordingActionReceiver.kt # Broadcast Receiver
├── viewmodel/
│   └── RecordingViewModel.kt     # Zentrale Business-Logik
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt         # Haupt-Screen
│   │   ├── SettingsScreen.kt     # Einstellungen
│   │   └── VideosScreen.kt       # Video-Liste
│   └── theme/                    # Material Theme
├── util/
│   ├── Constants.kt              # App-Konstanten
│   └── PermissionUtils.kt        # Permission-Helper
└── MainActivity.kt               # Entry Point
```

## Wichtige Hinweise

### Rechtlicher Hinweis
⚠️ **WICHTIG:** Diese App darf nur mit Einwilligung aller aufgenommenen Personen verwendet werden. Heimliche Aufnahmen können illegal sein und strafrechtlich verfolgt werden. Der Nutzer trägt die volle Verantwortung für die rechtmäßige Verwendung.

### Akku-Management
- Die App verwendet einen Wake Lock um das Gerät während der Aufnahme wach zu halten
- Automatischer Stop bei niedrigem Akkustand (10%)
- Empfehlung: Gerät während längerer Aufnahmen an Ladegerät anschließen

### Speicherort
Videos werden gespeichert unter:
```
/storage/emulated/0/Movies/XCam/
```

### Geräte-Optimierungen
Für optimale Performance:
1. App von Batterie-Optimierungen ausnehmen
2. Sicherstellen dass App im Hintergrund laufen darf
3. Einstellungen → Apps → XCam → Akku → Unbegrenzt

## Testing

### Manuelle Tests
1. **Basic Recording:**
   - Start recording → Display ausschalten → 1-2 Minuten warten → Display einschalten → Stop

2. **Long Recording:**
   - Recording >30 Minuten testen
   - Akkuverbrauch beobachten

3. **Permissions:**
   - App-Neuinstallation mit Permission-Flow testen

4. **Settings:**
   - Alle Einstellungen ändern und Recording testen
   - Front/Back Camera wechseln
   - Video-Qualität variieren

## Troubleshooting

### Recording startet nicht
- Prüfen ob alle Berechtigungen gewährt sind
- App-Cache leeren: Einstellungen → Apps → XCam → Speicher → Cache leeren

### Notification verschwindet
- System-Benachrichtigungen für XCam aktivieren
- App von Batterie-Optimierung ausnehmen

### Video-Qualität schlecht
- Höhere Qualität in Einstellungen wählen
- Ausreichend Speicherplatz sicherstellen
- Linse reinigen

## Performance

### Akkuverbrauch
- 720p: ~10-15% pro Stunde
- 1080p: ~15-20% pro Stunde
- 4K: ~25-35% pro Stunde

### Speicherplatz
- 720p: ~500 MB pro Stunde
- 1080p: ~1-2 GB pro Stunde
- 4K: ~4-8 GB pro Stunde

## Zukünftige Features (Optional)

- [ ] Motion Detection für automatischen Start
- [ ] Zeitplan für regelmäßige Aufnahmen
- [ ] Home Screen Widget
- [ ] Cloud-Backup-Optionen
- [ ] Video-Kompression
- [ ] Thumbnail-Generierung
- [ ] Video-Player in der App

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

## Support

Bei Problemen oder Fragen:
1. README durchlesen
2. Troubleshooting-Section überprüfen
3. Issue auf GitHub erstellen
4. Kontakt: martin.pfeffer@celox.io

## Entwickler

**Martin Pfeffer**
- E-Mail: martin.pfeffer@celox.io
- Webseite: https://celox.io

---

**Version:** 1.4
**Unterstützt:** Android 13+ (API 33+) auf ARM64-Geräten
**Datum:** 2025
