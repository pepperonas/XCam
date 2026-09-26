package io.celox.xcam.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.celox.xcam.ui.motion.rememberReduceMotion

/** The expressive [LoadingIndicator] (morphing polygons) — M3 Expressive's replacement for a spinner. */
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    LoadingIndicator(modifier = modifier, color = color)
}

/**
 * A springy press effect: the element scales down on the theme's *fast* spatial spring while held and
 * bounces back when released. Reduced-motion-safe.
 */
@Composable
fun Modifier.springPressed(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.94f,
): Modifier {
    val reduce = rememberReduceMotion()
    val pressed by interactionSource.collectIsPressedAsState()
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed, reduce) {
        val target = if (pressed) pressedScale else 1f
        if (reduce) scale.snapTo(target) else scale.animateTo(target, spec)
    }
    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * A connected segmented toggle: a pill track whose selected segment fills with a spring. Colours of
 * the fill move on the spatial spring (it reads as the pill travelling), the label on the effects
 * spring (a colour must never overshoot).
 */
@Composable
fun <T> SegmentedToggle(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptics = rememberHaptics()
    val reduce = rememberReduceMotion()
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            val bg by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = if (reduce) snap() else MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "segment-bg",
            )
            val fg by animateColorAsState(
                targetValue =
                when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "segment-fg",
            )
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier =
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .springPressed(interaction)
                    .clip(CircleShape)
                    .background(bg)
                    .semantics { this.selected = isSelected }
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = enabled,
                        role = Role.RadioButton,
                    ) {
                        if (!isSelected) {
                            haptics.segmentTick()
                            onSelect(value)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = fg,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}
