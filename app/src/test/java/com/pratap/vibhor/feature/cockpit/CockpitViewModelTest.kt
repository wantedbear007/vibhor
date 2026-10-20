package com.pratap.vibhor.feature.cockpit

import com.pratap.vibhor.core.platform.NavigationApp
import com.pratap.vibhor.core.platform.NavigationAppsSource
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
import org.junit.Assert.assertNotNull
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
    fun `ui state exposes first navigation app and formatted clock`() =
        runTest(dispatcher) {
            val instant = ZonedDateTime.of(2026, 10, 19, 18, 42, 0, 0, ZoneId.of("UTC"))
            val app = NavigationApp(
                label = "Maps",
                packageName = "com.example.maps",
                activityName = "com.example.maps.MainActivity",
            )
            val viewModel = CockpitViewModel(
                clockTicker = FakeClockTicker(instant),
                navigationAppsSource = FakeNavigationAppsSource(listOf(app)),
                is24HourFormat = { false },
                ioDispatcher = dispatcher,
            )

            val collection = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals("6:42 PM", viewModel.uiState.value.timeText)
            assertNotNull(viewModel.uiState.value.navigationApp)
            assertEquals(app, viewModel.uiState.value.navigationApp)

            collection.cancel()
        }

    private class FakeClockTicker(
        private val instant: ZonedDateTime,
    ) : ClockTicker {
        override fun ticks(): Flow<ZonedDateTime> = flowOf(instant)
    }

    private class FakeNavigationAppsSource(
        private val apps: List<NavigationApp>,
    ) : NavigationAppsSource {
        override fun resolveNavigationApps(): List<NavigationApp> = apps
    }
}
