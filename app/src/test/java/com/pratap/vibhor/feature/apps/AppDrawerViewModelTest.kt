package com.pratap.vibhor.feature.apps

import com.pratap.vibhor.core.platform.InMemorySettingsStore
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.platform.InstalledAppsSource
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings
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
import org.junit.Assert.assertTrue
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

    @Test
    fun `search filters displayed apps by label ignoring case`() = runTest(dispatcher) {
        val apps = sampleApps()
        val viewModel = AppDrawerViewModel(
            installedAppsSource = FakeInstalledAppsSource(flowOf(apps)),
            settingsStore = InMemorySettingsStore(),
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setSearchQuery("MaP")
        advanceUntilIdle()

        assertEquals("MaP", viewModel.uiState.value.searchQuery)
        assertEquals(listOf(apps[1]), viewModel.uiState.value.displayedApps)

        collection.cancel()
    }

    @Test
    fun `favourites tab search only matches favourites`() = runTest(dispatcher) {
        val apps = sampleApps()
        val viewModel = AppDrawerViewModel(
            installedAppsSource = FakeInstalledAppsSource(flowOf(apps)),
            settingsStore = InMemorySettingsStore(
                LauncherSettings(
                    favouritePackageNames = setOf(apps[0].packageName),
                ),
            ),
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.selectTab(AppDrawerTab.Favourites)
        viewModel.setSearchQuery("map")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.displayedApps.isEmpty())

        viewModel.setSearchQuery("dial")
        advanceUntilIdle()
        assertEquals(listOf(apps[0]), viewModel.uiState.value.displayedApps)

        collection.cancel()
    }

    @Test
    fun `launcher background is exposed to the drawer`() = runTest(dispatcher) {
        val viewModel = AppDrawerViewModel(
            installedAppsSource = FakeInstalledAppsSource(flowOf(sampleApps())),
            settingsStore = InMemorySettingsStore(
                LauncherSettings(
                    background = LauncherBackground.NAVY,
                    backgroundOpacity = 0.35f,
                ),
            ),
        )

        val collection = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(LauncherBackground.NAVY, viewModel.uiState.value.background)
        assertEquals(0.35f, viewModel.uiState.value.backgroundOpacity)

        collection.cancel()
    }

    private fun sampleApps() = listOf(
        InstalledApp(
            packageName = "com.example.dialer",
            activityName = "com.example.dialer.MainActivity",
            label = "Dialer",
        ),
        InstalledApp(
            packageName = "com.example.maps",
            activityName = "com.example.maps.MainActivity",
            label = "Maps",
        ),
    )

    private class FakeInstalledAppsSource(
        private val apps: Flow<List<InstalledApp>>,
    ) : InstalledAppsSource {
        override fun apps(): Flow<List<InstalledApp>> = apps
    }
}
