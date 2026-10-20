package com.pratap.vibhor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.pratap.vibhor.feature.apps.AppDrawerTestTags
import com.pratap.vibhor.feature.cockpit.CockpitTestTags
import org.junit.Rule
import org.junit.Test

class LauncherPagerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun swipeLeftFromHomeOpensCockpit() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeLeft() }

        composeRule.onNodeWithTag(CockpitTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(CockpitTestTags.VolumeSlider).assertIsDisplayed()
        composeRule.onNodeWithTag(CockpitTestTags.BrightnessSlider).assertIsDisplayed()
        composeRule.onNodeWithText("Navigation").assertDoesNotExist()
    }

    @Test
    fun swipeRightFromHomeOpensApps() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }

        composeRule.onNodeWithTag(AppDrawerTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(AppDrawerTestTags.Search).assertIsDisplayed()
        composeRule.onNodeWithTag(AppDrawerTestTags.TabFavourites).assertIsDisplayed()
        composeRule.onNodeWithTag(AppDrawerTestTags.TabAll).assertIsDisplayed()
    }
}
