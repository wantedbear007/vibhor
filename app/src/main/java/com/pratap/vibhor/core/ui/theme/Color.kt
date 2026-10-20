package com.pratap.vibhor.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.pratap.vibhor.core.platform.AccentColor
import com.pratap.vibhor.core.platform.LauncherBackground

/** Brand surfaces: a near-black chrome color and neutral raised panels. */
val Midnight = Color(0xFF0A0F16)
val DeepSlate = Color(0xFF14181E)
val SurfaceElevated = Color(0xFF1D2127)
val PrimaryText = Color(0xFFF4F6F8)
val SecondaryText = Color(0xFFA0AAB7)

/**
 * Background palette. Black is the default launcher surface; the remaining
 * choices stay deliberately dark and low-chroma so text and accents retain
 * contrast on every option.
 */
val BackgroundSlate = Color(0xFF11161D)
val BackgroundGraphite = Color(0xFF23272E)
val BackgroundBlack = Color(0xFF000000)
val BackgroundNavy = Color(0xFF07172B)
val BackgroundOcean = Color(0xFF062028)

/**
 * Default accent. UI code should read the active accent from
 * [VibhorThemeColors.accent] rather than this constant.
 */
val AccentBlue = Color(0xFF6C9FFF)

/** Accent picker palette, ordered like the settings swatches. */
val AccentTeal = Color(0xFF4DD8C8)
val AccentGreen = Color(0xFF74D68E)
val AccentPurple = Color(0xFFB28CFF)
val AccentOrange = Color(0xFFFFB168)
val AccentPink = Color(0xFFF78DBF)

/** Resolves a persisted accent choice to its Compose color. */
fun AccentColor.toColor(): Color = when (this) {
    AccentColor.BLUE -> AccentBlue
    AccentColor.TEAL -> AccentTeal
    AccentColor.GREEN -> AccentGreen
    AccentColor.PURPLE -> AccentPurple
    AccentColor.ORANGE -> AccentOrange
    AccentColor.PINK -> AccentPink
}

/** Resolves a persisted launcher background to its base Compose color. */
fun LauncherBackground.toColor(): Color = when (this) {
    LauncherBackground.BLACK -> BackgroundBlack
    LauncherBackground.MIDNIGHT -> Midnight
    LauncherBackground.SLATE -> BackgroundSlate
    LauncherBackground.GRAPHITE -> BackgroundGraphite
    LauncherBackground.NAVY -> BackgroundNavy
    LauncherBackground.OCEAN -> BackgroundOcean
}
