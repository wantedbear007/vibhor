package com.pratap.vibhor.core.platform.media

import kotlinx.coroutines.flow.StateFlow

/**
 * Reads the active media session and forwards transport commands to it.
 *
 * On stock Android 9 an ordinary app can only enumerate media sessions when
 * the user has granted Vibhor notification access. Until then [nowPlaying]
 * stays null and every action is a safe no-op, so the screen can show an
 * honest idle state without faking playback.
 */
interface MediaControls {

    /** Latest real snapshot, or null when no session can be read. */
    val nowPlaying: StateFlow<NowPlaying?>

    /** True when Vibhor is an enabled notification listener. */
    fun hasAccess(): Boolean

    /** Re-check access and re-bind to the current sessions (after Settings). */
    fun refresh()

    fun playPause()

    fun next()

    fun previous()

    /** Releases platform listeners. Called when the view model is cleared. */
    fun release() = Unit
}
