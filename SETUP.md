# XCam Setup Guide

## Schnellstart für Entwickler

### 1. Projekt in Android Studio öffnen

```bash
cd /path/to/XCam
```

Öffne das Projekt in Android Studio:
- File → Open → XCam-Verzeichnis auswählen

### 2. Gradle Sync

Android Studio führt automatisch einen Gradle Sync durch. Falls nicht:
- File → Sync Project with Gradle Files

### 3. SDK installieren (falls nötig)

Stelle sicher, dass folgende SDKs installiert sind:
- Android SDK Platform 34
- Android SDK Build-Tools 34.x
- Android Emulator (optional)

In Android Studio:
- Tools → SDK Manager → SDK Platforms → Android 14.0 (API 34) installieren

### 4. Gerät verbinden

**Option A: Physisches Gerät (empfohlen)**
1. USB-Debugging auf dem Gerät aktivieren:
   - Einstellungen → Über das Telefon → Software-Informationen
   - 7x auf "Build-Nummer" tippen
   - Zurück → Entwickleroptionen → USB-Debugging aktivieren
2. Gerät via USB verbinden
3. Debugging-Berechtigung auf Gerät akzeptieren

**Option B: Emulator**
1. Tools → Device Manager
2. Create Device → Phone → Pixel 7 Pro oder ähnlich
3. System Image: API 34 (Android 14)

### 5. App bauen und installieren

**In Android Studio:**
1. Run → Run 'app' (oder Shift+F10)
2. Gerät auswählen
3. Warten bis Build und Installation abgeschlossen

**Via Gradle (Terminal):**
```bash
# Debug Build
./gradlew assembleDebug

# Debug Build + Installation
./gradlew installDebug

# Optimierter Build mit Debug-Signatur (minifiziert mit R8)
./gradlew assembleReleaseDebug

# Release Build (benötigt Signing Config)
./gradlew assembleRelease
```

### 6. Tests ausführen

```bash
# Unit Tests
./gradlew test

# Instrumented Tests (benötigt verbundenes Gerät)
./gradlew connectedAndroidTest
```

## Erste Verwendung auf dem Gerät

### 1. App öffnen
- XCam-Icon (Stealth-Eye) im App-Drawer antippen
- Splash Screen wird kurz angezeigt

### 2. Onboarding durchlaufen
Beim ersten Start erscheint ein 4-seitiges Onboarding:
1. **Willkommen** — App-Beschreibung
2. **Kamera** — Kamera-Berechtigung gewähren
3. **Audio & Notifications** — Mikrofon- und Benachrichtigungs-Berechtigung gewähren
4. **Fertig** — "Get Started" drücken

Alle Berechtigungen müssen erlaubt werden für volle Funktionalität.

### 3. Batterie-Optimierung deaktivieren (wichtig!)
Für zuverlässige Background-Aufnahmen:

**Samsung One UI:**
1. Einstellungen → Apps → XCam
2. Akku → Unbegrenzt auswählen
3. Zurück → Im Hintergrund ausführen → Erlaubt

**Generisches Android:**
1. Einstellungen → Apps → XCam
2. App-Info → Akku → Unbeschränkt

### 4. Erste Aufnahme testen
1. In der App: "Start Recording" drücken
2. Display ausschalten (Power-Button)
3. 10-20 Sekunden warten
4. Display einschalten
5. Notification-Drawer öffnen → "Stop" drücken
6. Video-Icon in der App → Aufnahme sollte sichtbar sein
7. Thumbnail antippen → Video im Player ansehen

## Release erstellen und auf GitHub veröffentlichen

Der GitHub Actions Workflow baut automatisch ein APK und erstellt ein Release, wenn ein Git-Tag gepusht wird:

```bash
# Version in app/build.gradle.kts anpassen (versionCode + versionName)

# Tag erstellen und pushen
git tag -a v2.1 -m "v2.1 - Beschreibung der Änderungen"
git push origin v2.1
```

Das APK (`XCam-{version}-arm64-v8a.apk`) ist dann auf der [Releases-Seite](https://github.com/pepperonas/XCam/releases) zum Download verfügbar.

## Troubleshooting

### "Gradle Sync Failed"
```bash
./gradlew --refresh-dependencies
```

### "SDK Platform 34 not found"
- Tools → SDK Manager → SDK Platforms → Android 14.0 (API 34) installieren → Gradle Sync wiederholen

### App startet nicht auf Gerät
- USB-Debugging aktiviert?
- Gerät in Android Studio sichtbar? (Select Device Dropdown)
- `adb devices` im Terminal → Gerät sollte gelistet sein

### Build Error: "Unresolved reference"
- Build → Clean Project
- Build → Rebuild Project
- File → Invalidate Caches / Restart

### Recording funktioniert nicht
1. Berechtigungen überprüfen: Einstellungen → Apps → XCam → Berechtigungen
2. Batterie-Optimierung auf "Unbegrenzt" stellen
3. App-Logs prüfen:
   ```bash
   adb logcat | grep XCam
   ```

### Onboarding erneut anzeigen
- App-Daten löschen: Einstellungen → Apps → XCam → Speicher → Daten löschen

## Build-Varianten

| Variante | Beschreibung | Befehl |
|----------|-------------|--------|
| **debug** | Standard für Entwicklung, debuggbar | `./gradlew assembleDebug` |
| **releaseDebug** | Minifiziert mit R8, Debug-Signatur | `./gradlew assembleReleaseDebug` |
| **release** | Minifiziert mit R8, benötigt Signing Key | `./gradlew assembleRelease` |

APK-Ausgabepfade:
- Debug: `app/build/outputs/apk/debug/`
- ReleaseDebug: `app/build/outputs/apk/releaseDebug/`
- Release: `app/build/outputs/apk/release/`

## Signing Key erstellen (für Release Build)

```bash
keytool -genkey -v -keystore xcam-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias xcam
```

In `app/build.gradle.kts` Signing Config hinzufügen:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("xcam-release-key.jks")
            storePassword = "your_password"
            keyAlias = "xcam"
            keyPassword = "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## Nützliche ADB-Befehle

```bash
# App installieren
adb install app/build/outputs/apk/debug/app-arm64-v8a-debug.apk

# App deinstallieren
adb uninstall io.celox.xcam

# App starten
adb shell am start -n io.celox.xcam/.MainActivity

# App stoppen
adb shell am force-stop io.celox.xcam

# Berechtigungen gewähren (ohne UI)
adb shell pm grant io.celox.xcam android.permission.CAMERA
adb shell pm grant io.celox.xcam android.permission.RECORD_AUDIO

# App-Daten löschen (Onboarding wird erneut angezeigt)
adb shell pm clear io.celox.xcam

# Aufgenommene Videos auflisten
adb shell ls -la /sdcard/Movies/XCam/

# Video vom Gerät ziehen
adb pull /sdcard/Movies/XCam/VID_20250101_120000.mp4 .
```

## Logcat filtern

```bash
# Nur XCam-Logs
adb logcat -s XCam

# Recording Service Logs
adb logcat | grep RecordingService

# Alle App-Logs
adb logcat | grep "io.celox.xcam"

# Fehler und Warnungen
adb logcat *:E *:W
```

## Performance-Optimierung für Builds

In `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true
kotlin.incremental=true
```

Build-Cache leeren (bei Problemen):
```bash
./gradlew clean
```

---

**Bei Problemen:** README.md Troubleshooting-Section konsultieren
