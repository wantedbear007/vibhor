package com.pratap.vibhor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.feature.blackout.BlackoutScreen
import com.pratap.vibhor.feature.blackout.BlackoutTestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class BlackoutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clockAndTrackAreCenteredAndDismissSitsBottomRight() {
        var dismissed = false
        composeRule.setContent {
            VibhorTheme {
                BlackoutScreen(
                    timeText = "9:41",
                    trackTitle = "Test Track",
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeRule.onNodeWithTag(BlackoutTestTags.Page).assertIsDisplayed()
        composeRule.onNodeWithTag(BlackoutTestTags.Time).assertTextEquals("9:41")
        composeRule.onNodeWithTag(BlackoutTestTags.Track).assertTextEquals("Test Track")

        val page = composeRule.onNodeWithTag(BlackoutTestTags.Page)
            .fetchSemanticsNode().boundsInRoot
        val time = composeRule.onNodeWithTag(BlackoutTestTags.Time)
            .fetchSemanticsNode().boundsInRoot
        val track = composeRule.onNodeWithTag(BlackoutTestTags.Track)
            .fetchSemanticsNode().boundsInRoot
        val dismiss = composeRule.onNodeWithTag(BlackoutTestTags.Dismiss)
            .fetchSemanticsNode().boundsInRoot

        assertEquals(page.center.x, time.center.x, 1f)
        assertEquals(page.center.x, track.center.x, 1f)
        assertTrue(
            "Time and track should be vertically centered",
            abs((time.top + track.bottom) / 2f - page.center.y) < page.height * 0.1f,
        )

        assertTrue("Dismiss should sit on the right", dismiss.left > page.center.x)
        assertTrue("Dismiss should sit at the bottom", dismiss.top > track.bottom)
        assertTrue(dismiss.right <= page.right)
        assertTrue(dismiss.bottom <= page.bottom)

        composeRule.onNodeWithTag(BlackoutTestTags.Dismiss).performClick()
        assertTrue(dismissed)
    }
}
