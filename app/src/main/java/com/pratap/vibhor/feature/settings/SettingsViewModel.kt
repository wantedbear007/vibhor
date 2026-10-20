package com.pratap.vibhor.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.LauncherSettings
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
                clockFormat = settings.clockFormat,
                clockBlurRadius = settings.clockBlurRadius,
                backgroundDarkness = settings.backgroundDarkness,
                showStatusBar = settings.showStatusBar,
                quickButtonScale = settings.quickButtonScale,
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

    fun setClockFormat(format: ClockFormat) = settingsStore.setClockFormat(format)

    fun setClockBlurRadius(radius: Int) = settingsStore.setClockBlurRadius(radius)

    fun setBackgroundDarkness(darkness: Float) =
        settingsStore.setBackgroundDarkness(darkness)

    fun setShowStatusBar(show: Boolean) = settingsStore.setShowStatusBar(show)

    fun setQuickButtonScale(scale: Float) = settingsStore.setQuickButtonScale(scale)

    fun resetToDefaults() {
        settingsStore.setClockScale(LauncherDefaults.Scale)
        settingsStore.setAppIconScale(LauncherDefaults.Scale)
        settingsStore.setClockFace(ClockFace.DEFAULT)
        settingsStore.setClockFormat(ClockFormat.DEFAULT)
        settingsStore.setClockBlurRadius(LauncherSettings.DEFAULT_BLUR_RADIUS)
        settingsStore.setBackgroundDarkness(LauncherSettings.DEFAULT_DARKNESS)
        settingsStore.setShowStatusBar(LauncherSettings.DEFAULT_SHOW_STATUS_BAR)
        settingsStore.setQuickButtonScale(LauncherSettings.DEFAULT_QUICK_BUTTON_SCALE)
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
