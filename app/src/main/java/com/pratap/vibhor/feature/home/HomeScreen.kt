package com.pratap.vibhor.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.ui.clock.AnalogClockFace
import com.pratap.vibhor.core.ui.clock.ClassicClockFace
import com.pratap.vibhor.core.ui.clock.MinimalClockFace
import com.pratap.vibhor.core.ui.clock.SplitClockFace
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme

object HomeTestTags {
    const val Page = "page_home"
    const val Clock = "home_clock"
    const val Date = "home_date"
}

/**
 * Home shows only the system wallpaper and the clock/date. The wallpaper
 * itself is rendered by the window manager behind this transparent window
 * (android:windowShowWallpaper), so no storage permission is needed and no
 * bitmap is decoded or retained. Dark scrims keep the clock legible over
 * light wallpapers and double as the dark fallback when no wallpaper is set.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(HomeTestTags.Page),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.45f),
                            0.45f to Color.Black.copy(alpha = 0.12f),
                            1.0f to Color.Black.copy(alpha = 0.50f),
                        ),
                    ),
                ),
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(VibhorSpacing.xxl),
        ) {
            val clockSizeSp = (
                minOf(maxHeight.value / 3f, maxWidth.value / 2f)
                    .coerceIn(40f, 220f) * state.clockScale
                ).sp
            val timeModifier = Modifier.testTag(HomeTestTags.Clock)
            val dateModifier = Modifier.testTag(HomeTestTags.Date)
            when (state.clockFace) {
                ClockFace.CLASSIC -> ClassicClockFace(
                    timeText = state.timeText,
                    dateText = state.dateText,
                    baseSize = clockSizeSp,
                    modifier = Modifier.align(Alignment.Center),
                    timeModifier = timeModifier,
                    dateModifier = dateModifier,
                )
                ClockFace.SPLIT -> SplitClockFace(
                    hourText = state.hourText,
                    minuteText = state.minuteText,
                    meridiemText = state.meridiemText,
                    dateText = state.dateText,
                    baseSize = clockSizeSp,
                    modifier = Modifier.align(Alignment.Center),
                    timeModifier = timeModifier,
                    dateModifier = dateModifier,
                )
                ClockFace.MINIMAL -> MinimalClockFace(
                    timeText = state.timeText,
                    baseSize = clockSizeSp,
                    modifier = Modifier.align(Alignment.Center),
                    timeModifier = timeModifier,
                )
                ClockFace.ANALOG -> AnalogClockFace(
                    hour = state.hour,
                    minute = state.minute,
                    second = state.second,
                    dateText = state.dateText,
                    baseSize = clockSizeSp,
                    modifier = Modifier.align(Alignment.Center),
                    timeModifier = timeModifier,
                    dateModifier = dateModifier,
                )
            }
        }
    }
}

@Preview(name = "Home landscape", widthDp = 1024, heightDp = 600)
@Composable
private fun HomeLandscapePreview() {
    VibhorTheme {
        HomeScreen(
            state = HomeUiState(
                timeText = "10:42",
                dateText = "Monday, 19 October",
            ),
        )
    }
}

@Preview(name = "Home portrait", widthDp = 412, heightDp = 915)
@Composable
private fun HomePortraitPreview() {
    VibhorTheme {
        HomeScreen(
            state = HomeUiState(
                timeText = "10:42",
                dateText = "Monday, 19 October",
            ),
        )
    }
}
