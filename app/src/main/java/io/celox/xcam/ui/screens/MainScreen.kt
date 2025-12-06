package io.celox.xcam.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.celox.xcam.data.model.RecordingState
import io.celox.xcam.viewmodel.RecordingViewModel
import io.celox.xcam.ui.icons.*
import io.celox.xcam.ui.components.*
import io.celox.xcam.ui.theme.RecordingRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToVideos: () -> Unit,
    onRequestPermissions: () -> Unit,
    hasPermissions: Boolean,
    viewModel: RecordingViewModel = viewModel()
) {
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingConfig by viewModel.recordingConfig.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "XCam",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    AnimatedIconButton(
                        icon = Icons.Filled.VideoLibraryCustom,
                        contentDescription = "Videos",
                        onClick = onNavigateToVideos,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedIconButton(
                        icon = Icons.Filled.SettingsCustom,
                        contentDescription = "Settings",
                        onClick = onNavigateToSettings,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            AnimatedContent(
                targetState = hasPermissions,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "permission_content"
            ) { hasPerms ->
                if (!hasPerms) {
                    PermissionRequiredContent(onRequestPermissions)
                } else {
                    RecordingContent(
                        recordingState = recordingState,
                        onStartRecording = { viewModel.startRecording() },
                        onStopRecording = { viewModel.stopRecording() }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Recording configuration info with animation
            AnimatedVisibility(
                visible = hasPermissions,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                ConfigurationCard(recordingConfig)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionRequiredContent(onRequestPermissions: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            Icons.Filled.WarningCustom,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Camera and microphone permissions are needed to record videos.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedRecordButton(
            isRecording = false,
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecordingContent(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val isRecording = recordingState is RecordingState.Recording
    val isLoading = recordingState is RecordingState.Starting || recordingState is RecordingState.Stopping

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated status indicator
        AnimatedRecordingIndicator(
            isRecording = isRecording,
            size = 140.dp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Status chip
        AnimatedContent(
            targetState = recordingState,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.9f) togetherWith
                        fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.9f)
            },
            label = "status_chip"
        ) { state ->
            StatusChip(
                text = when (state) {
                    is RecordingState.Idle -> "Ready to Record"
                    is RecordingState.Starting -> "Starting..."
                    is RecordingState.Recording -> "Recording Active"
                    is RecordingState.Stopping -> "Stopping..."
                    is RecordingState.Error -> "Error"
                },
                isActive = state is RecordingState.Recording
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status text with animation
        AnimatedContent(
            targetState = recordingState,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "status_text"
        ) { state ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when (state) {
                        is RecordingState.Idle -> "Tap the button to start recording"
                        is RecordingState.Starting -> "Initializing camera..."
                        is RecordingState.Recording -> "Recording in background"
                        is RecordingState.Stopping -> "Saving video..."
                        is RecordingState.Error -> state.message
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (state is RecordingState.Error)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (state is RecordingState.Recording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can turn off the screen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Animated control button
        AnimatedRecordButton(
            isRecording = isRecording,
            onClick = if (isRecording) onStopRecording else onStartRecording,
            enabled = !isLoading
        )
    }
}

@Composable
private fun ConfigurationCard(config: io.celox.xcam.data.model.RecordingConfig) {
    GradientCard {
        Text(
            "Current Configuration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        ConfigDisplayRow(
            "Camera",
            if (config.cameraLens == androidx.camera.core.CameraSelector.LENS_FACING_BACK) "Back" else "Front"
        )
        ConfigDisplayRow("Quality", config.videoQuality.displayName)
        ConfigDisplayRow("Audio", if (config.enableAudio) "Enabled" else "Disabled")

        if (config.maxDurationMinutes > 0) {
            ConfigDisplayRow("Max Duration", "${config.maxDurationMinutes} min")
        }
    }
}
