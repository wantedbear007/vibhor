package com.pratap.vibhor.core.platform

data class NavigationApp(
    val label: String,
    val packageName: String,
    val activityName: String?,
)

/**
 * Resolves installed apps that provide map/navigation functionality. Returns
 * an empty list when the device has none; callers must show an honest empty
 * state rather than fabricating a navigation experience.
 */
interface NavigationAppsSource {
    fun resolveNavigationApps(): List<NavigationApp>
}
