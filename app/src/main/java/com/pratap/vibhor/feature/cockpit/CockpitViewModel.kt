package com.pratap.vibhor.feature.cockpit

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.SettingsStore
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.platform.SystemClockTicker
import com.pratap.vibhor.core.time.ClockTicker
import com.pratap.vibhor.core.time.formatClockDate
import com.pratap.vibhor.core.time.formatClockTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CockpitViewModel(
    clockTicker: ClockTicker,
    settingsStore: SettingsStore,
    is24HourFormat: () -> Boolean = { false },
    private val audioManager: AudioManager,
    private val application: Application,
) : ViewModel() {

    val uiState: StateFlow<CockpitUiState> = combine(
        clockTicker.ticks(),
        settingsStore.settings,
    ) { now, settings ->
        val use24Hour = when (settings.clockFormat) {
            ClockFormat.H24 -> true
            ClockFormat.H12 -> false
            ClockFormat.SYSTEM -> is24HourFormat()
        }
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volumeLevel = if (maxVolume == 0) 0.5f else (currentVolume.toFloat() / maxVolume.toFloat())
        var brightness = 0.5f
        val windowBrightness = application.currentActivityBrightness()
        if (windowBrightness >= 0f) {
            brightness = windowBrightness.coerceIn(0f, 1f)
        } else {
            val systemBrightness = runCatching {
                Settings.System.getInt(application.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            }.getOrDefault(128)
            brightness = (systemBrightness.toFloat() / 255f).coerceIn(0f, 1f)
        }
        CockpitUiState(
            timeText = formatClockTime(now, is24Hour = use24Hour),
            dateText = formatClockDate(now),
            volumeLevel = volumeLevel,
            brightnessLevel = brightness,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = CockpitUiState(),
    )

    fun setVolume(level: Float) {
        val clamped = level.coerceIn(0f, 1f)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val index = (clamped * maxVolume.toFloat()).toInt().coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
    }

    fun setBrightness(level: Float) {
        val clamped = level.coerceIn(0f, 1f)
        // Try to update current activity window if present
        val activity = application.currentActivity()
        val window = activity?.window
        if (window != null) {
            val lp = window.attributes
            lp.screenBrightness = clamped
            window.attributes = lp
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                CockpitViewModel(
                    clockTicker = SystemClockTicker(application),
                    settingsStore = SharedPreferencesSettingsStore.get(application),
                    is24HourFormat = { DateFormat.is24HourFormat(application) },
                    audioManager = audioManager,
                    application = application,
                )
            }
        }
    }
}

private fun Application.currentActivity(): android.app.Activity? {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
        val activitiesField = activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        val activities = activitiesField.get(activityThread) as? Map<*, *> ?: return null
        for (activityRecord in activities.values) {
            val activityRecordClass = activityRecord?.javaClass ?: continue
            val pausedField = activityRecordClass.getDeclaredField("paused")
            pausedField.isAccessible = true
            if (!pausedField.getBoolean(activityRecord)) {
                val activityField = activityRecordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                return activityField.get(activityRecord) as? android.app.Activity
            }
        }
        null
    } catch (_: Throwable) {
        null
    }
}

private fun Application.currentActivityBrightness(): Float {
    val activity = currentActivity() ?: return -1f
    val lp = activity.window?.attributes ?: return -1f
    val b = lp.screenBrightness
    return if (b < 0f) -1f else b.coerceIn(0f, 1f)
}
