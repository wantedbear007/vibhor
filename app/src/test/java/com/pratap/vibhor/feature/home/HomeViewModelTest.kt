package com.pratap.vibhor.feature.home

import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.InMemorySettingsStore
import com.pratap.vibhor.core.platform.LauncherSettings
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
class HomeViewModelTest {

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
    fun `ui state exposes formatted time and date`() = runTest(dispatcher) {
        val instant = ZonedDateTime.of(2026, 10, 19, 18, 42, 0, 0, ZoneId.of("UTC"))
        val viewModel = HomeViewModel(
            clockTicker = FakeClockTicker(instant),
            settingsStore = InMemorySettingsStore(),
            is24HourFormat = { false },
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals("6:42 PM", viewModel.uiState.value.timeText)
        assertEquals("Monday, 19 October", viewModel.uiState.value.dateText)

        collection.cancel()
    }

    @Test
    fun `ui state reflects clock scale from settings`() = runTest(dispatcher) {
        val instant = ZonedDateTime.of(2026, 10, 19, 18, 42, 0, 0, ZoneId.of("UTC"))
        val settingsStore = InMemorySettingsStore(LauncherSettings(clockScale = 1.5f))
        val viewModel = HomeViewModel(
            clockTicker = FakeClockTicker(instant),
            settingsStore = settingsStore,
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(1.5f, viewModel.uiState.value.clockScale)

        collection.cancel()
    }

    @Test
    fun `ui state starts empty before the first tick`() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            clockTicker = FakeClockTicker(ZonedDateTime.now()),
            settingsStore = InMemorySettingsStore(),
        )

        assertEquals(HomeUiState(), viewModel.uiState.value)
    }

    @Test
    fun `ui state reflects clock face and split time pieces from settings`() =
        runTest(dispatcher) {
            val instant = ZonedDateTime.of(2026, 10, 19, 18, 42, 0, 0, ZoneId.of("UTC"))
            val settingsStore = InMemorySettingsStore(
                LauncherSettings(clockFace = ClockFace.SPLIT),
            )
            val viewModel = HomeViewModel(
                clockTicker = FakeClockTicker(instant),
                settingsStore = settingsStore,
                is24HourFormat = { false },
            )

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals(ClockFace.SPLIT, viewModel.uiState.value.clockFace)
            assertEquals(18, viewModel.uiState.value.hour)
            assertEquals(42, viewModel.uiState.value.minute)
            assertEquals("6", viewModel.uiState.value.hourText)
            assertEquals("42", viewModel.uiState.value.minuteText)
            assertEquals("PM", viewModel.uiState.value.meridiemText)

            collection.cancel()
        }

    private class FakeClockTicker(
        private val instant: ZonedDateTime,
    ) : ClockTicker {
        override fun ticks(): Flow<ZonedDateTime> = flowOf(instant)
    }
}
