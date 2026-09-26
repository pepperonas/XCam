package io.celox.xcam.ui.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

/**
 * A [Shape] that renders the point [progress] along a [Morph] between two [RoundedPolygon]s. The
 * polygons are normalized to a 0..1 box and scaled to the composable's size, so the same morph fits
 * any button. Progress may run slightly outside 0..1: the expressive spatial spring overshoots, and
 * letting the shape overshoot with it is what makes the morph feel physical.
 */
class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = morph.toPath(progress.coerceIn(-0.15f, 1.15f), android.graphics.Path()).asComposePath()
        path.transform(Matrix().apply { scale(size.width, size.height) })
        return Outline.Generic(path)
    }
}

/** Remember a [Morph] between two polygons, normalized for [MorphShape]. */
@Composable
fun rememberMorph(
    start: RoundedPolygon,
    end: RoundedPolygon,
): Morph = remember(start, end) { Morph(start.normalized(), end.normalized()) }
