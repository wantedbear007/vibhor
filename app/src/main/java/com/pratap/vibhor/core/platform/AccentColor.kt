package com.pratap.vibhor.core.platform

/**
 * Accent choices for the launcher. Stored by stable [id] so the persisted
 * value survives renames and app updates. All UI reads its accent from the
 * theme, which is driven by this setting.
 */
enum class AccentColor(val id: String) {
    BLUE("blue"),
    TEAL("teal"),
    GREEN("green"),
    PURPLE("purple"),
    ORANGE("orange"),
    PINK("pink"),
    WHITE("white"),
    ;

    companion object {
        val DEFAULT = WHITE

        fun fromId(id: String?): AccentColor =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
