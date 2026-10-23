package com.pratap.vibhor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.feature.home.HomeScreen
import com.pratap.vibhor.feature.home.HomeTestTags
import com.pratap.vibhor.feature.home.HomeUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeNowPlayingTickerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tickerAppearsOnlyWithARealTitleAndSitsAboveTheHomeActions() {
        var title by mutableStateOf<String?>(null)
        composeRule.setContent {
            VibhorTheme {
                HomeScreen(
                    state = HomeUiState(
                        timeText = "10:42",
                        dateText = "Monday, 19 October",
                    ),
                    onLaunchApp = {},
                    onOpenAppDrawer = {},
                    onOpenBlackout = {},
                    nowPlayingTitle = title,
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.NowPlayingTicker).assertDoesNotExist()

        composeRule.runOnIdle { title = "Short track" }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Short track").assertIsDisplayed()

        val ticker = composeRule.onNodeWithTag(HomeTestTags.NowPlayingTicker)
            .fetchSemanticsNode().boundsInRoot
        val actions = composeRule.onNodeWithTag(HomeTestTags.BlackoutButton)
            .fetchSemanticsNode().boundsInRoot
        val density = InstrumentationRegistry.getInstrumentation()
            .targetContext.resources.displayMetrics.density
        assertTrue(
            "The ticker should sit above the Home action row",
            ticker.bottom <= actions.top,
        )
        assertTrue(
            "The ticker should leave a little breathing room above the actions",
            actions.top - ticker.bottom >= 16.dp.value * density,
        )

        composeRule.runOnIdle { title = null }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(HomeTestTags.NowPlayingTicker).assertDoesNotExist()
    }

    @Test
    fun showOnHomeSettingHidesAndRestoresTheTicker() {
        var showTicker by mutableStateOf(true)
        composeRule.setContent {
            VibhorTheme {
                HomeScreen(
                    state = HomeUiState(
                        timeText = "10:42",
                        dateText = "Monday, 19 October",
                        showNowPlayingTicker = showTicker,
                    ),
                    onLaunchApp = {},
                    onOpenAppDrawer = {},
                    onOpenBlackout = {},
                    nowPlayingTitle = "Track title",
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.NowPlayingTicker).assertIsDisplayed()

        composeRule.runOnIdle { showTicker = false }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(HomeTestTags.NowPlayingTicker).assertDoesNotExist()

        composeRule.runOnIdle { showTicker = true }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(HomeTestTags.NowPlayingTicker).assertIsDisplayed()
    }

    @Test
    fun changingTheTextSizeUpdatesTheTickerImmediately() {
        var textSizeSp by mutableFloatStateOf(12f)
        composeRule.setContent {
            VibhorTheme {
                HomeScreen(
                    state = HomeUiState(
                        timeText = "10:42",
                        dateText = "Monday, 19 October",
                        nowPlayingTextSizeSp = textSizeSp,
                    ),
                    onLaunchApp = {},
                    onOpenAppDrawer = {},
                    onOpenBlackout = {},
                    nowPlayingTitle = "Track title",
                )
            }
        }

        composeRule.waitForIdle()
        val smallHeight = composeRule.onNodeWithTag(HomeTestTags.NowPlayingText)
            .fetchSemanticsNode().boundsInRoot.height

        composeRule.runOnIdle { textSizeSp = 28f }
        composeRule.waitForIdle()
        val largeHeight = composeRule.onNodeWithTag(HomeTestTags.NowPlayingText)
            .fetchSemanticsNode().boundsInRoot.height

        assertTrue(
            "The ticker should grow with the persisted sp value",
            largeHeight > smallHeight + 8f,
        )
        composeRule.onNodeWithTag(HomeTestTags.NowPlayingTicker)
            .assertIsDisplayed()
    }
}
