package com.pratap.vibhor.core.platform

/**
 * Styles for the Home clock. Ids are stable strings persisted in settings;
 * unknown values fall back to [CLASSIC].
 */
enum class ClockFace(val id: String) {
    CLASSIC("classic"),
    SPLIT("split"),
    MINIMAL("minimal"),
    BOLD("bold"),
    ELEGANT("elegant"),
    ANALOG("analog"),
    ;

    companion object {
        val DEFAULT: ClockFace = CLASSIC

        fun fromId(id: String?): ClockFace =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
