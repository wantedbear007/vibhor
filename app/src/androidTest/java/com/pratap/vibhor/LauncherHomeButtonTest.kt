package com.pratap.vibhor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.platform.app.InstrumentationRegistry
import com.pratap.vibhor.core.platform.HomeButtonTarget
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.feature.home.HomeTestTags
import com.pratap.vibhor.feature.music.MusicTestTags
import org.junit.After
import org.junit.Rule
import org.junit.Test

class LauncherHomeButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val settingsStore
        get() = SharedPreferencesSettingsStore.get(
            InstrumentationRegistry.getInstrumentation().targetContext,
        )

    @After
    fun restoreDefaultTarget() {
        settingsStore.setHomeButtonTarget(HomeButtonTarget.DEFAULT)
    }

    @Test
    fun playerTargetOpensOnLaunchAndAfterHomeRequest() {
        settingsStore.setHomeButtonTarget(HomeButtonTarget.Player)
        var homeRequestToken by mutableIntStateOf(0)

        composeRule.setContent {
            VibhorTheme {
                VibhorApp(
                    homeRequestToken = homeRequestToken,
                    startOnHomeTarget = true,
                )
            }
        }

        composeRule.onNodeWithTag(MusicTestTags.Page).assertIsDisplayed()

        composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeLeft() }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(HomeTestTags.Page)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(HomeTestTags.Page).assertIsDisplayed()

        composeRule.runOnIdle { homeRequestToken++ }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(MusicTestTags.Page)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(MusicTestTags.Page).assertIsDisplayed()
    }
}
