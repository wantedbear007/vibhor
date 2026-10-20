package com.pratap.vibhor.feature.cockpit

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.AndroidNavigationAppsSource
import com.pratap.vibhor.core.platform.NavigationApp
import com.pratap.vibhor.core.platform.NavigationAppsSource
import com.pratap.vibhor.core.platform.SystemClockTicker
import com.pratap.vibhor.core.time.ClockTicker
import com.pratap.vibhor.core.time.formatClockDate
import com.pratap.vibhor.core.time.formatClockTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CockpitViewModel(
    clockTicker: ClockTicker,
    private val navigationAppsSource: NavigationAppsSource,
    is24HourFormat: () -> Boolean = { false },
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val navigationApps = MutableStateFlow<List<NavigationApp>>(emptyList())

    val uiState: StateFlow<CockpitUiState> = combine(
        clockTicker.ticks(),
        navigationApps,
    ) { now, apps ->
        CockpitUiState(
            timeText = formatClockTime(now, is24Hour = is24HourFormat()),
            dateText = formatClockDate(now),
            navigationApp = apps.firstOrNull(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = CockpitUiState(),
    )

    init {
        viewModelScope.launch(ioDispatcher) {
            navigationApps.value = runCatching {
                navigationAppsSource.resolveNavigationApps()
            }.getOrDefault(emptyList())
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                CockpitViewModel(
                    clockTicker = SystemClockTicker(application),
                    navigationAppsSource = AndroidNavigationAppsSource(application),
                    is24HourFormat = { DateFormat.is24HourFormat(application) },
                )
            }
        }
    }
}
