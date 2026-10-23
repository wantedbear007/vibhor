package com.pratap.vibhor

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.provider.Settings
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.pratap.vibhor.feature.blackout.BlackoutTestTags
import com.pratap.vibhor.feature.home.HomeTestTags
import org.junit.Assume.assumeTrue
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
    fun nowPlayingTickerFollowsTheRealSessionAndHidesWithoutMetadataOrWhenStopped() {
        assumeTrue(
            "notification access not granted; see MusicScreenTest kdoc",
            hasNotificationAccess(),
        )

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var session: MediaSession
        instrumentation.runOnMainSync {
            session = MediaSession(context, "vibhor-home-ticker-test").apply {
                setMetadata(
                    MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, "Short")
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artist")
                        .build(),
                )
                setPlaybackState(
                    PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PLAYING, 0L, 1f)
                        .build(),
                )
                isActive = true
            }
        }

        try {
            composeRule.waitUntil(timeoutMillis = 5_000L) {
                composeRule.onAllNodesWithTag(HomeTestTags.NowPlayingTicker)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Short").assertIsDisplayed()

            val longTitle = "A much longer track title that scrolls across Home"
            instrumentation.runOnMainSync {
                session.setMetadata(
                    MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, longTitle)
                        .build(),
                )
            }
            composeRule.waitUntil(timeoutMillis = 5_000L) {
                composeRule.onAllNodesWithText(longTitle)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            instrumentation.runOnMainSync {
                session.setPlaybackState(
                    PlaybackState.Builder()
                        .setState(PlaybackState.STATE_STOPPED, 0L, 1f)
                        .build(),
                )
            }
            composeRule.waitUntil(timeoutMillis = 5_000L) {
                composeRule.onAllNodesWithTag(HomeTestTags.NowPlayingTicker)
                    .fetchSemanticsNodes().isEmpty()
            }

            instrumentation.runOnMainSync {
                session.setMetadata(null)
                session.setPlaybackState(
                    PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PLAYING, 0L, 1f)
                        .build(),
                )
            }
            composeRule.waitUntil(timeoutMillis = 5_000L) {
                composeRule.onAllNodesWithTag(HomeTestTags.NowPlayingTicker)
                    .fetchSemanticsNodes().isEmpty()
            }
        } finally {
            instrumentation.runOnMainSync {
                session.isActive = false
                session.release()
            }
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

    private fun hasNotificationAccess(): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return flat.split(':').any { entry ->
            ComponentName.unflattenFromString(entry)?.packageName == context.packageName
        }
    }
}
