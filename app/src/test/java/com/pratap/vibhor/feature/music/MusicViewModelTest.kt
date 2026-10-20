package com.pratap.vibhor.feature.music

import com.pratap.vibhor.core.platform.media.MediaControls
import com.pratap.vibhor.core.platform.media.NowPlaying
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ui state mirrors the real session snapshot`() = runTest(dispatcher) {
        val controls = FakeMediaControls()
        val viewModel = MusicViewModel(controls)
        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.track)

        controls.track.value = NowPlaying(
            title = "Test Track",
            artist = "Test Artist",
            albumArt = null,
            isPlaying = true,
            positionMs = 12_000L,
            durationMs = 200_000L,
            canPlayPause = true,
            canSkipNext = true,
            canSkipPrevious = false,
        )
        advanceUntilIdle()

        val track = viewModel.uiState.value.track
        assertEquals("Test Track", track?.title)
        assertEquals("Test Artist", track?.artist)
        assertEquals(true, track?.isPlaying)
        assertEquals(12_000L, track?.positionMs)
        assertEquals(200_000L, track?.durationMs)

        collection.cancel()
    }

    @Test
    fun `transport actions are forwarded to the session`() = runTest(dispatcher) {
        val controls = FakeMediaControls()
        val viewModel = MusicViewModel(controls)
        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.playPause()
        viewModel.next()
        viewModel.previous()

        assertEquals(1, controls.playPauseCalls)
        assertEquals(1, controls.nextCalls)
        assertEquals(1, controls.previousCalls)

        collection.cancel()
    }

    @Test
    fun `refreshAccess re-reads access and refreshes the session layer`() =
        runTest(dispatcher) {
            val controls = FakeMediaControls(access = false)
            val viewModel = MusicViewModel(controls)
            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            // The constructor checks access and asks the session layer to bind.
            assertEquals(1, controls.refreshCalls)
            assertFalse(viewModel.uiState.value.hasAccess)

            controls.access = true
            viewModel.refreshAccess()
            advanceUntilIdle()

            assertEquals(2, controls.refreshCalls)
            assertEquals(true, viewModel.uiState.value.hasAccess)

            collection.cancel()
        }

    private class FakeMediaControls(
        var access: Boolean = true,
    ) : MediaControls {
        val track = MutableStateFlow<NowPlaying?>(null)
        override val nowPlaying: StateFlow<NowPlaying?> = track
        var refreshCalls = 0
        var playPauseCalls = 0
        var nextCalls = 0
        var previousCalls = 0

        override fun hasAccess(): Boolean = access

        override fun refresh() {
            refreshCalls++
        }

        override fun playPause() {
            playPauseCalls++
        }

        override fun next() {
            nextCalls++
        }

        override fun previous() {
            previousCalls++
        }
    }
}
