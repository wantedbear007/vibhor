package com.pratap.vibhor.core.time

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTimeTest {

    @Test
    fun `formats positions under an hour as minutes and seconds`() {
        assertEquals("0:00", formatPlaybackTime(0L))
        assertEquals("0:59", formatPlaybackTime(59_000L))
        assertEquals("1:01", formatPlaybackTime(61_000L))
        assertEquals("59:59", formatPlaybackTime(3_599_000L))
    }

    @Test
    fun `formats positions past an hour with an hours field`() {
        assertEquals("1:00:00", formatPlaybackTime(3_600_000L))
        assertEquals("2:03:05", formatPlaybackTime(7_385_000L))
    }

    @Test
    fun `negative positions clamp to zero`() {
        assertEquals("0:00", formatPlaybackTime(-5_000L))
    }
}
