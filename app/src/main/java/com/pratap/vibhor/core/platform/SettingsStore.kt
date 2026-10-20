package com.pratap.vibhor.core.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Launcher settings that the user can change at runtime. Scales are
 * multipliers applied to the responsive computed sizes, so one setting
 * works across screen densities and orientations.
 */
data class LauncherSettings(
    val clockScale: Float = 1f,
    val appIconScale: Float = 1f,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val clockFormat: ClockFormat = ClockFormat.DEFAULT,
) {
    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 2f
        const val DEFAULT_SCALE = 1f
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
}
