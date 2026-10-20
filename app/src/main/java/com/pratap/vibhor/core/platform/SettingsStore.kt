package com.pratap.vibhor.core.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Launcher settings that the user can change at runtime. Scales are
 * multipliers applied to the responsive computed sizes, so one setting
 * works across screen densities and orientations. [clockBlurRadius] and
 * [backgroundDarkness] drive the Home clock backdrop.
 */
data class LauncherSettings(
    val clockScale: Float = 1f,
    val appIconScale: Float = 1f,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val clockFormat: ClockFormat = ClockFormat.DEFAULT,
    val clockBlurRadius: Int = DEFAULT_BLUR_RADIUS,
    val backgroundDarkness: Float = DEFAULT_DARKNESS,
    val showStatusBar: Boolean = DEFAULT_SHOW_STATUS_BAR,
    val quickButtonScale: Float = DEFAULT_QUICK_BUTTON_SCALE,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val favouritePackageNames: Set<String> = emptySet(),
) {
    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 2f
        const val DEFAULT_SCALE = 1f

        /** Blur radius bounds: 0 disables blur; 8 is a subtle backdrop. */
        const val MIN_BLUR_RADIUS = 0
        const val MAX_BLUR_RADIUS = 20
        const val DEFAULT_BLUR_RADIUS = 8

        /** Backdrop darkness 0f (transparent) .. 1f (opaque dark). */
        const val MIN_DARKNESS = 0f
        const val MAX_DARKNESS = 1f
        const val DEFAULT_DARKNESS = 0.35f

        const val DEFAULT_SHOW_STATUS_BAR = true

        const val MIN_QUICK_BUTTON_SCALE = 0.5f
        const val MAX_QUICK_BUTTON_SCALE = 2f
        const val DEFAULT_QUICK_BUTTON_SCALE = 1f
    }
}

/**
 * Observable launcher settings with write-through persistence. Backed by
 * SharedPreferences in production and memory in tests/previews.
 */
interface SettingsStore {
    val settings: StateFlow<LauncherSettings>
    fun setClockScale(scale: Float)
    fun setAppIconScale(scale: Float)
    fun setClockFace(face: ClockFace)
    fun setClockFormat(format: ClockFormat)
    fun setClockBlurRadius(radius: Int)
    fun setBackgroundDarkness(darkness: Float)
    fun setShowStatusBar(show: Boolean)
    fun setQuickButtonScale(scale: Float)
    fun setAccentColor(color: AccentColor)
    fun addFavourite(packageName: String)
    fun removeFavourite(packageName: String)
    fun toggleFavourite(packageName: String)
}

class InMemorySettingsStore(
    initial: LauncherSettings = LauncherSettings(),
) : SettingsStore {

    private val state = MutableStateFlow(
        initial.copy(
            clockScale = initial.clockScale.coerceIn(
                LauncherSettings.MIN_SCALE,
                LauncherSettings.MAX_SCALE,
            ),
            appIconScale = initial.appIconScale.coerceIn(
                LauncherSettings.MIN_SCALE,
                LauncherSettings.MAX_SCALE,
            ),
            clockBlurRadius = initial.clockBlurRadius.coerceIn(
                LauncherSettings.MIN_BLUR_RADIUS,
                LauncherSettings.MAX_BLUR_RADIUS,
            ),
            backgroundDarkness = initial.backgroundDarkness.coerceIn(
                LauncherSettings.MIN_DARKNESS,
                LauncherSettings.MAX_DARKNESS,
            ),
        ),
    )

    override val settings: StateFlow<LauncherSettings> = state.asStateFlow()

    override fun setClockScale(scale: Float) {
        state.update {
            it.copy(
                clockScale = scale.coerceIn(
                    LauncherSettings.MIN_SCALE,
                    LauncherSettings.MAX_SCALE,
                ),
            )
        }
    }

    override fun setAppIconScale(scale: Float) {
        state.update {
            it.copy(
                appIconScale = scale.coerceIn(
                    LauncherSettings.MIN_SCALE,
                    LauncherSettings.MAX_SCALE,
                ),
            )
        }
    }

    override fun setClockFace(face: ClockFace) {
        state.update { it.copy(clockFace = face) }
    }

    override fun setClockFormat(format: ClockFormat) {
        state.update { it.copy(clockFormat = format) }
    }

    override fun setClockBlurRadius(radius: Int) {
        state.update {
            it.copy(
                clockBlurRadius = radius.coerceIn(
                    LauncherSettings.MIN_BLUR_RADIUS,
                    LauncherSettings.MAX_BLUR_RADIUS,
                ),
            )
        }
    }

    override fun setBackgroundDarkness(darkness: Float) {
        state.update {
            it.copy(
                backgroundDarkness = darkness.coerceIn(
                    LauncherSettings.MIN_DARKNESS,
                    LauncherSettings.MAX_DARKNESS,
                ),
            )
        }
    }

    override fun setShowStatusBar(show: Boolean) {
        state.update { it.copy(showStatusBar = show) }
    }

    override fun setQuickButtonScale(scale: Float) {
        state.update {
            it.copy(
                quickButtonScale = scale.coerceIn(
                    LauncherSettings.MIN_QUICK_BUTTON_SCALE,
                    LauncherSettings.MAX_QUICK_BUTTON_SCALE,
                ),
            )
        }
    }

    override fun setAccentColor(color: AccentColor) {
        state.update { it.copy(accentColor = color) }
    }

    override fun addFavourite(packageName: String) {
        val key = packageName.trim()
        if (key.isEmpty()) return
        state.update { it.copy(favouritePackageNames = it.favouritePackageNames + key) }
    }

    override fun removeFavourite(packageName: String) {
        val key = packageName.trim()
        if (key.isEmpty()) return
        state.update { it.copy(favouritePackageNames = it.favouritePackageNames - key) }
    }

    override fun toggleFavourite(packageName: String) {
        val key = packageName.trim()
        if (key.isEmpty()) return
        state.update {
            if (key in it.favouritePackageNames) {
                it.copy(favouritePackageNames = it.favouritePackageNames - key)
            } else {
                it.copy(favouritePackageNames = it.favouritePackageNames + key)
            }
        }
    }
}
