package io.celox.xcam.util

import java.util.Locale

/** `m:ss` below an hour, `h:mm:ss` above; empty for an unknown (≤ 0) duration. Used for clip lengths. */
fun formatDuration(millis: Long): String {
    if (millis <= 0) return ""
    return formatClock(millis, padMinutes = false)
}

/**
 * The running recording timer: always at least `00:00`, `h:mm:ss` from one hour on. Minutes are
 * zero-padded so the width does not jump when the timer passes 9:59.
 */
fun formatElapsed(millis: Long): String = formatClock(millis.coerceAtLeast(0), padMinutes = true)

private fun formatClock(
    millis: Long,
    padMinutes: Boolean,
): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds / 60) % 60
    val seconds = totalSeconds % 60
    // Locale.ROOT: timers are always Latin digits and a colon, whatever the UI language.
    return when {
        hours > 0 -> String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        padMinutes -> String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        else -> String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}
