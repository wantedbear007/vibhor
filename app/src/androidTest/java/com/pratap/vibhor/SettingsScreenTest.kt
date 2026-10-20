package com.pratap.vibhor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.feature.cockpit.CockpitTestTags
import com.pratap.vibhor.feature.settings.SettingsTestTags
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsPageShowsGroupedSections() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag(CockpitTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Settings").performClick()

        composeRule.onNodeWithTag(SettingsTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsTestTags.SectionClock).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionClockBackdrop).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionLauncherBackground).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionAppearance).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionHomeApps).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.ClockSlider).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionLauncherBackground)
            .performScrollTo()
        composeRule.onNodeWithTag(SettingsTestTags.BackgroundVisibilitySlider)
            .assertExists()
        composeRule.onNodeWithTag(
            SettingsTestTags.BackgroundOptionPrefix + LauncherBackground.NAVY.id,
        )
            .performScrollTo()
            .performClick()
            .assertIsSelected()
    }
}
