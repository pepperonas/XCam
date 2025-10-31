package io.celox.xcam.ui.screens

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.viewmodel.RecordingViewModel
import io.celox.xcam.ui.icons.ArrowBackCustom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecordingViewModel = viewModel()
) {
    val config by viewModel.recordingConfig.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBackCustom, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Camera Selection
            SettingSection(title = "Camera") {
                SegmentedButton(
                    selected = config.cameraLens == CameraSelector.LENS_FACING_BACK,
                    label = "Back Camera",
                    onClick = { viewModel.updateCameraLens(CameraSelector.LENS_FACING_BACK) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedButton(
                    selected = config.cameraLens == CameraSelector.LENS_FACING_FRONT,
                    label = "Front Camera",
                    onClick = { viewModel.updateCameraLens(CameraSelector.LENS_FACING_FRONT) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Video Quality
            SettingSection(title = "Video Quality") {
                VideoQuality.values().forEach { quality ->
                    SegmentedButton(
                        selected = config.videoQuality == quality,
                        label = quality.displayName,
                        onClick = { viewModel.updateVideoQuality(quality) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio Recording
            SettingSection(title = "Audio") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Audio Recording")
                    Switch(
                        checked = config.enableAudio,
                        onCheckedChange = { viewModel.updateEnableAudio(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Battery Management
            SettingSection(title = "Battery Management") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stop at Low Battery (10%)")
                    Switch(
                        checked = config.stopAtLowBattery,
                        onCheckedChange = { viewModel.updateStopAtLowBattery(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Legal Notice",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This app must only be used with consent of all recorded persons. " +
                                "Unauthorized recordings may be illegal and subject to criminal prosecution. " +
                                "You are solely responsible for lawful use of this application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SegmentedButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(label)
    }
}
