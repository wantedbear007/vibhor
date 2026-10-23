package com.pratap.vibhor.feature.home

import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.platform.LauncherSettings

data class HomeUiState(
    val timeText: String = "",
    val dateText: String = "",
    val clockScale: Float = 1f,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val clockBlurRadius: Int = 0,
    val backgroundDarkness: Float = 0f,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val hourText: String = "",
    val minuteText: String = "",
    val meridiemText: String = "",
    val quickButtonScale: Float = 1f,
    val nowPlayingTextSizeSp: Float =
        LauncherSettings.DEFAULT_NOW_PLAYING_TEXT_SIZE_SP,
    val showNowPlayingTicker: Boolean = LauncherSettings.DEFAULT_SHOW_NOW_PLAYING_TICKER,
    val favourites: List<InstalledApp> = emptyList(),
)
