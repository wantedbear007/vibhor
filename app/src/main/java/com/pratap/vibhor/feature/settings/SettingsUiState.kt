package com.pratap.vibhor.feature.settings

import com.pratap.vibhor.core.platform.AccentColor
import com.pratap.vibhor.core.platform.AnimationStyle
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.HomeButtonTarget
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings

data class SettingsUiState(
    val clockScale: Float = LauncherSettings.DEFAULT_CLOCK_SCALE,
    val appIconScale: Float = LauncherSettings.DEFAULT_SCALE,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val clockFormat: ClockFormat = ClockFormat.DEFAULT,
    val clockBlurRadius: Int = LauncherSettings.DEFAULT_BLUR_RADIUS,
    val backgroundDarkness: Float = LauncherSettings.DEFAULT_DARKNESS,
    val showStatusBar: Boolean = LauncherSettings.DEFAULT_SHOW_STATUS_BAR,
    val quickButtonScale: Float = LauncherSettings.DEFAULT_QUICK_BUTTON_SCALE,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val background: LauncherBackground = LauncherBackground.DEFAULT,
    val backgroundOpacity: Float = LauncherSettings.DEFAULT_BACKGROUND_OPACITY,
    val animationStyle: AnimationStyle = AnimationStyle.DEFAULT,
    val homeButtonTarget: HomeButtonTarget = HomeButtonTarget.DEFAULT,
    val blackoutBrightness: Float = LauncherSettings.DEFAULT_BLACKOUT_BRIGHTNESS,
    val nowPlayingTextSizeSp: Float = LauncherSettings.DEFAULT_NOW_PLAYING_TEXT_SIZE_SP,
    val showNowPlayingTicker: Boolean = LauncherSettings.DEFAULT_SHOW_NOW_PLAYING_TICKER,
)
