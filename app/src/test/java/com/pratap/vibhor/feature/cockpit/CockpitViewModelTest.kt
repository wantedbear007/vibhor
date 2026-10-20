package com.pratap.vibhor.feature.cockpit

import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.DeviceControls
import com.pratap.vibhor.core.platform.InMemorySettingsStore
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings
import com.pratap.vibhor.core.platform.QuickControls
import com.pratap.vibhor.core.platform.QuickControlsState
import com.pratap.vibhor.core.time.ClockTicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CockpitViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var previousLocale: Locale

    @Before
    fun setUp() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Locale.setDefault(previousLocale)
        Dispatchers.resetMain()
    }

    @Test
    fun `ui state exposes formatted clock and device levels`() = runTest(dispatcher) {
        val instant = ZonedDateTime.of(2026, 10, 19, 18, 42, 0, 0, ZoneId.of("UTC"))
        val controls = FakeDeviceControls(volume = 0.4f, brightness = 0.6f)
        val viewModel = CockpitViewModel(
            clockTicker = FakeClockTicker(instant),
            settingsStore = InMemorySettingsStore(),
            deviceControls = controls,
            quickControls = FakeQuickControls(),
            is24HourFormat = { false },
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals("6:42 PM", viewModel.uiState.value.timeText)
        assertEquals(0.4f, viewModel.uiState.value.volumeLevel)
        assertEquals(0.6f, viewModel.uiState.value.brightnessLevel)

        collection.cancel()
    }

    @Test
    fun `launcher background is exposed to the cockpit`() = runTest(dispatcher) {
        val viewModel = CockpitViewModel(
            clockTicker = FakeClockTicker(ZonedDateTime.now()),
            settingsStore = InMemorySettingsStore(
                LauncherSettings(
                    background = LauncherBackground.OCEAN,
                    backgroundOpacity = 0.2f,
                ),
            ),
            deviceControls = FakeDeviceControls(),
            quickControls = FakeQuickControls(),
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(LauncherBackground.OCEAN, viewModel.uiState.value.background)
        assertEquals(0.2f, viewModel.uiState.value.backgroundOpacity)

        collection.cancel()
    }

    @Test
    fun `rail clock follows the configured 24-hour format`() = runTest(dispatcher) {
        val instant = ZonedDateTime.of(2026, 10, 19, 18, 42, 0, 0, ZoneId.of("UTC"))
        val viewModel = CockpitViewModel(
            clockTicker = FakeClockTicker(instant),
            settingsStore = InMemorySettingsStore(
                LauncherSettings(clockFormat = ClockFormat.H24),
            ),
            deviceControls = FakeDeviceControls(),
            quickControls = FakeQuickControls(),
            is24HourFormat = { false },
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals("18:42", viewModel.uiState.value.timeText)

        collection.cancel()
    }

    @Test
    fun `setVolume and setBrightness drive the device controls`() = runTest(dispatcher) {
        val controls = FakeDeviceControls()
        val viewModel = CockpitViewModel(
            clockTicker = FakeClockTicker(ZonedDateTime.now()),
            settingsStore = InMemorySettingsStore(),
            deviceControls = controls,
            quickControls = FakeQuickControls(),
        )

        viewModel.setVolume(0.75f)
        viewModel.setBrightness(0.3f)

        assertEquals(0.75f, controls.volume)
        assertEquals(0.3f, controls.brightness)

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(0.75f, viewModel.uiState.value.volumeLevel)
        assertEquals(0.3f, viewModel.uiState.value.brightnessLevel)

        collection.cancel()
    }

    @Test
    fun `ui state exposes real connectivity and persisted night mode`() =
        runTest(dispatcher) {
            val quickControls = FakeQuickControls(
                QuickControlsState(
                    wifiSupported = true,
                    wifiEnabled = true,
                    bluetoothSupported = true,
                    bluetoothEnabled = false,
                ),
            )
            val viewModel = CockpitViewModel(
                clockTicker = FakeClockTicker(ZonedDateTime.now()),
                settingsStore = InMemorySettingsStore(
                    LauncherSettings(nightMode = true),
                ),
                deviceControls = FakeDeviceControls(),
                quickControls = quickControls,
            )

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals(true, viewModel.uiState.value.wifiSupported)
            assertEquals(true, viewModel.uiState.value.wifiEnabled)
            assertEquals(true, viewModel.uiState.value.bluetoothSupported)
            assertEquals(false, viewModel.uiState.value.bluetoothEnabled)
            assertEquals(true, viewModel.uiState.value.nightMode)

            collection.cancel()
        }

    @Test
    fun `quick toggles request real state changes and publish actual state`() =
        runTest(dispatcher) {
            val quickControls = FakeQuickControls(
                QuickControlsState(
                    wifiSupported = true,
                    wifiEnabled = false,
                    bluetoothSupported = true,
                    bluetoothEnabled = true,
                ),
            )
            val viewModel = CockpitViewModel(
                clockTicker = FakeClockTicker(ZonedDateTime.now()),
                settingsStore = InMemorySettingsStore(),
                deviceControls = FakeDeviceControls(),
                quickControls = quickControls,
            )
            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.toggleWifi()
            viewModel.toggleBluetooth()
            advanceUntilIdle()

            assertEquals(true, quickControls.current.wifiEnabled)
            assertEquals(false, quickControls.current.bluetoothEnabled)
            assertEquals(true, viewModel.uiState.value.wifiEnabled)
            assertEquals(false, viewModel.uiState.value.bluetoothEnabled)

            collection.cancel()
        }

    @Test
    fun `night mode toggle is persisted`() = runTest(dispatcher) {
        val store = InMemorySettingsStore()
        val viewModel = CockpitViewModel(
            clockTicker = FakeClockTicker(ZonedDateTime.now()),
            settingsStore = store,
            deviceControls = FakeDeviceControls(),
            quickControls = FakeQuickControls(),
        )
        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.toggleNightMode()
        advanceUntilIdle()

        assertEquals(true, store.settings.value.nightMode)
        assertEquals(true, viewModel.uiState.value.nightMode)

        collection.cancel()
    }

    private class FakeClockTicker(
        private val instant: ZonedDateTime,
    ) : ClockTicker {
        override fun ticks(): Flow<ZonedDateTime> = flowOf(instant)
    }

    private class FakeDeviceControls(
        var volume: Float = 0.5f,
        var brightness: Float = 0.5f,
    ) : DeviceControls {
        override fun mediaVolume(): Float = volume

        override fun setMediaVolume(fraction: Float) {
            volume = fraction
        }

        override fun screenBrightness(): Float = brightness

        override fun setScreenBrightness(fraction: Float) {
            brightness = fraction
        }

        override fun observeChanges(): Flow<Unit> = flowOf(Unit)
    }

    private class FakeQuickControls(
        initial: QuickControlsState = QuickControlsState(),
    ) : QuickControls {
        var current: QuickControlsState = initial

        override fun state(): QuickControlsState = current

        override fun setWifiEnabled(enabled: Boolean) {
            current = current.copy(wifiEnabled = enabled)
        }

        override fun setBluetoothEnabled(enabled: Boolean) {
            current = current.copy(bluetoothEnabled = enabled)
        }

        override fun observeChanges(): Flow<Unit> = flowOf(Unit)
    }
}
