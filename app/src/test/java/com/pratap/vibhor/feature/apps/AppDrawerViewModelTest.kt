package com.pratap.vibhor.feature.apps

import com.pratap.vibhor.core.platform.InMemorySettingsStore
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.platform.InstalledAppsSource
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

@OptIn(ExperimentalCoroutinesApi::class)
class AppDrawerViewModelTest {

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
    fun `ui state exposes discovered apps`() = runTest(dispatcher) {
        val apps = listOf(
            InstalledApp(
                packageName = "com.example.dialer",
                activityName = "com.example.dialer.MainActivity",
                label = "Dialer",
            ),
        )
        val viewModel = AppDrawerViewModel(
            installedAppsSource = FakeInstalledAppsSource(flowOf(apps)),
            settingsStore = InMemorySettingsStore(),
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(apps, viewModel.uiState.value.apps)

        collection.cancel()
    }

    private class FakeInstalledAppsSource(
        private val apps: Flow<List<InstalledApp>>,
    ) : InstalledAppsSource {
        override fun apps(): Flow<List<InstalledApp>> = apps
    }
}
