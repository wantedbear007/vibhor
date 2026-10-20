package com.pratap.vibhor

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.platform.app.InstrumentationRegistry
import com.pratap.vibhor.feature.music.MusicTestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Drives the screen with a real platform [MediaSession] hosted by the test
 * process. Reading sessions requires notification access, which an app cannot
 * grant itself; grant it before running the suite with:
 *
 * `adb shell cmd notification allow_listener \
 *  com.pratap.vibhor/.core.platform.media.VibhorNotificationListenerService`
 *
 * (`settings put secure enabled_notification_listeners ...` alone is not
 * durable on API 28; the notification manager rewrites it.)
 *
 * Without access the test is skipped instead of asserting fake playback.
 */
class MusicScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun activeMediaSessionDrivesThePlayer() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assumeTrue(
            "notification access not granted; see MusicScreenTest kdoc",
            hasNotificationAccess(),
        )

        val pauseCalls = AtomicInteger()
        val nextCalls = AtomicInteger()
        lateinit var session: MediaSession
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            session = MediaSession(context, "vibhor-music-test").apply {
                setCallback(object : MediaSession.Callback() {
                    override fun onPause() {
                        pauseCalls.incrementAndGet()
                    }

                    override fun onSkipToNext() {
                        nextCalls.incrementAndGet()
                    }
                })
                setMetadata(
                    MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, "Test Track")
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, "Test Artist")
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, 180_000L)
                        .build(),
                )
                setPlaybackState(
                    PlaybackState.Builder()
                        .setActions(
                            PlaybackState.ACTION_PLAY or
                                PlaybackState.ACTION_PAUSE or
                                PlaybackState.ACTION_PLAY_PAUSE or
                                PlaybackState.ACTION_SKIP_TO_NEXT or
                                PlaybackState.ACTION_SKIP_TO_PREVIOUS,
                        )
                        .setState(PlaybackState.STATE_PLAYING, 30_000L, 1f)
                        .build(),
                )
                isActive = true
            }
        }

        try {
            composeRule.onNodeWithTag(LauncherTestTags.Pager).performTouchInput { swipeRight() }
            composeRule.waitUntil(timeoutMillis = 5_000L) {
                composeRule.onAllNodesWithTag(MusicTestTags.TrackTitle)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeRule.onNodeWithText("Test Track").assertIsDisplayed()
            composeRule.onNodeWithText("Test Artist").assertIsDisplayed()

            composeRule.onNodeWithTag(MusicTestTags.PlayPause).performClick()
            composeRule.waitUntil(timeoutMillis = 3_000L) { pauseCalls.get() == 1 }

            composeRule.onNodeWithTag(MusicTestTags.Next).performClick()
            composeRule.waitUntil(timeoutMillis = 3_000L) { nextCalls.get() == 1 }

            assertEquals(1, pauseCalls.get())
            assertTrue(nextCalls.get() >= 1)
        } finally {
            instrumentation.runOnMainSync {
                session.isActive = false
                session.release()
            }
        }
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
