package io.celox.xcam.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import io.celox.xcam.data.model.VideoFile
import io.celox.xcam.viewmodel.RecordingViewModel
import io.celox.xcam.ui.icons.*
import io.celox.xcam.ui.components.AnimatedIconButton
import io.celox.xcam.ui.components.EmptyState
import io.celox.xcam.ui.components.GlassmorphicCard
import io.celox.xcam.ui.components.ShimmerEffect
import io.celox.xcam.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Int) -> Unit = {},
    viewModel: RecordingViewModel = viewModel()
) {
    val videos by viewModel.videoFiles.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.loadVideoFiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recorded Videos",
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
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AnimatedContent(
            targetState = videos.isEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "videos_content"
        ) { isEmpty ->
            if (isEmpty) {
                EmptyState(
                    icon = Icons.Filled.VideoLibraryCustom,
                    title = "No Videos Yet",
                    message = "Start recording to see your videos here",
                    modifier = Modifier.padding(padding)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with count
                    item {
                        Text(
                            "${videos.size} video${if (videos.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    itemsIndexed(
                        items = videos,
                        key = { _, video -> video.name }
                    ) { index, video ->
                        AnimatedVideoItem(
                            video = video,
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteVideo(video)
                            },
                            onClick = { onNavigateToPlayer(index) },
                            animationDelay = index * 50
                        )
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedVideoItem(
    video: VideoFile,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    animationDelay: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(animationSpec = tween(300)) { -it / 4 } +
                expandVertically(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200)) +
                shrinkVertically(animationSpec = tween(200))
    ) {
        VideoItemCard(
            video = video,
            onDeleteClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDeleteDialog = true
            },
            onClick = onClick
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            videoName = video.name,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun VideoItemCard(
    video: VideoFile,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceGlass
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video thumbnail with Coil
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.file)
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .crossfade(true)
                        .build(),
                    contentDescription = "Video thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PlayArrowCustom,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }

                // Duration badge
                if (video.duration > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = video.formattedDuration,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    video.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VideoMetadataChip(
                        text = formatFileSize(video.sizeInMB)
                    )
                    VideoMetadataChip(
                        text = formatDate(video.timestamp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button with animation
            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "delete_scale"
            )

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.scale(scale)
            ) {
                Icon(
                    Icons.Filled.DeleteCustom,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoMetadataChip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    videoName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                Icons.Filled.DeleteCustom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Delete Video?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Are you sure you want to delete \"$videoName\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

private fun formatFileSize(sizeInMB: Float): String {
    return if (sizeInMB < 1024) {
        "%.1f MB".format(sizeInMB)
    } else {
        "%.2f GB".format(sizeInMB / 1024)
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
