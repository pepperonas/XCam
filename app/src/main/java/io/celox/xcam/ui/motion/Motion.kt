package io.celox.xcam.ui.motion

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * True when the system animation scale is 0 (accessibility "remove animations"). All physics-heavy
 * motion is guarded on this so the app stays fully usable with reduced motion.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * A staggered spring "entrance": the element fades in while rising and settling from a slight
 * under-scale, delayed by [index]. Spatial parts ride the expressive spatial spring (a little
 * overshoot is intended), the fade the effects spring (never overshoots). No-op under reduced motion.
 */
fun Modifier.springEntrance(
    index: Int = 0,
    staggerMillis: Int = 55,
): Modifier =
    composed {
        if (rememberReduceMotion()) return@composed this

        val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
        val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        val rise = remember { Animatable(0f) }
        val fade = remember { Animatable(0f) }
        val currentIndex by rememberUpdatedState(index)

        LaunchedEffect(Unit) {
            delay(currentIndex.toLong() * staggerMillis)
            coroutineScope {
                launch { fade.animateTo(1f, effectsSpec) }
                rise.animateTo(1f, spatialSpec)
            }
        }

        this
            .alpha(fade.value.coerceIn(0f, 1f))
            .graphicsLayer {
                val p = rise.value
                translationY = (1f - p) * 28.dp.toPx()
                val s = 0.92f + 0.08f * p
                scaleX = s
                scaleY = s
            }
    }

