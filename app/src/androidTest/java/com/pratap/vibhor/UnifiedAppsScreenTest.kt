package com.pratap.vibhor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.pratap.vibhor.feature.apps.AppDrawerTestTags
import com.pratap.vibhor.feature.apps.UnifiedAppsTestTags
import com.pratap.vibhor.feature.settings.SettingsTestTags
import org.junit.Rule
import org.junit.Test

class UnifiedAppsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun swipeLeftOpensControlsAndAppsTogether() {
        openUnifiedApps()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.ControlPanel).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.Clock).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.Date).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.VolumeSlider).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.BrightnessSlider).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.BluetoothTile).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.WifiTile).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.NightTile).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.SettingsButton).assertIsDisplayed()
        composeRule.onNodeWithTag(AppDrawerTestTags.Search).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithTag(AppDrawerTestTags.Item)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(AppDrawerTestTags.Grid).assertIsDisplayed()
    }

    @Test
    fun settingsButtonOpensSettingsOverlay() {
        openUnifiedApps()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.SettingsButton).performClick()

        composeRule.onNodeWithTag(SettingsTestTags.Page).assertIsDisplayed()
    }

    private fun openUnifiedApps() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag(AppDrawerTestTags.Page).assertIsDisplayed()
    }
}
