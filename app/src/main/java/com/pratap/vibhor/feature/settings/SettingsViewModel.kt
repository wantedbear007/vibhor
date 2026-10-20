package com.pratap.vibhor.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.SettingsStore
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Exposes current launcher settings and forwards user changes to the
 * SettingsStore. New launcher settings are added here and on the
 * SettingsScreen.
 */
class SettingsViewModel(
    private val settingsStore: SettingsStore,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsStore.settings
        .map { settings ->
            SettingsUiState(
                clockScale = settings.clockScale,
                appIconScale = settings.appIconScale,
                clockFace = settings.clockFace,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SettingsUiState(),
        )

    fun setClockScale(scale: Float) = settingsStore.setClockScale(scale)

    fun setAppIconScale(scale: Float) = settingsStore.setAppIconScale(scale)

    fun setClockFace(face: ClockFace) = settingsStore.setClockFace(face)

    fun resetToDefaults() {
        settingsStore.setClockScale(LauncherDefaults.Scale)
        settingsStore.setAppIconScale(LauncherDefaults.Scale)
        settingsStore.setClockFace(ClockFace.DEFAULT)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                SettingsViewModel(SharedPreferencesSettingsStore.get(application))
            }
        }
    }
}
