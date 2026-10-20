package com.pratap.vibhor

import android.view.WindowManager
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.pratap.vibhor.feature.apps.AppDrawerTestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppDiscoveryTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun wallpaperIsRenderedBehindTheWindow() {
        val flags = composeRule.activity.window.attributes.flags
        assertTrue(
            flags and WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER != 0,
        )
    }

    @Test
    fun installedAppsAppearInTheGrid() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithTag(AppDrawerTestTags.Item)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
