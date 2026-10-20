package com.pratap.vibhor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.pratap.vibhor.feature.apps.AppDrawerTestTags
import com.pratap.vibhor.feature.apps.UnifiedAppsTestTags
import com.pratap.vibhor.feature.music.MusicTestTags
import org.junit.Rule
import org.junit.Test

class LauncherPagerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun swipeLeftFromHomeOpensUnifiedApps() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeLeft() }

        composeRule.onNodeWithTag(AppDrawerTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.ControlPanel).assertIsDisplayed()
        composeRule.onNodeWithTag(AppDrawerTestTags.Search).assertIsDisplayed()
        composeRule.onNodeWithTag(AppDrawerTestTags.TabFavourites).assertIsDisplayed()
        composeRule.onNodeWithTag(AppDrawerTestTags.TabAll).assertIsDisplayed()
    }

    @Test
    fun swipeRightFromHomeOpensMusic() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }

        composeRule.onNodeWithTag(MusicTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(MusicTestTags.Astronaut).assertIsDisplayed()
        composeRule.onNodeWithTag(MusicTestTags.Card).assertIsDisplayed()
        composeRule.onNodeWithTag(MusicTestTags.VolumeSlider).assertIsDisplayed()
        composeRule.onNodeWithText("Brightness").assertDoesNotExist()
        composeRule.onNodeWithText("Navigation").assertDoesNotExist()
    }
}
