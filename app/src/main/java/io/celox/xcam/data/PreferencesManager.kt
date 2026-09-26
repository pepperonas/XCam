package io.celox.xcam.data

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.ThemeMode
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.data.update.AppUpdate
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xcam_preferences")

/** Everything the user can set, as one value. */
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val recording: RecordingConfig = RecordingConfig(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    /** Check x-cam.celox.io for new releases (the app's only network access). On by default. */
    val updateChecks: Boolean = true,
    /** The newest release the last check found — shown in the app while it is newer than this build. */
    val knownUpdate: AppUpdate? = null,
)

/**
 * Pure mapping between [AppSettings] and raw preference values, kept apart from DataStore so the
 * round trip can be unit-tested. Unknown or out-of-range stored values fall back to defaults — a
 * renamed enum must never crash the app on start.
 */
object ConfigPrefs {
    fun decode(
        onboarding: Boolean?,
        lens: Int?,
        quality: String?,
        audio: Boolean?,
        maxMinutes: Int?,
        theme: String?,
        dynamic: Boolean?,
        updateChecks: Boolean? = null,
        knownVersion: String? = null,
        knownNotes: String? = null,
    ): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            onboardingCompleted = onboarding ?: defaults.onboardingCompleted,
            recording =
            RecordingConfig(
                cameraLens =
                lens?.takeIf { it == CameraSelector.LENS_FACING_BACK || it == CameraSelector.LENS_FACING_FRONT }
                    ?: defaults.recording.cameraLens,
                videoQuality = VideoQuality.fromName(quality),
                enableAudio = audio ?: defaults.recording.enableAudio,
                maxDurationMinutes =
                maxMinutes?.takeIf { it in RecordingConfig.MAX_DURATION_OPTIONS }
                    ?: defaults.recording.maxDurationMinutes,
            ),
            themeMode = ThemeMode.entries.firstOrNull { it.name == theme } ?: defaults.themeMode,
            dynamicColor = dynamic ?: defaults.dynamicColor,
            updateChecks = updateChecks ?: defaults.updateChecks,
            knownUpdate =
            if (!knownVersion.isNullOrBlank() && knownNotes?.startsWith("https://") == true) {
                AppUpdate(knownVersion, knownNotes)
            } else {
                null
            },
        )
    }
}

class PreferencesManager(private val context: Context) {
    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CAMERA_LENS = intPreferencesKey("camera_lens")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val ENABLE_AUDIO = booleanPreferencesKey("enable_audio")
        val MAX_DURATION = intPreferencesKey("max_duration_minutes")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val UPDATE_CHECKS = booleanPreferencesKey("update_checks")
        val KNOWN_UPDATE_VERSION = stringPreferencesKey("known_update_version")
        val KNOWN_UPDATE_NOTES = stringPreferencesKey("known_update_notes")
        val NOTIFIED_UPDATE_VERSION = stringPreferencesKey("notified_update_version")
        val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check_ms")
    }

    val settings: Flow<AppSettings> =
        context.dataStore.data.map { p ->
            ConfigPrefs.decode(
                onboarding = p[ONBOARDING_COMPLETED],
                lens = p[CAMERA_LENS],
                quality = p[VIDEO_QUALITY],
                audio = p[ENABLE_AUDIO],
                maxMinutes = p[MAX_DURATION],
                theme = p[THEME_MODE],
                dynamic = p[DYNAMIC_COLOR],
                updateChecks = p[UPDATE_CHECKS],
                knownVersion = p[KNOWN_UPDATE_VERSION],
                knownNotes = p[KNOWN_UPDATE_NOTES],
            )
        }

    suspend fun setOnboardingCompleted() = context.dataStore.edit { it[ONBOARDING_COMPLETED] = true }

    suspend fun setRecordingConfig(config: RecordingConfig) =
        context.dataStore.edit {
            it[CAMERA_LENS] = config.cameraLens
            it[VIDEO_QUALITY] = config.videoQuality.name
            it[ENABLE_AUDIO] = config.enableAudio
            it[MAX_DURATION] = config.maxDurationMinutes
        }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[THEME_MODE] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[DYNAMIC_COLOR] = enabled }

    suspend fun setUpdateChecks(enabled: Boolean) = context.dataStore.edit { it[UPDATE_CHECKS] = enabled }

    suspend fun setKnownUpdate(update: AppUpdate) =
        context.dataStore.edit {
            it[KNOWN_UPDATE_VERSION] = update.version
            it[KNOWN_UPDATE_NOTES] = update.notesUrl
        }

    val notifiedUpdateVersion: Flow<String?> = context.dataStore.data.map { it[NOTIFIED_UPDATE_VERSION] }

    suspend fun setNotifiedUpdateVersion(version: String) =
        context.dataStore.edit { it[NOTIFIED_UPDATE_VERSION] = version }

    val lastUpdateCheckMs: Flow<Long> = context.dataStore.data.map { it[LAST_UPDATE_CHECK] ?: 0L }

    suspend fun setLastUpdateCheckMs(ms: Long) = context.dataStore.edit { it[LAST_UPDATE_CHECK] = ms }
}
