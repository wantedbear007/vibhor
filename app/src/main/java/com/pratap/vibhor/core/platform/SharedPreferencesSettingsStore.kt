package com.pratap.vibhor.core.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * SharedPreferences-backed settings. A single process-wide instance is
 * shared so every ViewModel observes the same flow; changes made on the
 * settings screen are immediately visible to the home and app-drawer screens.
 */
class SharedPreferencesSettingsStore private constructor(
    context: Context,
) : SettingsStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val state = MutableStateFlow(read())

    override val settings: StateFlow<LauncherSettings> = state.asStateFlow()

    override fun setClockScale(scale: Float) {
        persist(KEY_CLOCK_SCALE, scale) { current, clamped ->
            current.copy(clockScale = clamped)
        }
    }

    override fun setAppIconScale(scale: Float) {
        persist(KEY_ICON_SCALE, scale) { current, clamped ->
            current.copy(appIconScale = clamped)
        }
    }

    override fun setClockFace(face: ClockFace) {
        prefs.edit { putString(KEY_CLOCK_FACE, face.id) }
        state.update { it.copy(clockFace = face) }
    }

    override fun setClockFormat(format: ClockFormat) {
        prefs.edit { putString(KEY_CLOCK_FORMAT, format.id) }
        state.update { it.copy(clockFormat = format) }
    }

    private fun read(): LauncherSettings = LauncherSettings(
        clockScale = prefs
            .getFloat(KEY_CLOCK_SCALE, LauncherSettings.DEFAULT_SCALE)
            .coerceIn(LauncherSettings.MIN_SCALE, LauncherSettings.MAX_SCALE),
        appIconScale = prefs
            .getFloat(KEY_ICON_SCALE, LauncherSettings.DEFAULT_SCALE)
            .coerceIn(LauncherSettings.MIN_SCALE, LauncherSettings.MAX_SCALE),
        clockFace = ClockFace.fromId(prefs.getString(KEY_CLOCK_FACE, null)),
        clockFormat = ClockFormat.fromId(prefs.getString(KEY_CLOCK_FORMAT, null)),
    )

    private fun persist(
        key: String,
        scale: Float,
        transform: (LauncherSettings, Float) -> LauncherSettings,
    ) {
        val clamped = scale.coerceIn(LauncherSettings.MIN_SCALE, LauncherSettings.MAX_SCALE)
        prefs.edit { putFloat(key, clamped) }
        state.update { transform(it, clamped) }
    }

    companion object {
        private const val PREFS_NAME = "vibhor_launcher_settings"
        private const val KEY_CLOCK_SCALE = "clock_scale"
        private const val KEY_ICON_SCALE = "app_icon_scale"
        private const val KEY_CLOCK_FACE = "clock_face"
        private const val KEY_CLOCK_FORMAT = "clock_format"

        @Volatile
        private var instance: SharedPreferencesSettingsStore? = null

        fun get(context: Context): SettingsStore = instance ?: synchronized(this) {
            instance
                ?: SharedPreferencesSettingsStore(context.applicationContext)
                    .also { instance = it }
        }
    }
}
