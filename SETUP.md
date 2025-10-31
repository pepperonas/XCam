# XCam Setup Guide

## Schnellstart für Entwickler

### 1. Projekt in Android Studio öffnen

```bash
cd /Users/martin/AndroidStudioProjects/XCam
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
1. USB-Debugging auf dem Samsung S24 aktivieren:
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

# Release Build (signiert)
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
- XCam-Icon im App-Drawer antippen

### 2. Berechtigungen gewähren
Die App fordert folgende Berechtigungen an:
- ✅ Kamera
- ✅ Mikrofon
- ✅ Benachrichtigungen

Alle müssen erlaubt werden für volle Funktionalität.

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

## Troubleshooting

### "Gradle Sync Failed"
```bash
# Terminal öffnen im Projekt-Verzeichnis
./gradlew --refresh-dependencies
```

### "SDK Platform 34 not found"
- Tools → SDK Manager
- SDK Platforms → Android 14.0 (API 34) installieren
- Apply → OK
- Gradle Sync wiederholen

### App startet nicht auf Gerät
- USB-Debugging aktiviert?
- Gerät in Android Studio sichtbar? (Select Device Dropdown)
- `adb devices` im Terminal ausführen → Gerät sollte listed sein

### Build Error: "Unresolved reference"
- Build → Clean Project
- Build → Rebuild Project
- File → Invalidate Caches / Restart

### Recording funktioniert nicht
1. Berechtigungen überprüfen:
   - Einstellungen → Apps → XCam → Berechtigungen
   - Kamera, Mikrofon, Benachrichtigungen sollten erlaubt sein

2. Batterie-Optimierung:
   - Muss auf "Unbegrenzt" stehen

3. App-Logs überprüfen:
   ```bash
   adb logcat | grep XCam
   ```

### Video-Dateien nicht sichtbar
- Einstellungen → Apps → XCam → Berechtigungen
- Dateien und Medien → Erlauben
- Geräte-Gallerie öffnen → Alben → XCam

## Build-Varianten

### Debug Build
- Standard für Entwicklung
- Debuggbar
- Größer (enthält Debug-Symbole)

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
- Optimiert für Produktion
- Kleiner und schneller
- Benötigt Signing Key

```bash
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

## Signing Key erstellen (für Release Build)

```bash
keytool -genkey -v -keystore xcam-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias xcam
```

In `app/build.gradle.kts` signing config hinzufügen:

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
            // ...
        }
    }
}
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

## Nützliche ADB-Befehle

```bash
# App installieren
adb install app/build/outputs/apk/debug/app-debug.apk

# App deinstallieren
adb uninstall io.celox.xcam

# App starten
adb shell am start -n io.celox.xcam/.MainActivity

# App stoppen
adb shell am force-stop io.celox.xcam

# Berechtigungen gewähren (ohne UI)
adb shell pm grant io.celox.xcam android.permission.CAMERA
adb shell pm grant io.celox.xcam android.permission.RECORD_AUDIO

# App-Daten löschen
adb shell pm clear io.celox.xcam

# Aufgenommene Videos auflisten
adb shell ls -la /sdcard/Movies/XCam/

# Video vom Gerät ziehen
adb pull /sdcard/Movies/XCam/VID_20240101_120000.mp4 .
```

## Performance-Optimierung

### Für schnellere Builds
In `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true
kotlin.incremental=true
```

### Build-Cache leeren (bei Problemen)
```bash
./gradlew clean
rm -rf ~/.gradle/caches/
```

## Entwicklungs-Workflow

### 1. Feature entwickeln
```bash
git checkout -b feature/neue-funktion
# Code schreiben...
```

### 2. Testen
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### 3. App auf Gerät testen
```bash
./gradlew installDebug
# Manuell auf Gerät testen
```

### 4. Build prüfen
```bash
./gradlew build
```

### 5. Commit und Push
```bash
git add .
git commit -m "Add neue Funktion"
git push origin feature/neue-funktion
```

## Produktions-Deployment

### 1. Version erhöhen
In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2  // Erhöhen
    versionName = "1.1"  // Erhöhen
}
```

### 2. Release Build erstellen
```bash
./gradlew assembleRelease
```

### 3. APK signieren und optimieren
```bash
# Bereits signiert durch signingConfig
# APK liegt in: app/build/outputs/apk/release/app-release.apk
```

### 4. Auf Google Play hochladen
- Play Console → XCam → Releases → Production
- APK hochladen
- Release Notes hinzufügen
- Review & Release

## Support-Informationen

### System-Info ausgeben
```bash
adb shell getprop ro.build.version.release  # Android Version
adb shell getprop ro.product.model          # Gerätemodell
adb shell getprop ro.build.version.sdk      # SDK Version
```

### App-Info
```bash
adb shell dumpsys package io.celox.xcam | grep version
```

## Häufige Fehler

### OutOfMemoryError beim Build
→ `gradle.properties`: Heap Size erhöhen auf 4096m oder mehr

### "Execution failed for task ':app:mergeDebugResources'"
→ Clean Project + Rebuild

### "Android SDK not found"
→ SDK-Pfad in `local.properties` setzen:
```
sdk.dir=/Users/martin/Library/Android/sdk
```

### CameraX Initialization Failed
→ Berechtigungen prüfen + App neu starten

---

**Bei Problemen:** README.md Troubleshooting-Section konsultieren
