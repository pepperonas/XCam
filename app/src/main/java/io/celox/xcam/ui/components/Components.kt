package io.celox.xcam.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.celox.xcam.ui.icons.*
import io.celox.xcam.ui.theme.*

// =============================================================================
// XCam Professional UI Components
// =============================================================================

/**
 * Animated Recording Indicator
 * Displays a pulsing amber dot with glow effect and rotating arc when recording
 */
@Composable
fun AnimatedRecordingIndicator(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")

    // Pulse animation for scale
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.15f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isRecording) 800 else 2000,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // Glow animation for alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isRecording) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_animation"
    )

    // Rotating arc angle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_animation"
    )

    // Color transition
    val iconColor by animateColorAsState(
        targetValue = if (isRecording) RecordingAmber else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "color_animation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Box(
            modifier = Modifier
                .size(size * 1.3f)
                .scale(scale)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isRecording) listOf(
                                RecordingAmber.copy(alpha = glowAlpha * 0.5f),
                                RecordingAmber.copy(alpha = glowAlpha * 0.2f),
                                Color.Transparent
                            ) else listOf(
                                Amber40.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        // Rotating gradient arc when recording
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(size * 1.1f)
                    .drawBehind {
                        rotate(rotation) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        RecordingAmber.copy(alpha = 0.6f),
                                        RecordingAmber,
                                        Color.Transparent
                                    )
                                ),
                                startAngle = 0f,
                                sweepAngle = 120f,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
            )
        }

        // Main icon
        Icon(
            imageVector = if (isRecording) Icons.Filled.FiberManualRecordCustom else Icons.Filled.VideocamCustom,
            contentDescription = if (isRecording) "Recording" else "Ready",
            modifier = Modifier
                .size(size)
                .scale(if (isRecording) scale else scale),
            tint = iconColor
        )
    }
}

/**
 * Animated Record Button
 * Large FAB-style button with press animation and haptic feedback
 */
@Composable
fun AnimatedRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    // Background color transition
    val containerColor by animateColorAsState(
        targetValue = if (isRecording) RecordingAmber else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "button_color"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        interactionSource = interactionSource
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.StopCustom else Icons.Filled.PlayArrowCustom,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isRecording) "Stop Recording" else "Start Recording",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Glassmorphic Card
 * Card with semi-transparent background and subtle border for glass effect
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceGlass
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfaceGlass,
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            content = content
        )
    }
}

/**
 * Gradient Card
 * Card with subtle gradient background and elevation
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassmorphicCard(
        modifier = modifier,
        onClick = onClick,
        content = content
    )
}

/**
 * Status Chip
 * Small chip displaying recording status with animation
 */
@Composable
fun StatusChip(
    text: String,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_chip")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chip_alpha"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) RecordingAmber else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(300),
        label = "chip_bg_color"
    )

    Surface(
        modifier = modifier.graphicsLayer { this.alpha = if (isActive) alpha else 1f },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Setting Section Header
 * Styled section header for settings screen
 */
@Composable
fun SettingSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(bottom = 12.dp)
    )
}

/**
 * Animated Icon Button
 * Icon button with scale animation on press
 */
@Composable
fun AnimatedIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_button_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/**
 * Empty State Component
 * Displays a centered message when content is empty
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Config Display Row
 * Displays a label-value pair with consistent styling
 */
@Composable
fun ConfigDisplayRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Shimmer Effect for loading states
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        DarkSurfaceVariant,
                        DarkSurfaceHigh,
                        DarkSurfaceVariant,
                    ),
                    start = Offset(offset * 300f, 0f),
                    end = Offset((offset + 1f) * 300f, 0f)
                )
            )
    )
}
