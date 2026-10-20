package com.pratap.vibhor

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.pratap.vibhor.feature.apps.AppDrawerTestTags
import com.pratap.vibhor.feature.apps.UnifiedAppsTestTags
import com.pratap.vibhor.feature.blackout.BlackoutTestTags
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
        composeRule.onNodeWithTag(UnifiedAppsTestTags.MuteTile).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.BlackoutTile).assertIsDisplayed()
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
    fun connectivityTilesExposeSystemSettingsLongPress() {
        openUnifiedApps()

        val longClickAction = SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick)
        composeRule.onNodeWithTag(UnifiedAppsTestTags.BluetoothTile).assert(longClickAction)
        composeRule.onNodeWithTag(UnifiedAppsTestTags.WifiTile).assert(longClickAction)
        composeRule.onNodeWithTag(UnifiedAppsTestTags.NightTile)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))
        composeRule.onNodeWithTag(UnifiedAppsTestTags.MuteTile)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))
    }

    @Test
    fun settingsButtonOpensSettingsOverlay() {
        openUnifiedApps()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.SettingsButton).performClick()

        composeRule.onNodeWithTag(SettingsTestTags.Page).assertIsDisplayed()
    }

    @Test
    fun blackoutTileOpensImmersivePageAndDismissesIt() {
        openUnifiedApps()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.BlackoutTile).performClick()

        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(BlackoutTestTags.Time).assertExists()
        composeRule.onNodeWithTag(BlackoutTestTags.Track).assertExists()

        composeRule.onNodeWithTag(BlackoutTestTags.Dismiss).performClick()

        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertDoesNotExist()
    }

    @Test
    fun backClosesBlackoutBeforeLeavingApps() {
        openUnifiedApps()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.BlackoutTile).performClick()
        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertDoesNotExist()
        composeRule.onNodeWithTag(AppDrawerTestTags.Page).assertIsDisplayed()
    }

    private fun openUnifiedApps() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag(AppDrawerTestTags.Page).assertIsDisplayed()
    }
}
