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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppDrawerViewModel(
    installedAppsSource: InstalledAppsSource,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    val uiState: StateFlow<AppDrawerUiState> = combine(
        installedAppsSource.apps(),
        settingsStore.settings,
    ) { apps, settings ->
        AppDrawerUiState(
            apps = apps,
            isLoading = false,
            iconScale = settings.appIconScale,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = AppDrawerUiState(),
    )

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
