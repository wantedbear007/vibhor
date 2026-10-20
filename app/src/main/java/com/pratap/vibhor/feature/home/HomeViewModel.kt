package com.pratap.vibhor.feature.home

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.AndroidInstalledAppsSource
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.InstalledAppsSource
import com.pratap.vibhor.core.platform.SettingsStore
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.platform.SystemClockTicker
import com.pratap.vibhor.core.time.ClockTicker
import com.pratap.vibhor.core.time.formatClockDate
import com.pratap.vibhor.core.time.formatClockTime
import com.pratap.vibhor.core.time.splitClockTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    clockTicker: ClockTicker,
    private val settingsStore: SettingsStore,
    installedAppsSource: InstalledAppsSource,
    is24HourFormat: () -> Boolean = { false },
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        clockTicker.ticks(),
        settingsStore.settings,
        installedAppsSource.apps(),
    ) { now, settings, apps ->
        val use24Hour = when (settings.clockFormat) {
            ClockFormat.H24 -> true
            ClockFormat.H12 -> false
            ClockFormat.SYSTEM -> is24HourFormat()
        }
        val split = splitClockTime(now, is24Hour = use24Hour)
        HomeUiState(
            timeText = formatClockTime(now, is24Hour = use24Hour),
            dateText = formatClockDate(now),
            clockScale = settings.clockScale,
            clockFace = settings.clockFace,
            clockBlurRadius = settings.clockBlurRadius,
            backgroundDarkness = settings.backgroundDarkness,
            hour = now.hour,
            minute = now.minute,
            second = now.second,
            hourText = split.hourText,
            minuteText = split.minuteText,
            meridiemText = split.meridiemText,
            quickButtonScale = settings.quickButtonScale,
            favourites = apps.filter { it.packageName in settings.favouritePackageNames },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState(),
    )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                HomeViewModel(
                    clockTicker = SystemClockTicker(application),
                    settingsStore = SharedPreferencesSettingsStore.get(application),
                    installedAppsSource = AndroidInstalledAppsSource(application),
                    is24HourFormat = { DateFormat.is24HourFormat(application) },
                )
            }
        }
    }
}
