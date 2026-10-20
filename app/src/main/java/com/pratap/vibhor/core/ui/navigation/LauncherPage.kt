package com.pratap.vibhor.core.ui.navigation

/**
 * The three launcher pages. The ordinal is the pager page index:
 * Apps (0), Home (1), Cockpit (2). With a standard pager, swiping left
 * (finger moving left) advances to the next page, so swipe-left from
 * Home reaches Cockpit and swipe-right reaches Apps.
 */
enum class LauncherPage {
    Apps,
    Home,
    Cockpit,
}
