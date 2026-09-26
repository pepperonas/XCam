package io.celox.xcam.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import io.celox.xcam.R
import io.celox.xcam.ui.Permissions
import io.celox.xcam.ui.components.rememberHaptics
import io.celox.xcam.ui.icons.XIcons
import io.celox.xcam.ui.motion.MorphShape
import io.celox.xcam.ui.motion.rememberMorph
import io.celox.xcam.ui.motion.rememberReduceMotion
import io.celox.xcam.ui.theme.Sizes
import io.celox.xcam.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.floor

private enum class Page(val title: Int, val text: Int) {
    WELCOME(R.string.onboarding_welcome_title, R.string.onboarding_welcome_text),
    CAMERA(R.string.onboarding_camera_title, R.string.onboarding_camera_text),
    AUDIO(R.string.onboarding_audio_title, R.string.onboarding_audio_text),
    READY(R.string.onboarding_ready_title, R.string.onboarding_ready_text),
}

/** One shape per page; the hero morphs between neighbours *with the swipe*, not after it. */
private val PAGE_SHAPES: List<RoundedPolygon> =
    listOf(MaterialShapes.Cookie9Sided, MaterialShapes.Sunny, MaterialShapes.Clover4Leaf, MaterialShapes.SoftBurst)

@Composable
fun OnboardingScreen(
    permissions: Permissions,
    onRequestCamera: () -> Unit,
    onRequestAudio: () -> Unit,
    onComplete: () -> Unit,
) {
    val pages = Page.entries
    // Swiping is off: the camera page must not be skippable, and buttons are the only way forward.
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val page = pages[pager.currentPage]

    fun goTo(target: Int) {
        scope.launch { pager.animateScrollToPage(target) }
    }

    // Back walks the pages backwards; only on the first page does it leave the app.
    BackHandler(enabled = pager.currentPage > 0) { goTo(pager.currentPage - 1) }

    // A freshly granted permission moves on by itself.
    LaunchedEffect(permissions.camera) {
        if (permissions.camera && pager.currentPage == Page.CAMERA.ordinal) {
            haptics.confirm()
            goTo(Page.AUDIO.ordinal)
        }
    }
    LaunchedEffect(permissions.audio) {
        if (permissions.audio && pager.currentPage == Page.AUDIO.ordinal) {
            haptics.confirm()
            goTo(Page.READY.ordinal)
        }
    }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = Spacing.xxl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.End) {
                if (page == Page.WELCOME) {
                    // "Skip" skips the introduction, never the required permission.
                    TextButton(onClick = { goTo(Page.CAMERA.ordinal) }) { Text(stringResource(R.string.onboarding_skip)) }
                }
            }

            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                HeroShape(pagePosition = { pager.currentPage + pager.currentPageOffsetFraction }, page = page)
            }

            HorizontalPager(
                state = pager,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxWidth(),
            ) { index ->
                val p = pages[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                    Modifier.graphicsLayer {
                        // Text parallax: pages drift and fade as they leave.
                        val offset = (pager.currentPage - index + pager.currentPageOffsetFraction).absoluteValue
                        alpha = 1f - offset.coerceIn(0f, 1f)
                    },
                ) {
                    Text(
                        stringResource(p.title),
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        stringResource(p.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
            PageIndicator(count = pages.size, current = pager.currentPage)
            Spacer(Modifier.height(Spacing.xxl))

            val granted =
                when (page) {
                    Page.CAMERA -> permissions.camera
                    Page.AUDIO -> permissions.audio
                    else -> false
                }
            Button(
                onClick = {
                    when (page) {
                        Page.WELCOME -> goTo(Page.CAMERA.ordinal)
                        Page.CAMERA -> if (granted) goTo(Page.AUDIO.ordinal) else onRequestCamera()
                        Page.AUDIO -> if (granted) goTo(Page.READY.ordinal) else onRequestAudio()
                        Page.READY -> {
                            haptics.confirm()
                            onComplete()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(Sizes.buttonHeight),
            ) {
                AnimatedContent(targetState = page to granted, label = "cta") { (p, g) ->
                    Text(
                        stringResource(
                            when {
                                p == Page.READY -> R.string.onboarding_done
                                (p == Page.CAMERA || p == Page.AUDIO) && !g -> R.string.onboarding_grant
                                else -> R.string.onboarding_next
                            },
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            // Microphone and notifications are optional — the page can be left without them.
            Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                if (page == Page.AUDIO && !granted) {
                    TextButton(onClick = { goTo(Page.READY.ordinal) }) { Text(stringResource(R.string.onboarding_next)) }
                }
            }
        }
    }
}

/**
 * The hero: a container that morphs between the pages' shapes as a continuous function of the pager
 * position, with the page's glyph swapping in the middle.
 */
@Composable
private fun HeroShape(
    pagePosition: () -> Float,
    page: Page,
) {
    val motion = MaterialTheme.motionScheme
    // Only the pair of neighbouring shapes changes in composition (a few times per swipe); the morph
    // fraction and rotation are read in the draw phase, so dragging never recomposes.
    val from by remember { derivedStateOf { floor(pagePosition()).toInt().coerceIn(0, PAGE_SHAPES.lastIndex) } }
    val to = (from + 1).coerceAtMost(PAGE_SHAPES.lastIndex)
    val morph = rememberMorph(PAGE_SHAPES[from], PAGE_SHAPES[to])
    Box(
        Modifier
            .size(184.dp)
            .graphicsLayer {
                val position = pagePosition()
                rotationZ = position * 45f
                shape = MorphShape(morph, (position - from).coerceIn(0f, 1f))
                clip = true
            }.background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (scaleIn(motion.defaultSpatialSpec(), 0.5f) + fadeIn(motion.defaultEffectsSpec())) togetherWith
                    (scaleOut(motion.fastSpatialSpec(), 0.5f) + fadeOut(motion.fastEffectsSpec()))
            },
            // Counter-rotate the glyph so it stays upright while the shape turns.
            modifier = Modifier.graphicsLayer { rotationZ = -pagePosition() * 45f },
            label = "hero-glyph",
        ) { p ->
            val tint = MaterialTheme.colorScheme.onPrimaryContainer
            when (p) {
                Page.WELCOME ->
                    Icon(painterResource(R.drawable.ic_app_mark), contentDescription = null, tint = tint, modifier = Modifier.size(96.dp))
                else -> Icon(heroIcon(p), contentDescription = null, tint = tint, modifier = Modifier.size(72.dp))
            }
        }
    }
}

private fun heroIcon(page: Page): ImageVector =
    when (page) {
        Page.CAMERA -> XIcons.PhotoCamera
        Page.AUDIO -> XIcons.Notifications
        else -> XIcons.Check
    }

@Composable
private fun PageIndicator(
    count: Int,
    current: Int,
) {
    val reduce = rememberReduceMotion()
    val label = stringResource(R.string.onboarding_page, current + 1, count)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.semantics { contentDescription = label },
    ) {
        repeat(count) { i ->
            val active = i == current
            val width by animateDpAsState(
                if (active) 28.dp else 8.dp,
                if (reduce) snap() else MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "dot-w",
            )
            val color by animateColorAsState(
                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "dot-c",
            )
            Box(
                Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
