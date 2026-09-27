package io.celox.xcam.ui.components

import android.app.Activity
import android.view.HapticFeedbackConstants
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class HapticsTest {
    private fun lastAfter(action: Haptics.() -> Unit): Int {
        val view = View(Robolectric.buildActivity(Activity::class.java).setup().get())
        Haptics(view).action()
        return shadowOf(view).lastHapticFeedbackPerformed()
    }

    @Test
    @Config(sdk = [34])
    fun `start and stop confirm, refusals reject`() {
        assertEquals(HapticFeedbackConstants.CONFIRM, lastAfter { confirm() })
        assertEquals(HapticFeedbackConstants.REJECT, lastAfter { reject() })
        assertEquals(HapticFeedbackConstants.LONG_PRESS, lastAfter { longPress() })
    }

    @Test
    @Config(sdk = [34])
    fun `android 14 gets the semantic toggle and segment constants`() {
        assertEquals(HapticFeedbackConstants.TOGGLE_ON, lastAfter { toggleOn() })
        assertEquals(HapticFeedbackConstants.TOGGLE_OFF, lastAfter { toggleOff() })
        assertEquals(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK, lastAfter { segmentTick() })
    }

    @Test
    @Config(sdk = [33])
    fun `android 13 falls back to a clock tick for constants it does not know`() {
        assertEquals(HapticFeedbackConstants.CLOCK_TICK, lastAfter { toggleOn() })
        assertEquals(HapticFeedbackConstants.CLOCK_TICK, lastAfter { toggleOff() })
        assertEquals(HapticFeedbackConstants.CLOCK_TICK, lastAfter { segmentTick() })
        assertEquals(HapticFeedbackConstants.CONFIRM, lastAfter { confirm() })
    }
}
