package com.pratap.vibhor.core.platform

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class AndroidAppLauncher(
    private val context: Context,
) : AppLauncher {

    override fun canLaunch(packageName: String): Boolean =
        context.packageManager.getLaunchIntentForPackage(packageName) != null

    override fun launch(packageName: String, activityName: String?): Boolean {
        val intent = if (activityName != null) {
            Intent().setComponent(ComponentName(packageName, activityName))
        } else {
            context.packageManager.getLaunchIntentForPackage(packageName)
        }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return false

        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
