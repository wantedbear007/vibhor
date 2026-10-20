package com.pratap.vibhor.core.platform

/**
 * The launcher screen that the system Home button returns to. The persisted
 * value also seeds the pager when the launcher activity is started from the
 * Home intent.
 */
enum class HomeButtonTarget(val id: String) {
    Clock("clock"),
    Player("player"),
    ;

    companion object {
        val DEFAULT = Clock

        fun fromId(id: String?): HomeButtonTarget =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
