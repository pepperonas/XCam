package io.celox.xcam.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import io.celox.xcam.BuildConfig
import io.celox.xcam.R
import io.celox.xcam.data.AppSettings
import io.celox.xcam.data.model.CameraLens
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.ThemeMode
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.ui.components.RowLabel
import io.celox.xcam.ui.components.SectionCard
import io.celox.xcam.ui.components.SectionHeader
import io.celox.xcam.ui.components.SegmentedToggle
import io.celox.xcam.ui.components.rememberHaptics
import io.celox.xcam.ui.icons.XIcons
import io.celox.xcam.ui.motion.springEntrance
import io.celox.xcam.ui.theme.Spacing

@Composable
fun SettingsScreen(
    settings: AppSettings,
    locked: Boolean,
    onLensChange: (Int) -> Unit,
    onQualityChange: (VideoQuality) -> Unit,
    onAudioChange: (Boolean) -> Unit,
    onMaxDurationChange: (Int) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
) {
    val haptics = rememberHaptics()
    val config = settings.recording
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLargeEmphasized) })
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxl),
        ) {
            SectionHeader(stringResource(R.string.settings_section_recording), Modifier.springEntrance(0))
            SectionCard(Modifier.springEntrance(1)) {
                if (locked) {
                    Text(
                        stringResource(R.string.record_locked_while_recording),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                LabeledIcon(XIcons.CameraSwitch, stringResource(R.string.setting_camera))
                SegmentedToggle(
                    options = CameraLens.entries.map { it.selector to stringResource(it.labelRes) },
                    selected = config.cameraLens,
                    onSelect = onLensChange,
                    enabled = !locked,
                )
                LabeledIcon(XIcons.HighQuality, stringResource(R.string.setting_quality))
                SegmentedToggle(
                    options = VideoQuality.entries.map { it to stringResource(it.shortLabelRes) },
                    selected = config.videoQuality,
                    onSelect = onQualityChange,
                    enabled = !locked,
                )
                Text(
                    stringResource(config.videoQuality.labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SwitchRow(
                    icon = if (config.enableAudio) XIcons.Mic else XIcons.MicOff,
                    title = stringResource(R.string.setting_audio),
                    supporting = stringResource(if (config.enableAudio) R.string.setting_audio_on else R.string.setting_audio_off),
                    checked = config.enableAudio,
                    enabled = !locked,
                    onChange = {
                        if (it) haptics.toggleOn() else haptics.toggleOff()
                        onAudioChange(it)
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                LabeledIcon(XIcons.Timer, stringResource(R.string.setting_max_duration))
                Text(
                    stringResource(R.string.setting_max_duration_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SegmentedToggle(
                    options =
                    RecordingConfig.MAX_DURATION_OPTIONS.map { minutes ->
                        minutes to
                            if (minutes == 0) {
                                stringResource(R.string.duration_off)
                            } else {
                                stringResource(R.string.duration_minutes, minutes)
                            }
                    },
                    selected = config.maxDurationMinutes,
                    onSelect = onMaxDurationChange,
                    enabled = !locked,
                )
            }

            SectionHeader(stringResource(R.string.settings_section_appearance), Modifier.springEntrance(2))
            SectionCard(Modifier.springEntrance(3)) {
                LabeledIcon(XIcons.DarkMode, stringResource(R.string.setting_theme))
                SegmentedToggle(
                    options =
                    listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.theme_light),
                        ThemeMode.DARK to stringResource(R.string.theme_dark),
                    ),
                    selected = settings.themeMode,
                    onSelect = onThemeChange,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SwitchRow(
                    icon = XIcons.Palette,
                    title = stringResource(R.string.setting_dynamic_color),
                    supporting = stringResource(R.string.setting_dynamic_color_hint),
                    checked = settings.dynamicColor,
                    onChange = {
                        if (it) haptics.toggleOn() else haptics.toggleOff()
                        onDynamicColorChange(it)
                    },
                )
            }

            SectionHeader(stringResource(R.string.settings_section_about), Modifier.springEntrance(4))
            SectionCard(Modifier.springEntrance(5)) {
                InfoRow(XIcons.Info, stringResource(R.string.setting_version), BuildConfig.VERSION_NAME)
                InfoRow(XIcons.VideoLibrary, stringResource(R.string.setting_storage), stringResource(R.string.setting_storage_value))
                val uriHandler = LocalUriHandler.current
                val website = stringResource(R.string.website_url)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(role = Role.Button) { uriHandler.openUri(website) }
                        .padding(vertical = Spacing.xs),
                ) {
                    Icon(XIcons.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.setting_website), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(website.removePrefix("https://"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.Top) {
                    Icon(XIcons.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(Spacing.md))
                    RowLabel(
                        title = stringResource(R.string.setting_legal_title),
                        supporting = stringResource(R.string.setting_legal_text),
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledIcon(
    icon: ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Spacing.md))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    supporting: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Spacing.md))
        RowLabel(title, supporting = supporting, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(Spacing.md))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
