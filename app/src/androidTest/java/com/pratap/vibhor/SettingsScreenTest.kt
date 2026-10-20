package com.pratap.vibhor

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.platform.app.InstrumentationRegistry
import com.pratap.vibhor.core.platform.AnimationStyle
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.feature.cockpit.CockpitTestTags
import com.pratap.vibhor.feature.settings.SettingsTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsPageShowsGroupedSections() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }
        composeRule.onNodeWithTag(CockpitTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Settings").performClick()

        composeRule.onNodeWithTag(SettingsTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsTestTags.SectionClock).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionClockBackdrop).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionLauncherBackground).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionAppearance).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionHomeApps).assertExists()
        composeRule.onNodeWithTag(SettingsTestTags.SectionAnimations).assertExists()
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

    @Test
    fun animationStyleCanBeSelected() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }
        composeRule.onNodeWithTag(CockpitTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Settings").performClick()

        composeRule.onNodeWithTag(SettingsTestTags.SectionAnimations).performScrollTo()
        composeRule.onNodeWithTag(
            SettingsTestTags.AnimationOptionPrefix + AnimationStyle.PARALLAX.id,
        )
            .performScrollTo()
            .performClick()
            .assertIsSelected()
    }

    @Test
    fun animationStylePersistsAndSurvivesRecreation() {
        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }
        composeRule.onNodeWithTag(CockpitTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithTag(SettingsTestTags.SectionAnimations).performScrollTo()
        composeRule.onNodeWithTag(
            SettingsTestTags.AnimationOptionPrefix + AnimationStyle.PARALLAX.id,
        )
            .performScrollTo()
            .performClick()
            .assertIsSelected()

        // The choice must be written to the preferences file a cold start reads.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val stored = context
            .getSharedPreferences("vibhor_launcher_settings", Context.MODE_PRIVATE)
            .getString("animation_style", null)
        assertEquals(AnimationStyle.PARALLAX.id, stored)

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        // Recreate can restore the Settings overlay and the pager position.
        if (composeRule.onAllNodesWithTag(SettingsTestTags.Page)
                .fetchSemanticsNodes().isNotEmpty()
        ) {
            composeRule.onNodeWithTag(SettingsTestTags.Close).performClick()
        }
        if (composeRule.onAllNodesWithTag(CockpitTestTags.Page)
                .fetchSemanticsNodes().isEmpty()
        ) {
            composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }
        }
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithTag(SettingsTestTags.SectionAnimations).performScrollTo()
        composeRule.onNodeWithTag(
            SettingsTestTags.AnimationOptionPrefix + AnimationStyle.PARALLAX.id,
        )
            .performScrollTo()
            .assertIsSelected()
    }
}
