package com.pratap.vibhor.core.ui.navigation

/**
 * The three launcher pages. The ordinal is the pager page index:
 * Cockpit (0), Home (1), Apps (2). With a standard pager, swiping left
 * (finger moving left) advances to the next page, so swipe-left from
 * Home reaches the unified Apps page and swipe-right reaches Cockpit.
 */
enum class LauncherPage {
    Cockpit,
    Home,
    Apps,
}
