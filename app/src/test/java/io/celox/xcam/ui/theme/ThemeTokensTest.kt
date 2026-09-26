package io.celox.xcam.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTokensTest {
    private fun contrast(
        a: Color,
        b: Color,
    ): Double {
        val la = a.luminance() + 0.05
        val lb = b.luminance() + 0.05
        return maxOf(la, lb) / minOf(la, lb)
    }

    /** Every text-on-colour pair the app uses must meet WCAG AA for body text (4.5:1). */
    private fun pairs(s: ColorScheme) =
        mapOf(
            "onPrimary/primary" to (s.onPrimary to s.primary),
            "onPrimaryContainer/primaryContainer" to (s.onPrimaryContainer to s.primaryContainer),
            "onSecondaryContainer/secondaryContainer" to (s.onSecondaryContainer to s.secondaryContainer),
            "onError/error" to (s.onError to s.error),
            "onErrorContainer/errorContainer" to (s.onErrorContainer to s.errorContainer),
            "onSurface/surface" to (s.onSurface to s.surface),
            "onSurfaceVariant/surfaceContainer" to (s.onSurfaceVariant to s.surfaceContainer),
            "onSurfaceVariant/surfaceContainerHighest" to (s.onSurfaceVariant to s.surfaceContainerHighest),
            "primary/surface" to (s.primary to s.surface),
        )

    @Test
    fun `light scheme meets AA contrast`() = assertAllPairs(XCamLightColors)

    @Test
    fun `dark scheme meets AA contrast`() = assertAllPairs(XCamDarkColors)

    private fun assertAllPairs(scheme: ColorScheme) {
        pairs(scheme).forEach { (name, pair) ->
            val ratio = contrast(pair.first, pair.second)
            assertTrue("$name is only %.2f:1".format(ratio), ratio >= 4.5)
        }
    }

    @Test
    fun `surface containers step up in tone in the dark scheme`() {
        val s = XCamDarkColors
        val ladder =
            listOf(s.surfaceContainerLowest, s.surfaceContainerLow, s.surfaceContainer, s.surfaceContainerHigh, s.surfaceContainerHighest)
        assertTrue(ladder.zipWithNext().all { (a, b) -> a.luminance() < b.luminance() })
    }

    @Test
    fun `the brand primary reads as red in both schemes`() {
        listOf(XCamLightColors.primary, XCamDarkColors.primary).forEach {
            assertTrue("$it is not red-dominant", it.red > it.green && it.red > it.blue)
        }
    }

    @Test
    fun `spacing and shape scales are strictly ascending`() {
        val spacing = listOf(Spacing.xs, Spacing.sm, Spacing.md, Spacing.lg, Spacing.xl, Spacing.xxl, Spacing.xxxl)
        assertTrue(spacing.zipWithNext().all { (a, b) -> a < b })
        assertTrue(Sizes.recordRing > Sizes.recordButton)
        assertTrue(Sizes.touchTarget.value >= 48f)
    }
}
