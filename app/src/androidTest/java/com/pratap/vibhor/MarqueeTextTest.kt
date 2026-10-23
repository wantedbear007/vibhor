package com.pratap.vibhor

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.core.ui.components.MarqueeText
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MarqueeTextTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longTextScrollsWhileShortTextStaysStill() {
        val longTitle =
            "A very long track title that cannot fit inside this narrow ticker viewport"
        var title by mutableStateOf(longTitle)
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            VibhorTheme {
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 48.dp)
                        .background(Color.Black)
                        .testTag(ViewportTag),
                ) {
                    MarqueeText(
                        text = title,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        composeRule.mainClock.advanceTimeBy(100L)
        val longFirst = capture()
        composeRule.mainClock.advanceTimeBy(2_500L)
        val longSecond = capture()
        assertFalse(
            "Long titles should move across the ticker",
            longFirst.sameAs(longSecond),
        )

        composeRule.runOnIdle { title = "Short" }
        composeRule.mainClock.advanceTimeBy(1_000L)
        val shortFirst = capture()
        composeRule.mainClock.advanceTimeBy(2_500L)
        val shortSecond = capture()
        assertTrue(
            "Short titles should stay stationary",
            shortFirst.sameAs(shortSecond),
        )
    }

    private fun capture(): Bitmap =
        composeRule.onNodeWithTag(ViewportTag)
            .captureToImage()
            .asAndroidBitmap()

    private companion object {
        const val ViewportTag = "marquee_test_viewport"
    }
}
