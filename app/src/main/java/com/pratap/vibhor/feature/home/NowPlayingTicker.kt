package com.pratap.vibhor.feature.home

import com.pratap.vibhor.core.platform.media.NowPlaying

/**
 * Returns the real session title for the Home ticker. Missing metadata,
 * blank titles, and stopped/error sessions produce no ticker rather than a
 * placeholder track.
 */
internal fun NowPlaying?.tickerTitle(): String? {
    val snapshot = this ?: return null
    if (snapshot.isStopped) return null
    return snapshot.title.trim().takeIf { it.isNotEmpty() }
}
