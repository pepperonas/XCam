package io.celox.xcam.ui.videos

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.celox.xcam.R
import io.celox.xcam.data.model.DayGroup
import io.celox.xcam.data.model.VideoFile
import io.celox.xcam.data.model.groupByDay
import io.celox.xcam.ui.components.AppMark
import io.celox.xcam.ui.components.ExpressiveLoadingIndicator
import io.celox.xcam.ui.components.VideoThumbnail
import io.celox.xcam.ui.components.rememberHaptics
import io.celox.xcam.ui.components.springPressed
import io.celox.xcam.ui.icons.XIcons
import io.celox.xcam.ui.motion.springEntrance
import io.celox.xcam.ui.theme.Sizes
import io.celox.xcam.ui.theme.Spacing
import io.celox.xcam.util.shareVideos
import io.celox.xcam.viewmodel.RecordingViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

@Composable
fun VideosScreen(
    videos: List<VideoFile>,
    loading: Boolean,
    events: Flow<RecordingViewModel.UiEvent>,
    onOpen: (VideoFile) -> Unit,
    onDelete: (List<VideoFile>) -> Unit,
    onReload: () -> Unit,
    onStartRecording: () -> Unit,
    mediaAccess: Boolean,
    onRequestMedia: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val context = LocalContext.current
    val haptics = rememberHaptics()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Selection survives rotation; ids that disappeared (deleted elsewhere) are dropped.
    var selectedIds by rememberSaveable { mutableStateOf(LongArray(0)) }
    val existing = videos.map { it.id }.toSet()
    val selection = selectedIds.filter { it in existing }.toSet()
    val selecting = selection.isNotEmpty()
    var pendingDelete by remember { mutableStateOf<List<VideoFile>?>(null) }

    val consentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { onReload() }

    val resources = LocalResources.current
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is RecordingViewModel.UiEvent.Deleted ->
                    scope.launch {
                        snackbar.showSnackbar(resources.getQuantityString(R.plurals.videos_deleted, event.count, event.count))
                    }
                is RecordingViewModel.UiEvent.ConfirmDelete ->
                    consentLauncher.launch(IntentSenderRequest.Builder(event.intentSender).build())
            }
        }
    }

    // Granting media access makes older recordings visible: query again.
    LaunchedEffect(mediaAccess) { if (mediaAccess) onReload() }

    BackHandler(enabled = selecting) { selectedIds = LongArray(0) }

    fun toggle(video: VideoFile) {
        selectedIds = (if (video.id in selection) selection - video.id else selection + video.id).toLongArray()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor =
                    animateColorAsState(
                        if (selecting) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        motion.defaultEffectsSpec(),
                        label = "bar",
                    ).value,
                ),
                navigationIcon = {
                    if (selecting) {
                        IconButton(onClick = { selectedIds = LongArray(0) }) {
                            Icon(XIcons.Close, contentDescription = stringResource(R.string.action_close_selection))
                        }
                    }
                },
                title = {
                    AnimatedContent(
                        targetState = selecting,
                        transitionSpec = {
                            fadeIn(motion.defaultEffectsSpec()) togetherWith
                                fadeOut(motion.fastEffectsSpec())
                        },
                        label = "title",
                    ) { sel ->
                        if (sel) {
                            Text(pluralStringResource(R.plurals.videos_selected, selection.size, selection.size))
                        } else {
                            Column {
                                Text(stringResource(R.string.videos_title), style = MaterialTheme.typography.titleLargeEmphasized)
                                if (videos.isNotEmpty()) {
                                    Text(
                                        pluralStringResource(R.plurals.videos_count, videos.size, videos.size),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    if (selecting) {
                        IconButton(onClick = { selectedIds = videos.map { it.id }.toLongArray() }) {
                            Icon(XIcons.CheckCircle, contentDescription = stringResource(R.string.action_select_all))
                        }
                        IconButton(onClick = { shareVideos(context, videos.filter { it.id in selection }.map { it.uri }) }) {
                            Icon(XIcons.Share, contentDescription = stringResource(R.string.action_share))
                        }
                        IconButton(onClick = { pendingDelete = videos.filter { it.id in selection } }) {
                            Icon(XIcons.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> ExpressiveLoadingIndicator(Modifier.align(Alignment.Center).size(72.dp))
                videos.isEmpty() ->
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyState(onStartRecording)
                        if (!mediaAccess) OlderRecordingsCard(onRequestMedia, Modifier.padding(horizontal = Spacing.lg))
                    }
                else -> {
                    val today = LocalDate.now()
                    val groups = remember(videos, today) { groupByDay(videos, today, ZoneId.systemDefault()) }
                    // Entrance stagger by on-screen order; computed once, not inside the lazy item lambdas.
                    val order = remember(groups) { groups.flatMap { it.second }.withIndex().associate { (i, v) -> v.id to i } }
                    LazyColumn(
                        contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.xxl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        groups.forEach { (group, items) ->
                            stickyHeader(key = "h-$group") { DayHeader(group) }
                            items(items, key = { it.id }) { video ->
                                VideoRow(
                                    video = video,
                                    selected = video.id in selection,
                                    selecting = selecting,
                                    onClick = { if (selecting) toggle(video) else onOpen(video) },
                                    onLongClick = {
                                        haptics.longPress()
                                        toggle(video)
                                    },
                                    onShare = { shareVideos(context, listOf(video.uri)) },
                                    onDelete = { pendingDelete = listOf(video) },
                                    modifier =
                                    Modifier.animateItem().let { m ->
                                        // Only the first screenful cascades in; rows scrolled into view later just appear.
                                        val pos = order[video.id] ?: Int.MAX_VALUE
                                        if (pos < 8) m.springEntrance(pos) else m
                                    },
                                )
                            }
                        }
                        if (!mediaAccess) {
                            item(key = "older") {
                                OlderRecordingsCard(onRequestMedia, Modifier.padding(top = Spacing.md))
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { toDelete ->
        DeleteDialog(
            count = toDelete.size,
            onConfirm = {
                haptics.confirm()
                onDelete(toDelete)
                selectedIds = LongArray(0)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun DayHeader(group: DayGroup) {
    val label =
        when (group) {
            DayGroup.Today -> stringResource(R.string.videos_today)
            DayGroup.Yesterday -> stringResource(R.string.videos_yesterday)
            is DayGroup.Day -> group.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
        }
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = Spacing.sm, top = Spacing.lg, bottom = Spacing.sm),
        )
    }
}

@Composable
private fun VideoRow(
    video: VideoFile,
    selected: Boolean,
    selecting: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val interaction = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        motion.defaultEffectsSpec(),
        label = "row-bg",
    )
    // The selected card rounds less: shape reinforces the colour change (M3 Expressive shape as state).
    val shape = if (selected) MaterialTheme.shapes.large else MaterialTheme.shapes.extraLarge
    Surface(
        color = container,
        shape = shape,
        modifier =
        modifier
            .fillMaxWidth()
            .springPressed(interaction, 0.97f)
            .semantics { this.selected = selected },
    ) {
        Row(
            modifier =
            Modifier
                .combinedClickable(
                    interactionSource = interaction,
                    indication = androidx.compose.material3.ripple(),
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                VideoThumbnail(
                    uri = video.uri,
                    durationLabel = video.formattedDuration,
                    modifier = Modifier.size(width = Sizes.thumbnailWidth, height = Sizes.thumbnailHeight),
                )
            }
            Spacer(Modifier.width(Spacing.lg))
            Column(Modifier.weight(1f)) {
                Text(
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(video.timestamp)),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Text(
                    stringResource(
                        R.string.videos_meta,
                        video.formattedDuration.ifEmpty { "–" },
                        stringResource(R.string.videos_size_mb, video.sizeInMB),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedContent(
                targetState = selecting,
                transitionSpec = {
                    (scaleIn(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec())) togetherWith
                        (scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()))
                },
                label = "trailing",
            ) { sel ->
                if (sel) {
                    Icon(
                        if (selected) XIcons.CheckCircle else XIcons.Circle,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(Spacing.md),
                    )
                } else {
                    OverflowMenu(onShare, onDelete)
                }
            }
        }
    }
}

@Composable
private fun OverflowMenu(
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(XIcons.MoreVert, contentDescription = stringResource(R.string.action_more))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, shape = MaterialTheme.shapes.large) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_share)) },
                leadingIcon = { Icon(XIcons.Share, contentDescription = null) },
                onClick = {
                    open = false
                    onShare()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_delete)) },
                leadingIcon = { Icon(XIcons.Delete, contentDescription = null) },
                onClick = {
                    open = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
fun DeleteDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(XIcons.Delete, contentDescription = null) },
        title = { Text(pluralStringResource(R.plurals.videos_delete_title, count, count), textAlign = TextAlign.Center) },
        text = { Text(stringResource(R.string.videos_delete_text)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/**
 * After a reinstall, MediaStore no longer attributes earlier recordings to XCam, and without media
 * access the app cannot list them (they are still in the gallery). Offered here, never at start.
 */
@Composable
private fun OlderRecordingsCard(
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(XIcons.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(Spacing.md))
            Text(
                stringResource(R.string.videos_older_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(Spacing.sm))
            TextButton(onClick = onAllow) { Text(stringResource(R.string.videos_older_allow)) }
        }
    }
}

@Composable
private fun EmptyState(
    onStartRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppMark(size = Sizes.emptyMark, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), modifier = Modifier.springEntrance(0))
        Spacer(Modifier.height(Spacing.xl))
        Text(
            stringResource(R.string.videos_empty_title),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.springEntrance(1),
        )
        Text(
            stringResource(R.string.videos_empty_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.sm).springEntrance(2),
        )
        Spacer(Modifier.height(Spacing.xl))
        Button(onClick = onStartRecording, modifier = Modifier.height(Sizes.buttonHeight).springEntrance(3)) {
            Icon(XIcons.Videocam, contentDescription = null, modifier = Modifier.size(Sizes.iconSmall))
            Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(R.string.videos_empty_action))
        }
    }
}

