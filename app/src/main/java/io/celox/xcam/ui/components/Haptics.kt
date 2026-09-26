package io.celox.xcam.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Semantic haptics. Each call names *what happened* and maps it to the matching platform constant
 * (the old code used LONG_PRESS for every toggle, which is the wrong signal). The platform respects
 * the user's system haptics setting, so there is nothing to gate here.
 */
class Haptics(private val view: View) {
    /** A primary action succeeded (recording started/stopped). */
    fun confirm() = perform(HapticFeedbackConstants.CONFIRM)

    /** An action was refused or failed. */
    fun reject() = perform(HapticFeedbackConstants.REJECT)

    fun toggleOn() = perform(if (API_34) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.CLOCK_TICK)

    fun toggleOff() = perform(if (API_34) HapticFeedbackConstants.TOGGLE_OFF else HapticFeedbackConstants.CLOCK_TICK)

    /** A discrete choice changed (segmented buttons, chips). */
    fun segmentTick() =
        perform(if (API_34) HapticFeedbackConstants.SEGMENT_FREQUENT_TICK else HapticFeedbackConstants.CLOCK_TICK)

    /** Entering a mode by long-press (multi-select). */
    fun longPress() = perform(HapticFeedbackConstants.LONG_PRESS)

    private companion object {
        // TOGGLE_ON/OFF and SEGMENT_FREQUENT_TICK exist from Android 14; minSdk is 13.
        val API_34 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    private fun perform(constant: Int) {
        view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}
