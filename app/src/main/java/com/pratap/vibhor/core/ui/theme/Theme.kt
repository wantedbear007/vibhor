package com.pratap.vibhor.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pratap.vibhor.core.platform.AccentColor

/**
 * The single source of accent color for the whole launcher. Every screen —
 * existing and future — must read accents from here (or from
 * [MaterialTheme.colorScheme.primary], which is derived from the same value)
 * rather than hardcoding a color.
 */
val LocalAccentColor = staticCompositionLocalOf { AccentBlue }

/** Typed accessors for the launcher palette. */
object VibhorThemeColors {
    val accent: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAccentColor.current

    val accentOn: Color
        @Composable
        @ReadOnlyComposable
        get() = BackgroundBlack
}

/**
 * Applies the user's persisted [accentColor] to the Material color scheme and
 * exposes it through [LocalAccentColor] for custom-drawn UI.
 */
@Composable
fun VibhorTheme(
    accentColor: AccentColor = AccentColor.DEFAULT,
    content: @Composable () -> Unit,
) {
    val accent = accentColor.toColor()
    val colorScheme = remember(accent) {
        darkColorScheme(
            primary = accent,
            onPrimary = BackgroundBlack,
            primaryContainer = DeepSlate,
            onPrimaryContainer = accent,
            secondary = accent,
            onSecondary = BackgroundBlack,
            background = BackgroundBlack,
            onBackground = PrimaryText,
            surface = DeepSlate,
            onSurface = PrimaryText,
            surfaceVariant = SurfaceElevated,
            onSurfaceVariant = SecondaryText,
            outline = SecondaryText,
            outlineVariant = SurfaceElevated,
        )
    }
    CompositionLocalProvider(LocalAccentColor provides accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VibhorTypography,
            shapes = VibhorShapes,
            content = content,
        )
    }
}
