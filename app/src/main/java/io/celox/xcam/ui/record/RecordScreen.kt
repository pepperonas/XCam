package io.celox.xcam.ui.record

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.celox.xcam.R
import io.celox.xcam.data.model.CameraLens
import io.celox.xcam.data.model.RecordingConfig
import io.celox.xcam.data.model.RecordingState
import io.celox.xcam.data.model.VideoFile
import io.celox.xcam.data.model.VideoQuality
import io.celox.xcam.data.update.AppUpdate
import io.celox.xcam.data.update.UpdateChecker
import io.celox.xcam.ui.PermissionActions
import io.celox.xcam.ui.Permissions
import io.celox.xcam.ui.components.ExpressiveLoadingIndicator
import io.celox.xcam.ui.components.RollingText
import io.celox.xcam.ui.components.RowLabel
import io.celox.xcam.ui.components.SectionCard
import io.celox.xcam.ui.components.SegmentedToggle
import io.celox.xcam.ui.components.VideoThumbnail
import io.celox.xcam.ui.components.rememberHaptics
import io.celox.xcam.ui.components.springPressed
import io.celox.xcam.ui.icons.XIcons
import io.celox.xcam.ui.motion.MorphShape
import io.celox.xcam.ui.motion.rememberMorph
import io.celox.xcam.ui.motion.rememberReduceMotion
import io.celox.xcam.ui.motion.springEntrance
import io.celox.xcam.ui.theme.BannerShape
import io.celox.xcam.ui.theme.Sizes
import io.celox.xcam.ui.theme.Spacing
import io.celox.xcam.util.formatElapsed
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun RecordScreen(
    state: RecordingState,
    config: RecordingConfig,
    latest: VideoFile?,
    availableUpdate: AppUpdate?,
    permissions: Permissions,
    permissionActions: PermissionActions,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDismissError: () -> Unit,
    onLensChange: (Int) -> Unit,
    onQualityChange: (VideoQuality) -> Unit,
    onAudioChange: (Boolean) -> Unit,
    onOpenVideo: (VideoFile) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLargeEmphasized) },
            )
        },
    ) { padding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(visible = availableUpdate != null && !state.isBusy) {
                availableUpdate?.let { UpdateBanner(it) }
            }
            AnimatedVisibility(visible = state is RecordingState.Error) {
                (state as? RecordingState.Error)?.let { error ->
                    ErrorBanner(error, onDismiss = onDismissError, onRetry = {
                        onDismissError()
                        onStart()
                    })
                }
            }

            if (!permissions.camera) {
                PermissionCard(permissionActions, Modifier.padding(top = Spacing.lg).springEntrance(0))
            } else {
                StatusPill(state, Modifier.padding(top = Spacing.lg).springEntrance(0))
                Spacer(Modifier.height(Spacing.xxl))
                RecordHero(
                    state = state,
                    onStart = onStart,
                    onStop = onStop,
                    modifier = Modifier.springEntrance(1),
                )
                Spacer(Modifier.height(Spacing.xl))
                TimerReadout(state, Modifier.springEntrance(2))
                Text(
                    text =
                    stringResource(
                        if (state.isBusy) R.string.record_hint_recording else R.string.record_hint_idle,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xxl).springEntrance(3),
                )
            }

            QuickSetup(
                config = config,
                locked = state.isBusy,
                audioAllowed = permissions.audio,
                onLensChange = onLensChange,
                onQualityChange = onQualityChange,
                onAudioChange = { enabled ->
                    if (enabled && !permissions.audio) permissionActions.requestAudioAndNotifications()
                    onAudioChange(enabled)
                },
                modifier = Modifier.springEntrance(4),
            )

            if (latest != null) {
                Spacer(Modifier.height(Spacing.md))
                LatestRecording(latest, onClick = { onOpenVideo(latest) }, modifier = Modifier.springEntrance(5))
            }
        }
    }
}

/** A pill naming the state, with a live dot that pulses while recording. Announced to TalkBack. */
@Composable
private fun StatusPill(
    state: RecordingState,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val recording = state is RecordingState.Recording
    val container by animateColorAsState(
        if (recording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        motion.defaultEffectsSpec(),
        label = "pill-bg",
    )
    val content by animateColorAsState(
        if (recording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        motion.defaultEffectsSpec(),
        label = "pill-fg",
    )
    val label =
        stringResource(
            when (state) {
                RecordingState.Starting -> R.string.record_status_starting
                is RecordingState.Recording -> R.string.record_status_recording
                RecordingState.Stopping -> R.string.record_status_stopping
                else -> R.string.record_status_ready
            },
        )
    Row(
        modifier =
        modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiveDot(active = recording, color = if (recording) MaterialTheme.colorScheme.error else content)
        Spacer(Modifier.width(Spacing.sm))
        AnimatedContent(
            targetState = label,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec()) togetherWith
                    fadeOut(motion.fastEffectsSpec())
            },
            label = "pill-label",
        ) { text ->
            Text(text, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}

@Composable
private fun LiveDot(
    active: Boolean,
    color: androidx.compose.ui.graphics.Color,
) {
    val reduce = rememberReduceMotion()
    val alpha =
        if (active && !reduce) {
            rememberInfiniteTransition(label = "live").animateFloat(
                initialValue = 1f,
                targetValue = 0.25f,
                animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                label = "live-alpha",
            ).value
        } else {
            1f
        }
    Box(
        Modifier
            .size(8.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(color),
    )
}

/**
 * The signature element: one button whose *shape* is its state. Idle it is a slowly turning
 * nine-lobed cookie; tapping morphs it — on the expressive spatial spring, overshoot included — into
 * a rounded square, the universal "stop". Around it a wavy ring runs while recording: indeterminate
 * without a limit, a filling progress ring with one.
 */
@Composable
private fun RecordHero(
    state: RecordingState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val reduce = rememberReduceMotion()
    val haptics = rememberHaptics()
    val recording = state is RecordingState.Recording || state is RecordingState.Stopping
    val transitional = state is RecordingState.Starting || state is RecordingState.Stopping

    val morph = rememberMorph(MaterialShapes.Cookie9Sided, MaterialShapes.Square)
    val progress = remember { Animatable(if (recording) 1f else 0f) }
    val spatial = motion.defaultSpatialSpec<Float>()
    LaunchedEffect(recording, reduce) {
        val target = if (recording) 1f else 0f
        if (reduce) progress.snapTo(target) else progress.animateTo(target, spatial)
    }

    // Idle, the cookie turns slowly — the button is alive before it is touched.
    val rotation =
        if (!reduce && !recording) {
            rememberInfiniteTransition(label = "idle-spin").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing)),
                label = "spin",
            ).value
        } else {
            0f
        }

    val container by animateColorAsState(
        if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        motion.defaultEffectsSpec(),
        label = "hero-color",
    )
    val onContainer = if (recording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary

    val interaction = remember { MutableInteractionSource() }
    val description = stringResource(if (recording) R.string.record_stop else R.string.record_start)

    Box(modifier = modifier.size(Sizes.recordRing), contentAlignment = Alignment.Center) {
        DurationRing(state)

        Box(
            modifier =
            Modifier
                .size(Sizes.recordButton)
                .springPressed(interaction, pressedScale = 0.9f)
                .graphicsLayer { rotationZ = rotation * (1f - progress.value.coerceIn(0f, 1f)) }
                .clip(MorphShape(morph, progress.value))
                .background(container)
                .semantics {
                    contentDescription = description
                    stateDescription = description
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = !transitional,
                    role = Role.Button,
                ) {
                    haptics.confirm()
                    if (recording) onStop() else onStart()
                },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState =
                when {
                    transitional -> HeroGlyph.Busy
                    recording -> HeroGlyph.Stop
                    else -> HeroGlyph.Record
                },
                transitionSpec = {
                    val spatial = motion.defaultSpatialSpec<Float>()
                    (scaleIn(spatial, initialScale = 0.4f) + fadeIn(motion.defaultEffectsSpec())) togetherWith
                        (scaleOut(spatial, targetScale = 0.4f) + fadeOut(motion.fastEffectsSpec()))
                },
                label = "hero-glyph",
            ) { glyph ->
                when (glyph) {
                    HeroGlyph.Busy -> ExpressiveLoadingIndicator(Modifier.size(64.dp), color = onContainer)
                    // Counter-rotate so the dot does not orbit while the cookie turns.
                    HeroGlyph.Record ->
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(onContainer),
                        )
                    HeroGlyph.Stop ->
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(onContainer),
                        )
                }
            }
        }
    }
}

private enum class HeroGlyph { Record, Stop, Busy }

@Composable
private fun DurationRing(state: RecordingState) {
    val recording = state as? RecordingState.Recording
    AnimatedVisibility(
        visible = recording != null,
        enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) + scaleIn(MaterialTheme.motionScheme.defaultSpatialSpec(), 0.85f),
        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) + scaleOut(MaterialTheme.motionScheme.fastSpatialSpec(), 0.85f),
    ) {
        val limit = recording?.maxDurationMillis ?: 0L
        val color = MaterialTheme.colorScheme.error
        val track = MaterialTheme.colorScheme.errorContainer
        if (recording != null && limit > 0) {
            val elapsed = rememberElapsed(recording.startTime)
            val fraction by animateFloatAsState(
                (elapsed.toFloat() / limit).coerceIn(0f, 1f),
                MaterialTheme.motionScheme.slowEffectsSpec(),
                label = "limit",
            )
            CircularWavyProgressIndicator(
                progress = { fraction },
                modifier = Modifier.size(Sizes.recordRing),
                color = color,
                trackColor = track,
            )
        } else {
            CircularWavyProgressIndicator(
                modifier = Modifier.size(Sizes.recordRing),
                color = color,
                trackColor = track,
            )
        }
    }
}

/** Elapsed millis since [startTime], ticking a few times a second while composed. */
@Composable
private fun rememberElapsed(startTime: Long): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startTime) {
        while (isActive) {
            now = System.currentTimeMillis()
            delay(250)
        }
    }
    return (now - startTime).coerceAtLeast(0)
}

@Composable
private fun TimerReadout(
    state: RecordingState,
    modifier: Modifier = Modifier,
) {
    val recording = state as? RecordingState.Recording
    val elapsed = if (recording != null) rememberElapsed(recording.startTime) else 0L
    val style =
        MaterialTheme.typography.displayMediumEmphasized.copy(fontFeatureSettings = "tnum")
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        RollingText(
            text = formatElapsed(elapsed),
            style = style,
            color = if (recording != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val limit = recording?.maxDurationMillis ?: 0L
        AnimatedVisibility(visible = limit > 0) {
            Text(
                stringResource(R.string.record_limit, formatElapsed(limit)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickSetup(
    config: RecordingConfig,
    locked: Boolean,
    audioAllowed: Boolean,
    onLensChange: (Int) -> Unit,
    onQualityChange: (VideoQuality) -> Unit,
    onAudioChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    SectionCard(modifier) {
        Column {
            Text(stringResource(R.string.record_quick_settings), style = MaterialTheme.typography.titleMediumEmphasized)
            AnimatedVisibility(visible = locked) {
                Text(
                    stringResource(R.string.record_locked_while_recording),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SegmentedToggle(
            options = CameraLens.entries.map { it.selector to stringResource(it.labelRes) },
            selected = config.cameraLens,
            onSelect = onLensChange,
            enabled = !locked,
        )
        SegmentedToggle(
            options = VideoQuality.entries.map { it to stringResource(it.shortLabelRes) },
            selected = config.videoQuality,
            onSelect = onQualityChange,
            enabled = !locked,
        )
        val audioOn = config.enableAudio && audioAllowed
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (audioOn) XIcons.Mic else XIcons.MicOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Spacing.md))
            RowLabel(
                title = stringResource(R.string.setting_audio),
                supporting =
                stringResource(
                    when {
                        !audioAllowed && config.enableAudio -> R.string.setting_audio_no_permission
                        audioOn -> R.string.setting_audio_on
                        else -> R.string.setting_audio_off
                    },
                ),
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = audioOn,
                enabled = !locked,
                onCheckedChange = {
                    if (it) haptics.toggleOn() else haptics.toggleOff()
                    onAudioChange(it)
                },
            )
        }
    }
}

@Composable
private fun LatestRecording(
    video: VideoFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth().springPressed(interaction, 0.97f),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            VideoThumbnail(
                uri = video.uri,
                modifier = Modifier.size(width = Sizes.latestThumbnail * 1.5f, height = Sizes.latestThumbnail),
            )
            Spacer(Modifier.width(Spacing.lg))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.record_latest),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(
                        R.string.videos_meta,
                        video.formattedDuration.ifEmpty { "–" },
                        stringResource(R.string.videos_size_mb, video.sizeInMB),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Icon(XIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionCard(
    actions: PermissionActions,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            Modifier.padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                Modifier
                    .size(Sizes.heroMark)
                    .clip(MorphShape(rememberMorph(MaterialShapes.Cookie9Sided, MaterialShapes.Cookie9Sided), 0f))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(XIcons.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Text(
                stringResource(R.string.record_permission_title),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.record_permission_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
            Button(onClick = actions.requestCamera, modifier = Modifier.fillMaxWidth().height(Sizes.buttonHeight)) {
                Text(stringResource(R.string.record_permission_grant))
            }
            TextButton(onClick = actions.openAppSettings) {
                Text(stringResource(R.string.record_permission_settings))
            }
        }
    }
}

/** "XCam 3.1.0 is available" — opens the product page (download button, changelog, checksum). */
@Composable
private fun UpdateBanner(update: AppUpdate) {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
        shape = BannerShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Row(
            Modifier.padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(XIcons.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.width(Spacing.md))
            Text(
                stringResource(R.string.update_available, update.version.removePrefix("v")),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { uriHandler.openUri(UpdateChecker.WEBSITE_URL) }) {
                Text(stringResource(R.string.update_open_website))
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    error: RecordingState.Error,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
        shape = BannerShape,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(XIcons.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.width(Spacing.md))
                Text(
                    stringResource(
                        when (error.reason) {
                            RecordingState.Reason.CAMERA_UNAVAILABLE -> R.string.error_camera_unavailable
                            RecordingState.Reason.RECORDING_FAILED -> R.string.error_recording_failed
                            RecordingState.Reason.NO_SPACE -> R.string.error_no_space
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_dismiss)) }
                OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            }
        }
    }
}
