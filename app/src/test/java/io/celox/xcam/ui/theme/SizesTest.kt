package io.celox.xcam.ui.theme

import androidx.compose.ui.unit.dp
import io.celox.xcam.ui.motion.ScreenMotion
import org.junit.Assert.assertTrue
import org.junit.Test

class SizesTest {
    @Test
    fun `touch targets meet the 48 dp minimum`() {
        assertTrue(Sizes.touchTarget >= 48.dp)
        assertTrue(Sizes.buttonHeight >= Sizes.touchTarget)
        assertTrue(Sizes.recordButton >= Sizes.touchTarget)
    }

    @Test
    fun `the progress ring surrounds the record button with room for the wave`() {
        assertTrue(Sizes.recordRing > Sizes.recordButton)
        assertTrue(Sizes.recordRing - Sizes.recordButton >= 16.dp)
    }

    @Test
    fun `thumbnails are 16 to 9`() {
        assertTrue(Sizes.thumbnailWidth.value * 9 == Sizes.thumbnailHeight.value * 16)
    }

    @Test
    fun `icons step up and the empty-state mark is the largest mark`() {
        assertTrue(Sizes.iconSmall < Sizes.icon)
        assertTrue(Sizes.heroMark < Sizes.emptyMark)
    }

    @Test
    fun `screen edges use a step of the spacing ladder`() {
        val ladder = listOf(Spacing.xs, Spacing.sm, Spacing.md, Spacing.lg, Spacing.xl, Spacing.xxl, Spacing.xxxl)
        assertTrue(ladder.all { it.value % 4f == 0f })
    }

    @Test
    fun `screen motion stays a hint - small slides, scales just under one`() {
        assertTrue(ScreenMotion.TAB_SLIDE_FRACTION in 0.01f..0.15f)
        assertTrue(ScreenMotion.HIERARCHY_SLIDE_FRACTION in 0.01f..0.2f)
        assertTrue(ScreenMotion.ENTER_SCALE in 0.9f..0.999f)
        assertTrue(ScreenMotion.PARENT_SCALE in 0.9f..0.999f)
    }
}
