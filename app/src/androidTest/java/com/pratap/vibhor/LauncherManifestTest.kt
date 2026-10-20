package com.pratap.vibhor

import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherManifestTest {

    @Test
    fun mainActivityIsRegisteredAsHome() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val launchers = context.packageManager.queryIntentActivities(
            homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )

        assertTrue(
            launchers.any {
                it.activityInfo.packageName == "com.pratap.vibhor" &&
                    it.activityInfo.name == "com.pratap.vibhor.MainActivity"
            },
        )
    }
}
