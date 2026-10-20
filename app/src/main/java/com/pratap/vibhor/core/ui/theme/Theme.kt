package com.pratap.vibhor.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VibhorColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Midnight,
    primaryContainer = DeepSlate,
    onPrimaryContainer = ElectricBlue,
    secondary = ElectricBlue,
    onSecondary = Midnight,
    background = Midnight,
    onBackground = PrimaryText,
    surface = DeepSlate,
    onSurface = PrimaryText,
    surfaceVariant = DeepSlate,
    onSurfaceVariant = SecondaryText,
    outline = SecondaryText,
    outlineVariant = DeepSlate,
)

@Composable
fun VibhorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VibhorColorScheme,
        typography = VibhorTypography,
        shapes = VibhorShapes,
        content = content,
    )
}
