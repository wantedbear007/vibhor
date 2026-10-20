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

    override fun setClockBlurRadius(radius: Int) {
        val clamped = radius.coerceIn(
            LauncherSettings.MIN_BLUR_RADIUS,
            LauncherSettings.MAX_BLUR_RADIUS,
        )
        prefs.edit { putInt(KEY_CLOCK_BLUR, clamped) }
        state.update { it.copy(clockBlurRadius = clamped) }
    }

    override fun setBackgroundDarkness(darkness: Float) {
        val clamped = darkness.coerceIn(
            LauncherSettings.MIN_DARKNESS,
            LauncherSettings.MAX_DARKNESS,
        )
        prefs.edit { putFloat(KEY_DARKNESS, clamped) }
        state.update { it.copy(backgroundDarkness = clamped) }
    }

    override fun setShowStatusBar(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_STATUS_BAR, show) }
        state.update { it.copy(showStatusBar = show) }
    }

    override fun setQuickButtonScale(scale: Float) {
        val clamped = scale.coerceIn(
            LauncherSettings.MIN_QUICK_BUTTON_SCALE,
            LauncherSettings.MAX_QUICK_BUTTON_SCALE,
        )
        prefs.edit { putFloat(KEY_QUICK_BUTTON_SCALE, clamped) }
        state.update { it.copy(quickButtonScale = clamped) }
    }

    override fun setAccentColor(color: AccentColor) {
        prefs.edit { putString(KEY_ACCENT_COLOR, color.id) }
        state.update { it.copy(accentColor = color) }
    }

    override fun setBackground(background: LauncherBackground) {
        prefs.edit { putString(KEY_BACKGROUND, background.id) }
        state.update { it.copy(background = background) }
    }

    override fun setBackgroundOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(
            LauncherSettings.MIN_BACKGROUND_OPACITY,
            LauncherSettings.MAX_BACKGROUND_OPACITY,
        )
        prefs.edit { putFloat(KEY_BACKGROUND_OPACITY, clamped) }
        state.update { it.copy(backgroundOpacity = clamped) }
    }

    override fun setNightMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NIGHT_MODE, enabled) }
        state.update { it.copy(nightMode = enabled) }
    }

    override fun setAnimationStyle(style: AnimationStyle) {
        prefs.edit { putString(KEY_ANIMATION_STYLE, style.id) }
        state.update { it.copy(animationStyle = style) }
    }

    override fun addFavourite(packageName: String) {
        val key = packageName.trim()
        if (key.isEmpty()) return
        state.update { current ->
            if (key in current.favouritePackageNames) {
                current
            } else {
                val updated = current.favouritePackageNames + key
                prefs.edit { putStringSet(KEY_FAVOURITES, updated) }
                current.copy(favouritePackageNames = updated)
            }
        }
    }

    override fun removeFavourite(packageName: String) {
        val key = packageName.trim()
        if (key.isEmpty()) return
        state.update { current ->
            if (key !in current.favouritePackageNames) {
                current
            } else {
                val updated = current.favouritePackageNames - key
                prefs.edit { putStringSet(KEY_FAVOURITES, updated) }
                current.copy(favouritePackageNames = updated)
            }
        }
    }

    override fun toggleFavourite(packageName: String) {
        val key = packageName.trim()
        if (key.isEmpty()) return
        state.update { current ->
            val updated = if (key in current.favouritePackageNames) {
                current.favouritePackageNames - key
            } else {
                current.favouritePackageNames + key
            }
            prefs.edit { putStringSet(KEY_FAVOURITES, updated) }
            current.copy(favouritePackageNames = updated)
        }
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
        clockBlurRadius = prefs
            .getInt(KEY_CLOCK_BLUR, LauncherSettings.DEFAULT_BLUR_RADIUS)
            .coerceIn(LauncherSettings.MIN_BLUR_RADIUS, LauncherSettings.MAX_BLUR_RADIUS),
        backgroundDarkness = prefs
            .getFloat(KEY_DARKNESS, LauncherSettings.DEFAULT_DARKNESS)
            .coerceIn(LauncherSettings.MIN_DARKNESS, LauncherSettings.MAX_DARKNESS),
        showStatusBar = prefs.getBoolean(
            KEY_SHOW_STATUS_BAR,
            LauncherSettings.DEFAULT_SHOW_STATUS_BAR,
        ),
        quickButtonScale = prefs
            .getFloat(KEY_QUICK_BUTTON_SCALE, LauncherSettings.DEFAULT_QUICK_BUTTON_SCALE)
            .coerceIn(LauncherSettings.MIN_QUICK_BUTTON_SCALE, LauncherSettings.MAX_QUICK_BUTTON_SCALE),
        accentColor = AccentColor.fromId(prefs.getString(KEY_ACCENT_COLOR, null)),
        background = LauncherBackground.fromId(prefs.getString(KEY_BACKGROUND, null)),
        backgroundOpacity = prefs
            .getFloat(KEY_BACKGROUND_OPACITY, LauncherSettings.DEFAULT_BACKGROUND_OPACITY)
            .coerceIn(
                LauncherSettings.MIN_BACKGROUND_OPACITY,
                LauncherSettings.MAX_BACKGROUND_OPACITY,
            ),
        nightMode = prefs.getBoolean(
            KEY_NIGHT_MODE,
            LauncherSettings.DEFAULT_NIGHT_MODE,
        ),
        animationStyle = AnimationStyle.fromId(
            prefs.getString(KEY_ANIMATION_STYLE, null),
        ),
        favouritePackageNames = prefs.getStringSet(KEY_FAVOURITES, emptySet())
            .orEmpty()
            .toSet(),
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
        private const val KEY_CLOCK_BLUR = "clock_blur_radius"
        private const val KEY_DARKNESS = "background_darkness"
        private const val KEY_SHOW_STATUS_BAR = "show_status_bar"
        private const val KEY_QUICK_BUTTON_SCALE = "quick_button_scale"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_BACKGROUND = "background_color"
        private const val KEY_BACKGROUND_OPACITY = "background_opacity"
        private const val KEY_NIGHT_MODE = "night_mode"
        private const val KEY_ANIMATION_STYLE = "animation_style"
        private const val KEY_FAVOURITES = "favourite_package_names"

        @Volatile
        private var instance: SharedPreferencesSettingsStore? = null

        fun get(context: Context): SettingsStore = instance ?: synchronized(this) {
            instance
                ?: SharedPreferencesSettingsStore(context.applicationContext)
                    .also { instance = it }
        }
    }
}
