package com.pratap.vibhor.core.platform

/**
 * Persisted swipe transition style for the launcher pager. Stored by stable
 * [id] so the persisted value survives renames and app updates, and applied
 * immediately to every transition between Home, Cockpit, and Apps.
 */
enum class AnimationStyle(val id: String) {
    SMOOTH_SLIDE("smooth_slide"),
    DEPTH("depth"),
    SOFT_FADE("soft_fade"),
    PARALLAX("parallax"),
    CARD_STACK("card_stack"),
    TILT_CARDS("tilt_cards"),
    CUBE("cube"),
    ;

    companion object {
        val DEFAULT = SMOOTH_SLIDE

        fun fromId(id: String?): AnimationStyle =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
