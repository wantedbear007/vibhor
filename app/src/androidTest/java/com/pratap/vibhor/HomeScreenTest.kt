package com.pratap.vibhor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    fun homeHasNoBrandingCardsOrActions() {
        composeRule.onNodeWithText("Vibhor").assertDoesNotExist()
        composeRule.onNodeWithText("Navigation").assertDoesNotExist()
        composeRule.onNodeWithText("Media").assertDoesNotExist()
        composeRule.onNodeWithText("App drawer").assertDoesNotExist()
        composeRule.onNodeWithText("Settings").assertDoesNotExist()
    }
}
