package com.pratap.vibhor.core.platform.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Real media-session reader for Android 9.
 *
 * Session enumeration requires Vibhor to be an enabled notification listener
 * (see [VibhorNotificationListenerService]); the service itself never reads a
 * notification, it only makes the package trusted by MediaSessionManager.
 * All binder calls are wrapped so a disappearing session can never crash the
 * launcher.
 */
class AndroidMediaControls(context: Context) : MediaControls {

    private val appContext = context.applicationContext
    private val sessionManager =
        appContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    private val listenerComponent =
        ComponentName(appContext, VibhorNotificationListenerService::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _nowPlaying = MutableStateFlow<NowPlaying?>(null)
    override val nowPlaying: StateFlow<NowPlaying?> = _nowPlaying.asStateFlow()

    private var listening = false
    private var controllers: List<MediaController> = emptyList()
    private var active: MediaController? = null
    private var metadata: MediaMetadata? = null
    private var albumArt: Bitmap? = null
    private var ticker: Job? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            reselect()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            refreshMetadata()
            publish()
        }

        override fun onSessionDestroyed() {
            active = null
            metadata = null
            albumArt = null
            reloadSessions()
        }
    }

    private val sessionsChanged =
        MediaSessionManager.OnActiveSessionsChangedListener { sessions ->
            bindTo(sessions.orEmpty())
        }

    override fun hasAccess(): Boolean = NotificationAccess.isGranted(appContext)

    override fun refresh() {
        if (!hasAccess()) {
            detach()
            return
        }
        if (!listening) {
            listening = runCatching {
                sessionManager?.addOnActiveSessionsChangedListener(
                    sessionsChanged,
                    listenerComponent,
                )
            }.isSuccess && sessionManager != null
        }
        reloadSessions()
    }

    override fun playPause() {
        val controller = active ?: return
        val playing = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        runCatching {
            if (playing) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        }
    }

    override fun next() {
        val controller = active ?: return
        runCatching { controller.transportControls.skipToNext() }
    }

    override fun previous() {
        val controller = active ?: return
        runCatching { controller.transportControls.skipToPrevious() }
    }

    override fun seekTo(positionMs: Long) {
        val controller = active ?: return
        val supportsSeek =
            controller.playbackState?.actions?.and(PlaybackState.ACTION_SEEK_TO) != 0L
        if (!supportsSeek) return
        runCatching { controller.transportControls.seekTo(positionMs.coerceAtLeast(0L)) }
    }

    override fun release() {
        detach()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun reloadSessions() {
        val manager = sessionManager ?: return
        val sessions = runCatching {
            manager.getActiveSessions(listenerComponent)
        }.getOrNull().orEmpty()
        bindTo(sessions)
    }

    private fun bindTo(sessions: List<MediaController>) {
        controllers.forEach { controller ->
            runCatching { controller.unregisterCallback(controllerCallback) }
        }
        controllers = sessions
        controllers.forEach { controller ->
            runCatching { controller.registerCallback(controllerCallback) }
        }
        active = null
        reselect()
    }

    /**
     * Prefers whichever session is playing, then keeps the current session
     * while it is still listed, then the first one.
     */
    private fun reselect() {
        val playing = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        }
        val target = playing
            ?: active?.takeIf { current -> controllers.any { it === current } }
            ?: controllers.firstOrNull()
        if (target !== active) {
            active = target
            refreshMetadata()
        }
        publish()
    }

    private fun refreshMetadata() {
        val value = runCatching { active?.metadata }.getOrNull()
        metadata = value
        albumArt = value?.let(::extractArt)
    }

    private fun extractArt(value: MediaMetadata): Bitmap? =
        value.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: value.getBitmap(MediaMetadata.METADATA_KEY_ART)

    private fun publish() {
        val controller = active
        val value = metadata
        if (controller == null || value == null) {
            _nowPlaying.value = null
            stopTicker()
            return
        }
        // A live session without a title is still real; the screen shows a
        // neutral label instead of hiding the transport.
        val title = value.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?.trim()
            .orEmpty()
            .ifEmpty {
                value.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                    ?.trim()
                    .orEmpty()
            }

        val state = runCatching { controller.playbackState }.getOrNull()
        val actions = state?.actions ?: 0L
        val duration = value.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(0L)
        val artist = value.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: value.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            ?: value.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

        _nowPlaying.value = NowPlaying(
            title = title,
            artist = artist,
            albumArt = albumArt,
            isPlaying = state?.state == PlaybackState.STATE_PLAYING,
            positionMs = positionOf(state, duration),
            durationMs = duration,
            canPlayPause = actions and PLAY_PAUSE_ACTIONS != 0L,
            canSkipNext = actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
            canSkipPrevious = actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
            canSeek = actions and PlaybackState.ACTION_SEEK_TO != 0L,
        )

        if (state?.state == PlaybackState.STATE_PLAYING) {
            startTicker()
        } else {
            stopTicker()
        }
    }

    private fun positionOf(state: PlaybackState?, durationMs: Long): Long {
        if (state == null) return 0L
        val elapsed = if (state.state == PlaybackState.STATE_PLAYING) {
            ((SystemClock.elapsedRealtime() - state.lastPositionUpdateTime) *
                state.playbackSpeed).toLong().coerceAtLeast(0L)
        } else {
            0L
        }
        val position = state.position.coerceAtLeast(0L) + elapsed
        return if (durationMs > 0L) position.coerceIn(0L, durationMs) else position
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                delay(POSITION_TICK_MILLIS)
                publish()
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun detach() {
        if (listening) {
            runCatching { sessionManager?.removeOnActiveSessionsChangedListener(sessionsChanged) }
            listening = false
        }
        controllers.forEach { controller ->
            runCatching { controller.unregisterCallback(controllerCallback) }
        }
        controllers = emptyList()
        active = null
        metadata = null
        albumArt = null
        _nowPlaying.value = null
        stopTicker()
    }

    private companion object {
        const val POSITION_TICK_MILLIS = 500L

        const val PLAY_PAUSE_ACTIONS =
            PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE
    }
}

/** Reads the enabled-notification-listener setting without extra permissions. */
internal object NotificationAccess {

    fun isGranted(context: Context): Boolean {
        val flat = runCatching {
            Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners",
            )
        }.getOrNull() ?: return false
        return flat.split(':').any { entry ->
            ComponentName.unflattenFromString(entry)?.packageName == context.packageName
        }
    }
}
