package com.pratap.vibhor.feature.cockpit

import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings

data class CockpitUiState(
    val timeText: String = "",
    val dateText: String = "",
    val volumeLevel: Float = 0.5f,
    val brightnessLevel: Float = 0.5f,
    val background: LauncherBackground = LauncherBackground.DEFAULT,
    val backgroundOpacity: Float = LauncherSettings.DEFAULT_BACKGROUND_OPACITY,
)
