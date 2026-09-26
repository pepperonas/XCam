package io.celox.xcam.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import io.celox.xcam.R
import io.celox.xcam.ui.icons.XIcons
import io.celox.xcam.ui.motion.rememberReduceMotion
import io.celox.xcam.ui.theme.Spacing

/** The XCam launcher mark (cookie + camera), tinted. Used for the hero of empty states and onboarding. */
@Composable
fun AppMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Icon(
        painter = painterResource(R.drawable.ic_app_mark),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/** A section label above a group of cards (Settings, Record). */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = Spacing.sm, bottom = Spacing.sm, top = Spacing.lg),
    )
}

/** A grouped container on surfaceContainer with the expressive extraLarge corner. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            content = content,
        )
    }
}

/** A label + supporting text column, the left side of a settings row. */
@Composable
fun RowLabel(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        if (supporting != null) {
            Text(supporting, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Text whose characters roll vertically when they change — each character is its own
 * [AnimatedContent], so on a timer tick only the digits that actually changed move (the seconds
 * digit every second, the tens every ten). Rolls on the spatial spring; a cut under reduced motion.
 */
@Composable
fun RollingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val reduce = rememberReduceMotion()
    val spatial = MaterialTheme.motionScheme.fastSpatialSpec<androidx.compose.ui.unit.IntOffset>()
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    Row(modifier = modifier) {
        // Keyed by position from the right, so "9:59" → "10:00" keeps the seconds in place.
        text.forEachIndexed { index, ch ->
            key(text.length - index) {
                AnimatedContent(
                    targetState = ch,
                    transitionSpec = {
                        if (reduce) {
                            fadeIn(snap()) togetherWith fadeOut(snap())
                        } else {
                            (slideInVertically(spatial) { it / 2 } + fadeIn(effects)) togetherWith
                                (slideOutVertically(spatial) { -it / 2 } + fadeOut(effects))
                        }
                    },
                    label = "roll",
                ) { c ->
                    Text(c.toString(), style = style, color = color)
                }
            }
        }
    }
}

/** A video frame thumbnail (Coil + VideoFrameDecoder), 1 s in so it does not show a black first frame. */
@Composable
fun VideoThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    durationLabel: String? = null,
) {
    val context = LocalContext.current
    Box(
        modifier =
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Icon(
            XIcons.Videocam,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.Center),
        )
        AsyncImage(
            model =
            ImageRequest.Builder(context)
                .data(uri)
                .videoFrameMillis(1000)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (!durationLabel.isNullOrEmpty()) {
            Text(
                text = durationLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}
