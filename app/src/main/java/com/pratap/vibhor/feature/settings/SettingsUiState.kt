package com.pratap.vibhor.feature.settings

import com.pratap.vibhor.core.platform.AccentColor
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings

data class SettingsUiState(
    val clockScale: Float = LauncherDefaults.Scale,
    val appIconScale: Float = LauncherDefaults.Scale,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val clockFormat: ClockFormat = ClockFormat.DEFAULT,
    val clockBlurRadius: Int = LauncherSettings.DEFAULT_BLUR_RADIUS,
    val backgroundDarkness: Float = LauncherSettings.DEFAULT_DARKNESS,
    val showStatusBar: Boolean = LauncherSettings.DEFAULT_SHOW_STATUS_BAR,
    val quickButtonScale: Float = LauncherSettings.DEFAULT_QUICK_BUTTON_SCALE,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val background: LauncherBackground = LauncherBackground.DEFAULT,
    val backgroundOpacity: Float = LauncherSettings.DEFAULT_BACKGROUND_OPACITY,
)

object LauncherDefaults {
    const val Scale = 1f
}
