package io.celox.xcam.ui.theme

import org.junit.Assert.*
import org.junit.Test

class ColorTest {

    @Test
    fun `amber colors are in correct order of brightness`() {
        // Amber80 should be lightest (highest value), Amber20 darkest
        assertTrue(Amber80.red > Amber40.red || Amber80.green > Amber40.green)
    }

    @Test
    fun `dark surface colors get progressively lighter`() {
        // DarkBackground < DarkSurface < DarkSurfaceVariant < DarkSurfaceHigh
        assertTrue(DarkBackground.red < DarkSurface.red)
        assertTrue(DarkSurface.red < DarkSurfaceVariant.red)
        assertTrue(DarkSurfaceVariant.red < DarkSurfaceHigh.red)
    }

    @Test
    fun `glass colors have alpha transparency`() {
        assertTrue(SurfaceGlass.alpha < 1.0f)
        assertTrue(SurfaceGlassStroke.alpha < 1.0f)
        // Stroke should be more visible than glass
        assertTrue(SurfaceGlassStroke.alpha > SurfaceGlass.alpha)
    }

    @Test
    fun `recording glow has reduced alpha`() {
        assertTrue(RecordingAmberGlow.alpha < 1.0f)
        assertTrue(RecordingAmber.alpha == 1.0f)
    }

    @Test
    fun `semantic colors are distinct`() {
        val colors = setOf(
            SuccessGreen.value,
            WarningAmber.value,
            ErrorRed.value,
            InfoBlue.value
        )
        assertEquals(4, colors.size)
    }

    @Test
    fun `onAmber is black for contrast`() {
        assertEquals(0f, OnAmber.red, 0.01f)
        assertEquals(0f, OnAmber.green, 0.01f)
        assertEquals(0f, OnAmber.blue, 0.01f)
    }

    @Test
    fun `gray values form a gradient`() {
        assertTrue(Gray90.red > Gray80.red)
        assertTrue(Gray80.red > Gray60.red)
        assertTrue(Gray60.red > Gray40.red)
        assertTrue(Gray40.red > Gray20.red)
    }
}
