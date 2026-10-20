package com.pratap.vibhor.core.time

import java.util.Locale

/**
 * Formats a media position or duration as `m:ss`, switching to `h:mm:ss`
 * beyond an hour. Negative values clamp to zero so a malformed session
 * position can never render as a negative countdown.
 */
fun formatPlaybackTime(positionMs: Long): String {
    val totalSeconds = positionMs.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
