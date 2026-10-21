package com.pratap.vibhor

import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.pratap.vibhor.feature.blackout.BlackoutTestTags
import com.pratap.vibhor.feature.home.HomeTestTags
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeShowsClockAndDate() {
        composeRule.onNodeWithTag(HomeTestTags.Clock).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.Date).assertIsDisplayed()
    }

    @Test
    fun favouritesButtonOpensTheFavouritesCard() {
        composeRule.onNodeWithTag(HomeTestTags.QuickButton).performClick()
        composeRule.onNodeWithTag(HomeTestTags.QuickPopup).assertIsDisplayed()
    }

    @Test
    fun blackoutButtonOpensBlackoutAndRestoresHome() {
        composeRule.onNodeWithTag(HomeTestTags.BlackoutButton).performClick()
        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertIsDisplayed()

        composeRule.onNodeWithTag(BlackoutTestTags.Dismiss).performClick()
        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertDoesNotExist()
        composeRule.onNodeWithTag(HomeTestTags.Page).assertIsDisplayed()
    }

    @Suppress("DEPRECATION")
    @Test
    fun blackoutHidesSystemBarsThenRestoresNavigation() {
        // The first immersive-mode entry normally shows Android's one-time
        // "Viewing full screen" tutorial, which temporarily owns the screen.
        // Mark it confirmed so this test exercises the launcher's bars.
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(
                "settings put secure immersive_mode_confirmations confirmed",
            )
            .close()

        val hiddenFlags = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        composeRule.onNodeWithTag(HomeTestTags.BlackoutButton).performClick()
        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertIsDisplayed()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            val visibility = composeRule.activity.window.decorView.systemUiVisibility
            (visibility and hiddenFlags) == hiddenFlags
        }

        composeRule.onNodeWithTag(BlackoutTestTags.Dismiss).performClick()
        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertDoesNotExist()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            val visibility = composeRule.activity.window.decorView.systemUiVisibility
            (visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
        }
    }

    @Test
    fun homeHasNoBrandingCardsOrActions() {
        composeRule.onNodeWithText("Vibhor").assertDoesNotExist()
        composeRule.onNodeWithText("Navigation").assertDoesNotExist()
        composeRule.onNodeWithText("Media").assertDoesNotExist()
        composeRule.onNodeWithText("App drawer").assertDoesNotExist()
        composeRule.onNodeWithText("Settings").assertDoesNotExist()
    }
}
