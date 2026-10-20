package com.pratap.vibhor.core.platform

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri

/**
 * Finds navigation apps via the platform's own signals: the APP_MAPS
 * category first, then the geo: URI scheme as a fallback. No permissions
 * are required and no package list is enumerated beyond what the system
 * reports as handling these intents.
 */
class AndroidNavigationAppsSource(
    private val context: Context,
) : NavigationAppsSource {

    override fun resolveNavigationApps(): List<NavigationApp> {
        val packageManager = context.packageManager
        val mapsIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MAPS)
        var apps = packageManager.queryIntentActivities(mapsIntent, 0)
            .mapNotNull { it.toNavigationApp(packageManager) }
        if (apps.isEmpty()) {
            val geoIntent = Intent(Intent.ACTION_VIEW, "geo:0,0".toUri())
            apps = packageManager.queryIntentActivities(geoIntent, 0)
                .mapNotNull { it.toNavigationApp(packageManager) }
        }
        return apps.distinctBy { it.packageName }
    }

    private fun android.content.pm.ResolveInfo.toNavigationApp(
        packageManager: PackageManager,
    ): NavigationApp? {
        val packageName = activityInfo?.packageName ?: return null
        return NavigationApp(
            label = loadLabel(packageManager).toString(),
            packageName = packageName,
            activityName = activityInfo?.name,
        )
    }
}
