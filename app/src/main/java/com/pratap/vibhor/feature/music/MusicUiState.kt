package com.pratap.vibhor.feature.music

import com.pratap.vibhor.core.platform.media.NowPlaying

/**
 * Music screen state. [track] is null whenever no session can be read, which
 * includes the phase before notification access is granted.
 */
data class MusicUiState(
    val hasAccess: Boolean = false,
    val track: NowPlaying? = null,
)
