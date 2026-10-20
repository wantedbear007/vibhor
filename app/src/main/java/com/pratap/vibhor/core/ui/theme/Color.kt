package com.pratap.vibhor.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.pratap.vibhor.core.platform.AccentColor
import com.pratap.vibhor.core.platform.LauncherBackground

val Midnight = Color(0xFF0B111A)
val DeepSlate = Color(0xFF142537)
val PrimaryText = Color(0xFFF3F6FA)
val SecondaryText = Color(0xFF91A6BC)

/** Background palette. Midnight is the default launcher surface. */
val BackgroundSlate = Color(0xFF151C26)
val BackgroundGraphite = Color(0xFF23272E)
val BackgroundBlack = Color(0xFF000000)
val BackgroundNavy = Color(0xFF0A1B30)
val BackgroundOcean = Color(0xFF082029)

/**
 * Base accent. Kept for backward compatibility; UI code should read the
 * active accent from [VibhorThemeColors.accent] instead of this constant.
 */
val ElectricBlue = Color(0xFF59C9FF)

/** Accent picker palette, ordered like the settings swatches. */
val AccentTeal = Color(0xFF4FE3C1)
val AccentGreen = Color(0xFF7BE495)
val AccentPurple = Color(0xFFB8A1FF)
val AccentOrange = Color(0xFFFFB86B)
val AccentPink = Color(0xFFFF8FC7)

/** Resolves a persisted accent choice to its Compose color. */
fun AccentColor.toColor(): Color = when (this) {
    AccentColor.BLUE -> ElectricBlue
    AccentColor.TEAL -> AccentTeal
    AccentColor.GREEN -> AccentGreen
    AccentColor.PURPLE -> AccentPurple
    AccentColor.ORANGE -> AccentOrange
    AccentColor.PINK -> AccentPink
}

/** Resolves a persisted launcher background to its base Compose color. */
fun LauncherBackground.toColor(): Color = when (this) {
    LauncherBackground.MIDNIGHT -> Midnight
    LauncherBackground.SLATE -> BackgroundSlate
    LauncherBackground.GRAPHITE -> BackgroundGraphite
    LauncherBackground.BLACK -> BackgroundBlack
    LauncherBackground.NAVY -> BackgroundNavy
    LauncherBackground.OCEAN -> BackgroundOcean
}
