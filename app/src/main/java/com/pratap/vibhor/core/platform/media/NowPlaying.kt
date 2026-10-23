package com.pratap.vibhor.core.platform.media

import android.graphics.Bitmap

/**
 * A snapshot of the active media session. Every field comes from the real
 * session; nothing is synthesised. A null snapshot (see [MediaControls])
 * means there is no readable session or notification access is missing.
 */
data class NowPlaying(
    val title: String,
    val artist: String?,
    val albumArt: Bitmap?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val canPlayPause: Boolean,
    val canSkipNext: Boolean,
    val canSkipPrevious: Boolean,
    val canSeek: Boolean,
    val isStopped: Boolean = false,
)
