package com.pratap.vibhor.feature.cockpit

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.AndroidDeviceControls
import com.pratap.vibhor.core.platform.AndroidQuickControls
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.DeviceControls
import com.pratap.vibhor.core.platform.QuickControls
import com.pratap.vibhor.core.platform.SettingsStore
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.platform.SystemClockTicker
import com.pratap.vibhor.core.time.ClockTicker
import com.pratap.vibhor.core.time.formatClockDate
import com.pratap.vibhor.core.time.formatClockTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CockpitViewModel(
    clockTicker: ClockTicker,
    private val settingsStore: SettingsStore,
    private val deviceControls: DeviceControls,
    private val quickControls: QuickControls,
    is24HourFormat: () -> Boolean = { false },
) : ViewModel() {

    /**
     * Broadcasts from Wi-Fi/Bluetooth are the source of truth. This counter
     * additionally requests a fresh read right after a tap so refused
     * platform requests look like no-ops instead of optimistic toggles.
     */
    private val connectivityRefresh = MutableStateFlow(0)
    private val connectivityChanges = merge(
        quickControls.observeChanges(),
        connectivityRefresh.map { },
    ).onStart { emit(Unit) }

    /**
     * Mute changes are not consistently broadcast on every Android version.
     * This counter requests an immediate real-state read after a tap while the
     * device flow continues to handle external changes and fallback polling.
     */
    private val volumeRefresh = MutableStateFlow(0)

    /**
     * Window-level brightness changes made by the Blackout page do not touch
     * the system setting, so they are not covered by the content observers.
     * This counter refreshes the shared brightness slider when Blackout opens
     * or closes.
     */
    private val brightnessRefresh = MutableStateFlow(0)
    private var blackoutBrightnessActive = false

    private val deviceChanges = merge(
        deviceControls.observeChanges(),
        volumeRefresh.map { },
        brightnessRefresh.map { },
    ).onStart { emit(Unit) }

    val uiState: StateFlow<CockpitUiState> = combine(
        clockTicker.ticks(),
        settingsStore.settings,
        deviceChanges,
        connectivityChanges,
    ) { now, settings, _, _ ->
        val use24Hour = when (settings.clockFormat) {
            ClockFormat.H24 -> true
            ClockFormat.H12 -> false
            ClockFormat.SYSTEM -> is24HourFormat()
        }
        val connectivity = quickControls.state()
        CockpitUiState(
            timeText = formatClockTime(now, is24Hour = use24Hour),
            dateText = formatClockDate(now),
            volumeLevel = deviceControls.mediaVolume(),
            volumeMuted = deviceControls.isMediaMuted(),
            brightnessLevel = deviceControls.screenBrightness(),
            brightnessPermissionGranted = deviceControls.canWriteSystemBrightness(),
            wifiSupported = connectivity.wifiSupported,
            wifiEnabled = connectivity.wifiEnabled,
            bluetoothSupported = connectivity.bluetoothSupported,
            bluetoothEnabled = connectivity.bluetoothEnabled,
            nightMode = settings.nightMode,
            background = settings.background,
            backgroundOpacity = settings.backgroundOpacity,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = CockpitUiState(),
    )

    fun setVolume(level: Float) {
        deviceControls.setMediaVolume(level)
    }

    fun toggleMute() {
        deviceControls.setMediaMuted(!deviceControls.isMediaMuted())
        volumeRefresh.update { it + 1 }
    }

    fun setBrightness(level: Float) {
        deviceControls.setScreenBrightness(level)
    }

    /**
     * Applies the persisted Blackout brightness as a window-only override.
     * The first call captures the current window brightness so [exitBlackout]
     * can restore it exactly, including "follow system brightness".
     */
    fun enterBlackout(level: Float) {
        blackoutBrightnessActive = true
        deviceControls.setTemporaryScreenBrightness(level)
        brightnessRefresh.update { it + 1 }
    }

    /** Restores the brightness that was active before Blackout opened. */
    fun exitBlackout() {
        if (!blackoutBrightnessActive) return
        blackoutBrightnessActive = false
        deviceControls.clearTemporaryScreenBrightness()
        brightnessRefresh.update { it + 1 }
    }

    override fun onCleared() {
        if (blackoutBrightnessActive) {
            blackoutBrightnessActive = false
            deviceControls.clearTemporaryScreenBrightness()
        }
    }

    fun toggleWifi() {
        val current = quickControls.state()
        if (!current.wifiSupported) return
        quickControls.setWifiEnabled(!current.wifiEnabled)
        refreshConnectivity()
    }

    fun toggleBluetooth() {
        val current = quickControls.state()
        if (!current.bluetoothSupported) return
        quickControls.setBluetoothEnabled(!current.bluetoothEnabled)
        refreshConnectivity()
    }

    fun toggleNightMode() {
        settingsStore.setNightMode(!settingsStore.settings.value.nightMode)
    }

    private fun refreshConnectivity() {
        connectivityRefresh.update { it + 1 }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                CockpitViewModel(
                    clockTicker = SystemClockTicker(application),
                    settingsStore = SharedPreferencesSettingsStore.get(application),
                    deviceControls = AndroidDeviceControls(application),
                    quickControls = AndroidQuickControls(application),
                    is24HourFormat = { DateFormat.is24HourFormat(application) },
                )
            }
        }
    }
}
