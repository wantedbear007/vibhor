package com.pratap.vibhor.feature.settings

import com.pratap.vibhor.core.platform.ClockFace

data class SettingsUiState(
    val clockScale: Float = LauncherDefaults.Scale,
    val appIconScale: Float = LauncherDefaults.Scale,
    val clockFace: ClockFace = ClockFace.DEFAULT,
)

object LauncherDefaults {
    const val Scale = 1f
}
