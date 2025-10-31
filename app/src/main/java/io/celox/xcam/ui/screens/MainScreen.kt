package io.celox.xcam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.celox.xcam.data.model.RecordingState
import io.celox.xcam.viewmodel.RecordingViewModel

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
                title = { Text("XCam") },
                actions = {
                    IconButton(onClick = onNavigateToVideos) {
                        Icon(Icons.Default.VideoLibrary, "Videos")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!hasPermissions) {
                PermissionRequiredContent(onRequestPermissions)
            } else {
                RecordingContent(
                    recordingState = recordingState,
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = { viewModel.stopRecording() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recording configuration info
            ConfigurationCard(recordingConfig)
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
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Camera and microphone permissions are needed to record videos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permissions")
        }
    }
}

@Composable
private fun RecordingContent(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val isRecording = recordingState is RecordingState.Recording

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status indicator
        Icon(
            if (isRecording) Icons.Default.FiberManualRecord else Icons.Default.Videocam,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            when (recordingState) {
                is RecordingState.Idle -> "Ready to Record"
                is RecordingState.Starting -> "Starting..."
                is RecordingState.Recording -> "Recording in Background"
                is RecordingState.Stopping -> "Stopping..."
                is RecordingState.Error -> "Error: ${recordingState.message}"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (recordingState is RecordingState.Recording) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You can turn off the screen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Control button
        Button(
            onClick = if (isRecording) onStopRecording else onStartRecording,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isRecording) "Stop Recording" else "Start Recording",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ConfigurationCard(config: io.celox.xcam.data.model.RecordingConfig) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Current Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ConfigItem("Camera", if (config.cameraLens == androidx.camera.core.CameraSelector.LENS_FACING_BACK) "Back" else "Front")
            ConfigItem("Quality", config.videoQuality.displayName)
            ConfigItem("Audio", if (config.enableAudio) "Enabled" else "Disabled")
            if (config.maxDurationMinutes > 0) {
                ConfigItem("Max Duration", "${config.maxDurationMinutes} minutes")
            }
        }
    }
}

@Composable
private fun ConfigItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
