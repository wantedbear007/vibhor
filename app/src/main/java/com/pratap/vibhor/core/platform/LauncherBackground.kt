package com.pratap.vibhor.core.platform

/**
 * Persisted launcher surface background. Every option is a dark, low-chroma
 * color so white text and the accent keep their contrast. The persisted
 * [LauncherSettings.backgroundOpacity] makes any choice translucent, and 0f
 * leaves only the system wallpaper visible. Home keeps the wallpaper and its
 * separate clock backdrop; this setting applies to the Apps, Cockpit, and
 * Settings surfaces.
 */
enum class LauncherBackground(val id: String) {
    MIDNIGHT("midnight"),
    SLATE("slate"),
    GRAPHITE("graphite"),
    BLACK("black"),
    NAVY("navy"),
    OCEAN("ocean"),
    ;

    companion object {
        val DEFAULT = MIDNIGHT

        fun fromId(id: String?): LauncherBackground =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
