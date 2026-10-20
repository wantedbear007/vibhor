package com.pratap.vibhor.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.AndroidInstalledAppsSource
import com.pratap.vibhor.core.platform.InstalledAppsSource
import com.pratap.vibhor.core.platform.SettingsStore
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class AppDrawerViewModel(
    installedAppsSource: InstalledAppsSource,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(AppDrawerTab.All)

    val uiState: StateFlow<AppDrawerUiState> = combine(
        installedAppsSource.apps(),
        settingsStore.settings,
        selectedTab,
    ) { apps, settings, tab ->
        val favourites = settings.favouritePackageNames
        val favouriteApps = apps.filter { it.packageName in favourites }
        AppDrawerUiState(
            apps = apps,
            favouriteApps = favouriteApps,
            favouritePackageNames = favourites,
            selectedTab = tab,
            isLoading = false,
            iconScale = settings.appIconScale,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = AppDrawerUiState(),
    )

    fun selectTab(tab: AppDrawerTab) {
        selectedTab.update { tab }
    }

    fun toggleFavourite(packageName: String) = settingsStore.toggleFavourite(packageName)

    fun addFavourite(packageName: String) = settingsStore.addFavourite(packageName)

    fun removeFavourite(packageName: String) = settingsStore.removeFavourite(packageName)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                AppDrawerViewModel(
                    installedAppsSource = AndroidInstalledAppsSource(application),
                    settingsStore = SharedPreferencesSettingsStore.get(application),
                )
            }
        }
    }
}
