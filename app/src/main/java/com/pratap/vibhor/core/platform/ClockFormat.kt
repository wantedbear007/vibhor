package com.pratap.vibhor.core.platform

/**
 * Clock time format shown on Home. [SYSTEM] follows the device's 12/24-hour
 * setting; the others force a format regardless of the system. Ids are stable
 * strings persisted in settings; unknown values fall back to [SYSTEM].
 */
enum class ClockFormat(val id: String) {
    SYSTEM("system"),
    H12("12"),
    H24("24"),
    ;

    companion object {
        val DEFAULT: ClockFormat = SYSTEM

        fun fromId(id: String?): ClockFormat =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
