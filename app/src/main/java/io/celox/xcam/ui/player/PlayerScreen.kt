package io.celox.xcam.ui.player

import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import io.celox.xcam.R
import io.celox.xcam.data.model.VideoFile
import io.celox.xcam.ui.components.rememberHaptics
import io.celox.xcam.ui.components.springPressed
import io.celox.xcam.ui.icons.XIcons
import io.celox.xcam.ui.motion.MorphShape
import io.celox.xcam.ui.motion.rememberMorph
import io.celox.xcam.ui.motion.rememberReduceMotion
import io.celox.xcam.ui.theme.Spacing
import io.celox.xcam.ui.videos.DeleteDialog
import io.celox.xcam.util.formatElapsed
import io.celox.xcam.util.shareVideos
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.DateFormat
import java.util.Date

private const val CONTROLS_HIDE_MS = 3_000L

@Composable
fun PlayerScreen(
    video: VideoFile?,
    onBack: () -> Unit,
    onDelete: (VideoFile) -> Unit,
) {
    // The recording can vanish (deleted elsewhere, removed from MediaStore): leave instead of showing
    // a dead player. Navigating is a side effect — never do it straight from composition.
    if (video == null) {
        LaunchedEffect(Unit) { onBack() }
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.player_not_found), color = Color.White)
        }
        return
    }

    val context = LocalContext.current
    val haptics = rememberHaptics()
    // Survives rotation and theme changes, which recreate the activity (and with it the player).
    var savedPosition by rememberSaveable(video.uri) { mutableLongStateOf(0L) }
    val player =
        remember(video.uri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(video.uri), savedPosition)
                prepare()
                playWhenReady = true
            }
        }
    DisposableEffect(player) { onDispose { player.release() } }

    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(video.duration.coerceAtLeast(0L)) }
    var scrubbing by remember { mutableStateOf<Float?>(null) }
    var controlsVisible by remember { mutableStateOf(true) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirmDelete by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener =
            object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (player.duration > 0) duration = player.duration
                    if (state == Player.STATE_ENDED) controlsVisible = true
                }
            }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    // Position ticker — only while playing, so a paused player costs nothing.
    LaunchedEffect(player, isPlaying) {
        while (isActive && isPlaying) {
            position = player.currentPosition
            savedPosition = position
            delay(200)
        }
        position = player.currentPosition
        savedPosition = position
    }

    // Auto-hide the controls while playing.
    LaunchedEffect(controlsVisible, isPlaying, lastInteraction) {
        if (controlsVisible && isPlaying) {
            delay(CONTROLS_HIDE_MS)
            controlsVisible = false
        }
    }

    ImmersiveMode(hidden = !controlsVisible)

    fun touch() {
        lastInteraction = System.currentTimeMillis()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                controlsVisible = !controlsVisible
                touch()
            },
    ) {
        PlayerSurface(player)

        // Top bar
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) + slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) { -it / 2 },
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) + slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { -it / 2 },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.xs, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(XIcons.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = Color.White)
                }
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(video.timestamp)),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { shareVideos(context, listOf(video.uri)) }) {
                    Icon(XIcons.Share, contentDescription = stringResource(R.string.action_share), tint = Color.White)
                }
                IconButton(onClick = {
                    player.pause()
                    confirmDelete = true
                }) {
                    Icon(XIcons.Delete, contentDescription = stringResource(R.string.action_delete), tint = Color.White)
                }
            }
        }

        // Centre play/pause
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) + scaleIn(MaterialTheme.motionScheme.defaultSpatialSpec(), 0.6f),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) + scaleOut(MaterialTheme.motionScheme.fastSpatialSpec(), 0.6f),
            modifier = Modifier.align(Alignment.Center),
        ) {
            PlayPauseButton(
                playing = isPlaying,
                onToggle = {
                    haptics.segmentTick()
                    touch()
                    if (isPlaying) {
                        player.pause()
                    } else {
                        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
                        player.play()
                    }
                },
            )
        }

        // Bottom seek bar
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) + slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) { it / 2 },
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) + slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            ) {
                val shown = scrubbing?.let { (it * duration).toLong() } ?: position
                Slider(
                    value = if (duration > 0) (scrubbing ?: (position.toFloat() / duration)).coerceIn(0f, 1f) else 0f,
                    onValueChange = {
                        // Only preview while dragging; seek once on release (seeking every tick stutters).
                        scrubbing = it
                        touch()
                    },
                    onValueChangeFinished = {
                        scrubbing?.let { player.seekTo((it * duration).toLong()) }
                        scrubbing = null
                    },
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(formatElapsed(shown), style = MaterialTheme.typography.labelMedium, color = Color.White)
                    Box(Modifier.weight(1f))
                    Text(formatElapsed(duration), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }

    if (confirmDelete) {
        DeleteDialog(
            count = 1,
            onConfirm = {
                confirmDelete = false
                onDelete(video)
                onBack()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurface(player: ExoPlayer) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                this.player = player
            }
        },
        update = { it.player = player },
        modifier = Modifier.fillMaxSize(),
    )
}

/**
 * Play/pause as shape: paused it is the idle cookie of the record button (the app's "ready" shape),
 * playing it settles into a circle. The morph rides the spatial spring.
 */
@Composable
private fun PlayPauseButton(
    playing: Boolean,
    onToggle: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val reduce = rememberReduceMotion()
    val morph = rememberMorph(MaterialShapes.Cookie9Sided, MaterialShapes.Circle)
    val progress = remember { Animatable(if (playing) 1f else 0f) }
    val spec = motion.defaultSpatialSpec<Float>()
    LaunchedEffect(playing, reduce) {
        val target = if (playing) 1f else 0f
        if (reduce) progress.snapTo(target) else progress.animateTo(target, spec)
    }
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(if (playing) R.string.player_pause else R.string.player_play)
    Box(
        Modifier
            .size(88.dp)
            .springPressed(interaction, 0.9f)
            .clip(MorphShape(morph, progress.value))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .semantics { contentDescription = label }
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = playing,
            transitionSpec = {
                (scaleIn(motion.fastSpatialSpec(), 0.5f) + fadeIn(motion.fastEffectsSpec())) togetherWith
                    (scaleOut(motion.fastSpatialSpec(), 0.5f) + fadeOut(motion.fastEffectsSpec()))
            },
            label = "play-glyph",
        ) { p ->
            Icon(
                if (p) XIcons.Pause else XIcons.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

/** Hides the system bars together with the controls; always restores them when leaving. */
@Composable
private fun ImmersiveMode(hidden: Boolean) {
    val activity = LocalActivity.current ?: return
    val controller =
        remember(activity) {
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    LaunchedEffect(hidden) {
        if (hidden) controller.hide(WindowInsetsCompat.Type.systemBars()) else controller.show(WindowInsetsCompat.Type.systemBars())
    }
    DisposableEffect(controller) { onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) } }
}
