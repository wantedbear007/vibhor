package com.pratap.vibhor.feature.settings

import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.ClockFormat

data class SettingsUiState(
    val clockScale: Float = LauncherDefaults.Scale,
    val appIconScale: Float = LauncherDefaults.Scale,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val clockFormat: ClockFormat = ClockFormat.DEFAULT,
)

object LauncherDefaults {
    const val Scale = 1f
}
