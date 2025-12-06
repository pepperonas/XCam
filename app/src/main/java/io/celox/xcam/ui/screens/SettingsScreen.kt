package io.celox.xcam.ui.screens

import androidx.camera.core.CameraSelector
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.viewmodel.RecordingViewModel
import io.celox.xcam.ui.icons.ArrowBackCustom
import io.celox.xcam.ui.icons.WarningCustom
import io.celox.xcam.ui.components.AnimatedIconButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecordingViewModel = viewModel()
) {
    val config by viewModel.recordingConfig.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Animation states for staggered entrance
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    AnimatedIconButton(
                        icon = Icons.Filled.ArrowBackCustom,
                        contentDescription = "Back",
                        onClick = onNavigateBack,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Camera Selection Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 0)) +
                        slideInVertically(animationSpec = tween(300, delayMillis = 0)) { it / 4 }
            ) {
                SettingsCard(title = "Camera") {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = config.cameraLens == CameraSelector.LENS_FACING_BACK,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.updateCameraLens(CameraSelector.LENS_FACING_BACK)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Back", fontWeight = FontWeight.Medium)
                        }
                        SegmentedButton(
                            selected = config.cameraLens == CameraSelector.LENS_FACING_FRONT,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.updateCameraLens(CameraSelector.LENS_FACING_FRONT)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Front", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Video Quality Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 100)) +
                        slideInVertically(animationSpec = tween(300, delayMillis = 100)) { it / 4 }
            ) {
                SettingsCard(title = "Video Quality") {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VideoQuality.entries.forEachIndexed { index, quality ->
                            SegmentedButton(
                                selected = config.videoQuality == quality,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.updateVideoQuality(quality)
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = VideoQuality.entries.size
                                ),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primary,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    quality.displayName.replace(" ", "\n"),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            // Audio Settings Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 200)) +
                        slideInVertically(animationSpec = tween(300, delayMillis = 200)) { it / 4 }
            ) {
                SettingsCard(title = "Audio") {
                    SettingToggleRow(
                        title = "Enable Audio Recording",
                        subtitle = "Record sound with video",
                        checked = config.enableAudio,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.updateEnableAudio(it)
                        }
                    )
                }
            }

            // Battery Management Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 300)) +
                        slideInVertically(animationSpec = tween(300, delayMillis = 300)) { it / 4 }
            ) {
                SettingsCard(title = "Battery Management") {
                    SettingToggleRow(
                        title = "Stop at Low Battery",
                        subtitle = "Automatically stop recording at 10%",
                        checked = config.stopAtLowBattery,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.updateStopAtLowBattery(it)
                        }
                    )
                }
            }

            // Legal Disclaimer Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 400)) +
                        slideInVertically(animationSpec = tween(300, delayMillis = 400)) { it / 4 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.WarningCustom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Legal Notice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "This app must only be used with consent of all recorded persons. " +
                                        "Unauthorized recordings may be illegal and subject to criminal prosecution. " +
                                        "You are solely responsible for lawful use of this application.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
