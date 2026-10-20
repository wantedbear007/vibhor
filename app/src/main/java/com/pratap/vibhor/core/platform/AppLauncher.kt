package com.pratap.vibhor.core.platform

/**
 * Launches installed activities. Kept behind an interface so presentation
 * code never calls Android startActivity directly and unit tests can fake it.
 */
interface AppLauncher {
    fun canLaunch(packageName: String): Boolean
    fun launch(packageName: String, activityName: String?): Boolean
}
