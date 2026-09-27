package io.celox.xcam.service

import androidx.camera.core.CameraSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DualCameraTest {
    private data class Cam(val id: String, val facing: Int)

    private val back = Cam("0", CameraSelector.LENS_FACING_BACK)
    private val front = Cam("1", CameraSelector.LENS_FACING_FRONT)
    private val tele = Cam("2", CameraSelector.LENS_FACING_BACK)

    private fun pair(vararg combos: List<Cam>) = DualCamera.backFrontPair(combos.toList()) { it.facing }

    @Test
    fun `a back and front combination gives back first, front second`() {
        assertEquals(back to front, pair(listOf(front, back)))
    }

    @Test
    fun `no concurrent combinations means no dual recording`() {
        assertNull(pair())
    }

    @Test
    fun `two back cameras at once are not front plus back`() {
        assertNull(pair(listOf(back, tele)))
    }

    @Test
    fun `the first combination that has both facings is used`() {
        assertEquals(tele to front, pair(listOf(back, tele), listOf(tele, front), listOf(back, front)))
    }

    @Test
    fun `an external camera in the combination is ignored`() {
        val usb = Cam("9", CameraSelector.LENS_FACING_EXTERNAL)
        assertEquals(back to front, pair(listOf(usb, back, front)))
    }

    @Test
    fun `the back camera fills the frame`() {
        val p = DualCamera.PRIMARY
        assertEquals(1f, p.alpha)
        assertEquals(1f to 1f, p.scale.first to p.scale.second)
        assertEquals(0f to 0f, p.offset.first to p.offset.second)
    }

    @Test
    fun `the front inset is a smaller, opaque picture that stays fully inside the frame`() {
        val inset = DualCamera.INSET
        assertEquals(1f, inset.alpha)
        val (sx, sy) = inset.scale.first to inset.scale.second
        val (ox, oy) = inset.offset.first to inset.offset.second
        assertTrue(sx in 0.2f..0.5f && sy in 0.2f..0.5f)
        // Normalized device coordinates: the inset spans offset ± scale and must stay within [-1, 1].
        assertTrue("x", abs(ox) + sx <= 1f)
        assertTrue("y", abs(oy) + sy <= 1f)
        assertTrue("sits in a corner, off centre", abs(ox) > 0.3f && abs(oy) > 0.3f)
    }
}
