package com.pratap.vibhor.feature.home

import com.pratap.vibhor.core.platform.media.NowPlaying
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NowPlayingTickerTest {

    @Test
    fun `playing and paused tracks expose their trimmed title`() {
        assertEquals("Track Name", track(title = "  Track Name  ").tickerTitle())
        assertEquals(
            "Paused Track",
            track(title = "Paused Track", isPlaying = false).tickerTitle(),
        )
    }

    @Test
    fun `blank metadata hides the ticker`() {
        assertNull(track(title = "").tickerTitle())
        assertNull(track(title = "   ").tickerTitle())
    }

    @Test
    fun `stopped session hides the ticker`() {
        assertNull(
            track(
                title = "Stopped Track",
                isPlaying = false,
                isStopped = true,
            ).tickerTitle(),
        )
    }

    @Test
    fun `missing session hides the ticker`() {
        assertNull((null as NowPlaying?).tickerTitle())
    }

    private fun track(
        title: String,
        isPlaying: Boolean = true,
        isStopped: Boolean = false,
    ) = NowPlaying(
        title = title,
        artist = null,
        albumArt = null,
        isPlaying = isPlaying,
        positionMs = 0L,
        durationMs = 0L,
        canPlayPause = false,
        canSkipNext = false,
        canSkipPrevious = false,
        canSeek = false,
        isStopped = isStopped,
    )
}
