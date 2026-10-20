package com.pratap.vibhor

import androidx.test.platform.app.InstrumentationRegistry
import com.pratap.vibhor.core.platform.AndroidInstalledAppsSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppsSourceTest {

    @Test
    fun launcherActivityWithoutDefaultCategoryIsListed() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val apps = AndroidInstalledAppsSource(context).apps().first()

        assertTrue(
            "Launcher activities without CATEGORY_DEFAULT must be discovered",
            apps.any { it.packageName == "${context.packageName}.test" },
        )
    }
}
