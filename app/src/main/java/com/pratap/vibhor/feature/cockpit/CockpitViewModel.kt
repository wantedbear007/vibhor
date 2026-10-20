package com.pratap.vibhor.feature.cockpit

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.AndroidDeviceControls
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.DeviceControls
import com.pratap.vibhor.core.platform.SettingsStore
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.platform.SystemClockTicker
import com.pratap.vibhor.core.time.ClockTicker
import com.pratap.vibhor.core.time.formatClockDate
import com.pratap.vibhor.core.time.formatClockTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class CockpitViewModel(
    clockTicker: ClockTicker,
    settingsStore: SettingsStore,
    private val deviceControls: DeviceControls,
    is24HourFormat: () -> Boolean = { false },
) : ViewModel() {

    val uiState: StateFlow<CockpitUiState> = combine(
        clockTicker.ticks(),
        settingsStore.settings,
        deviceControls.observeChanges().onStart { emit(Unit) },
    ) { now, settings, _ ->
        val use24Hour = when (settings.clockFormat) {
            ClockFormat.H24 -> true
            ClockFormat.H12 -> false
            ClockFormat.SYSTEM -> is24HourFormat()
        }
        CockpitUiState(
            timeText = formatClockTime(now, is24Hour = use24Hour),
            dateText = formatClockDate(now),
            volumeLevel = deviceControls.mediaVolume(),
            brightnessLevel = deviceControls.screenBrightness(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = CockpitUiState(),
    )

    fun setVolume(level: Float) {
        deviceControls.setMediaVolume(level)
    }

    fun setBrightness(level: Float) {
        deviceControls.setScreenBrightness(level)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                CockpitViewModel(
                    clockTicker = SystemClockTicker(application),
                    settingsStore = SharedPreferencesSettingsStore.get(application),
                    deviceControls = AndroidDeviceControls(application),
                    is24HourFormat = { DateFormat.is24HourFormat(application) },
                )
            }
        }
    }
}
