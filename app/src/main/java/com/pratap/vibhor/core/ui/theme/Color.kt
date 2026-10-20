package com.pratap.vibhor.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.pratap.vibhor.core.platform.AccentColor

val Midnight = Color(0xFF0B111A)
val DeepSlate = Color(0xFF142537)
val PrimaryText = Color(0xFFF3F6FA)
val SecondaryText = Color(0xFF91A6BC)

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
