package com.pratap.vibhor.feature.settings

import com.pratap.vibhor.core.platform.AccentColor
import com.pratap.vibhor.core.platform.AnimationStyle
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.HomeButtonTarget
import com.pratap.vibhor.core.platform.InMemorySettingsStore
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

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
    fun `changing scales updates ui state and persists to store`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setClockScale(1.5f)
            viewModel.setAppIconScale(0.75f)
            advanceUntilIdle()

            assertEquals(1.5f, viewModel.uiState.value.clockScale)
            assertEquals(0.75f, viewModel.uiState.value.appIconScale)
            assertEquals(1.5f, store.settings.value.clockScale)
            assertEquals(0.75f, store.settings.value.appIconScale)

            collection.cancel()
        }

    @Test
    fun `fresh settings use the installer defaults`() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(InMemorySettingsStore())

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(
            LauncherSettings.DEFAULT_CLOCK_SCALE,
            viewModel.uiState.value.clockScale,
        )
        assertEquals(LauncherSettings.DEFAULT_SCALE, viewModel.uiState.value.appIconScale)
        assertEquals(AccentColor.WHITE, viewModel.uiState.value.accentColor)
        assertEquals(LauncherBackground.BLACK, viewModel.uiState.value.background)

        collection.cancel()
    }

    @Test
    fun `reset restores half-size clock and full-size app icons`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = SettingsViewModel(store)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setClockScale(2f)
        viewModel.setAppIconScale(1.5f)
        advanceUntilIdle()
        viewModel.resetToDefaults()
        advanceUntilIdle()

        assertEquals(
            LauncherSettings.DEFAULT_CLOCK_SCALE,
            viewModel.uiState.value.clockScale,
        )
        assertEquals(LauncherSettings.DEFAULT_SCALE, viewModel.uiState.value.appIconScale)

        collection.cancel()
    }

    @Test
    fun `scales are clamped to the supported range`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = SettingsViewModel(store)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setClockScale(10f)
        viewModel.setAppIconScale(0f)
        advanceUntilIdle()

        assertEquals(2f, viewModel.uiState.value.clockScale)
        assertEquals(0.5f, viewModel.uiState.value.appIconScale)

        collection.cancel()
    }

    @Test
    fun `accent color updates ui state and persists to store`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = SettingsViewModel(store)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setAccentColor(AccentColor.TEAL)
        advanceUntilIdle()

        assertEquals(AccentColor.TEAL, viewModel.uiState.value.accentColor)
        assertEquals(AccentColor.TEAL, store.settings.value.accentColor)

        collection.cancel()
    }

    @Test
    fun `reset to defaults restores the default accent color`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = SettingsViewModel(store)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setAccentColor(AccentColor.PURPLE)
        advanceUntilIdle()
        viewModel.resetToDefaults()
        advanceUntilIdle()

        assertEquals(AccentColor.DEFAULT, viewModel.uiState.value.accentColor)

        collection.cancel()
    }

    @Test
    fun `launcher background updates ui state and persists to store`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setBackground(LauncherBackground.NAVY)
            viewModel.setBackgroundOpacity(0.4f)
            advanceUntilIdle()

            assertEquals(LauncherBackground.NAVY, viewModel.uiState.value.background)
            assertEquals(0.4f, viewModel.uiState.value.backgroundOpacity)
            assertEquals(LauncherBackground.NAVY, store.settings.value.background)
            assertEquals(0.4f, store.settings.value.backgroundOpacity)

            collection.cancel()
        }

    @Test
    fun `reset to defaults restores the default launcher background`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setBackground(LauncherBackground.OCEAN)
            viewModel.setBackgroundOpacity(0f)
            advanceUntilIdle()
            viewModel.resetToDefaults()
            advanceUntilIdle()

            assertEquals(LauncherBackground.DEFAULT, viewModel.uiState.value.background)
            assertEquals(1f, viewModel.uiState.value.backgroundOpacity)

            collection.cancel()
        }

    @Test
    fun `background opacity is clamped to the supported range`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setBackgroundOpacity(2f)
            advanceUntilIdle()
            assertEquals(1f, viewModel.uiState.value.backgroundOpacity)

            viewModel.setBackgroundOpacity(-1f)
            advanceUntilIdle()
            assertEquals(0f, viewModel.uiState.value.backgroundOpacity)

            collection.cancel()
        }

    @Test
    fun `reset to defaults restores night mode off`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = SettingsViewModel(store)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        store.setNightMode(true)
        advanceUntilIdle()
        viewModel.resetToDefaults()
        advanceUntilIdle()

        assertEquals(false, store.settings.value.nightMode)

        collection.cancel()
    }

    @Test
    fun `animation style updates ui state and persists to store`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals(AnimationStyle.DEFAULT, viewModel.uiState.value.animationStyle)

            viewModel.setAnimationStyle(AnimationStyle.PARALLAX)
            advanceUntilIdle()

            assertEquals(AnimationStyle.PARALLAX, viewModel.uiState.value.animationStyle)
            assertEquals(AnimationStyle.PARALLAX, store.settings.value.animationStyle)

            collection.cancel()
        }

    @Test
    fun `reset to defaults restores the default animation style`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setAnimationStyle(AnimationStyle.DEPTH)
            advanceUntilIdle()
            viewModel.resetToDefaults()
            advanceUntilIdle()

            assertEquals(AnimationStyle.DEFAULT, viewModel.uiState.value.animationStyle)

            collection.cancel()
        }

    @Test
    fun `home button target updates ui state and persists to store`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals(HomeButtonTarget.DEFAULT, viewModel.uiState.value.homeButtonTarget)

            viewModel.setHomeButtonTarget(HomeButtonTarget.Player)
            advanceUntilIdle()

            assertEquals(HomeButtonTarget.Player, viewModel.uiState.value.homeButtonTarget)
            assertEquals(HomeButtonTarget.Player, store.settings.value.homeButtonTarget)

            collection.cancel()
        }

    @Test
    fun `reset to defaults restores the clock home button target`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setHomeButtonTarget(HomeButtonTarget.Player)
            advanceUntilIdle()
            viewModel.resetToDefaults()
            advanceUntilIdle()

            assertEquals(HomeButtonTarget.DEFAULT, viewModel.uiState.value.homeButtonTarget)
            assertEquals(HomeButtonTarget.Clock, store.settings.value.homeButtonTarget)

            collection.cancel()
        }

    @Test
    fun `changing clock face updates ui state and persists to store`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setClockFace(ClockFace.ANALOG)
            advanceUntilIdle()

            assertEquals(ClockFace.ANALOG, viewModel.uiState.value.clockFace)
            assertEquals(ClockFace.ANALOG, store.settings.value.clockFace)

            collection.cancel()
        }

    @Test
    fun `reset to defaults restores the default clock face`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = SettingsViewModel(store)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setClockFace(ClockFace.MINIMAL)
        advanceUntilIdle()
        viewModel.resetToDefaults()
        advanceUntilIdle()

        assertEquals(ClockFace.DEFAULT, viewModel.uiState.value.clockFace)

        collection.cancel()
    }

    @Test
    fun `changing clock format updates ui state and persists to store`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setClockFormat(ClockFormat.H24)
            advanceUntilIdle()

            assertEquals(ClockFormat.H24, viewModel.uiState.value.clockFormat)
            assertEquals(ClockFormat.H24, store.settings.value.clockFormat)

            collection.cancel()
        }

    @Test
    fun `reset to defaults restores the default clock format`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = SettingsViewModel(store)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setClockFormat(ClockFormat.H12)
        advanceUntilIdle()
        viewModel.resetToDefaults()
        advanceUntilIdle()

        assertEquals(ClockFormat.DEFAULT, viewModel.uiState.value.clockFormat)

        collection.cancel()
    }

    @Test
    fun `blackout brightness updates ui state and persists to store`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals(
                LauncherSettings.DEFAULT_BLACKOUT_BRIGHTNESS,
                viewModel.uiState.value.blackoutBrightness,
            )

            viewModel.setBlackoutBrightness(0.35f)
            advanceUntilIdle()

            assertEquals(0.35f, viewModel.uiState.value.blackoutBrightness)
            assertEquals(0.35f, store.settings.value.blackoutBrightness)

            collection.cancel()
        }

    @Test
    fun `reset to defaults restores the default blackout brightness`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setBlackoutBrightness(0.8f)
            advanceUntilIdle()
            viewModel.resetToDefaults()
            advanceUntilIdle()

            assertEquals(
                LauncherSettings.DEFAULT_BLACKOUT_BRIGHTNESS,
                viewModel.uiState.value.blackoutBrightness,
            )

            collection.cancel()
        }

    @Test
    fun `blackout brightness is clamped to the supported range`() =
        runTest(dispatcher) {
            val store = InMemorySettingsStore()
            val viewModel = SettingsViewModel(store)

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.setBlackoutBrightness(2f)
            advanceUntilIdle()
            assertEquals(
                LauncherSettings.MAX_BLACKOUT_BRIGHTNESS,
                viewModel.uiState.value.blackoutBrightness,
            )

            viewModel.setBlackoutBrightness(0f)
            advanceUntilIdle()
            assertEquals(
                LauncherSettings.MIN_BLACKOUT_BRIGHTNESS,
                viewModel.uiState.value.blackoutBrightness,
            )

            collection.cancel()
        }
}
