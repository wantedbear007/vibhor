package com.pratap.vibhor.feature.home

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.ui.clock.AnalogClockFace
import com.pratap.vibhor.core.ui.clock.BoldClockFace
import com.pratap.vibhor.core.ui.clock.ClassicClockFace
import com.pratap.vibhor.core.ui.clock.ElegantClockFace
import com.pratap.vibhor.core.ui.clock.MinimalClockFace
import com.pratap.vibhor.core.ui.clock.SplitClockFace
import com.pratap.vibhor.core.ui.theme.DeepSlate
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme

object HomeTestTags {
    const val Page = "page_home"
    const val Clock = "home_clock"
    const val Date = "home_date"
    const val ClockBackdrop = "home_clock_backdrop"
    const val QuickButton = "home_quick_button"
    const val BlackoutButton = "home_blackout_button"
    const val QuickPopup = "home_quick_popup"
}

/**
 * Home shows only the system wallpaper and the clock/date. The wallpaper
 * itself is rendered by the window manager behind this transparent window
 * (android:windowShowWallpaper), so no storage permission is needed and no
 * bitmap is decoded or retained.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onLaunchApp: (InstalledApp) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenBlackout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(HomeTestTags.Page),
        contentAlignment = Alignment.Center,
    ) {
        // Full-screen background over the wallpaper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blurClockBackdrop(state.clockBlurRadius)
                .background(
                    color = Color.Black.copy(alpha = state.backgroundDarkness),
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

            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                ClockFaceContent(
                    state = state,
                    clockSizeSp = clockSizeSp,
                    timeModifier = timeModifier,
                    dateModifier = dateModifier,
                )
            }

            var quickMenuExpanded by remember { mutableStateOf(false) }
            val scale = state.quickButtonScale
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(VibhorSpacing.md)
                    .padding(VibhorSpacing.md * scale),
                horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.sm * scale),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeActionButton(
                    iconRes = R.drawable.ic_fullscreen,
                    label = stringResource(R.string.home_blackout_button),
                    scale = scale,
                    onClick = onOpenBlackout,
                    testTag = HomeTestTags.BlackoutButton,
                )
                Box {
                    HomeActionButton(
                        iconRes = R.drawable.ic_favourite,
                        label = stringResource(R.string.home_quick_button),
                        scale = scale,
                        onClick = { quickMenuExpanded = true },
                        testTag = HomeTestTags.QuickButton,
                    )
                    DropdownMenu(
                        expanded = quickMenuExpanded,
                        onDismissRequest = { quickMenuExpanded = false },
                        modifier = Modifier
                            .widthIn(min = 360.dp, max = 440.dp)
                            .testTag(HomeTestTags.QuickPopup),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Midnight.copy(alpha = 0.94f),
                        tonalElevation = 0.dp,
                        shadowElevation = 16.dp,
                        border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.12f)),
                    ) {
                        if (state.favourites.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.home_quick_empty),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = SecondaryText,
                                    )
                                },
                                onClick = {
                                    quickMenuExpanded = false
                                    onOpenAppDrawer()
                                },
                                contentPadding = PaddingValues(
                                    horizontal = VibhorSpacing.lg,
                                    vertical = VibhorSpacing.md,
                                ),
                            )
                        } else {
                            state.favourites.forEach { app ->
                                QuickAppItem(
                                    app = app,
                                    onClick = {
                                        quickMenuExpanded = false
                                        onLaunchApp(app)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shared Home action pill used for Blackout and Favourites. Both buttons use
 * the persisted Home-action scale so the pair stays visually balanced.
 */
@Composable
private fun HomeActionButton(
    iconRes: Int,
    label: String,
    scale: Float,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp * scale)
    Surface(
        onClick = onClick,
        shape = shape,
        color = Midnight.copy(alpha = 0.64f),
        contentColor = PrimaryText,
        border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.12f)),
        modifier = modifier.testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = VibhorSpacing.lg * scale,
                vertical = VibhorSpacing.md * scale,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.sm * scale),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp * scale)
                    .background(PrimaryText.copy(alpha = 0.06f), CircleShape)
                    .border(
                        width = 1.dp,
                        color = PrimaryText.copy(alpha = 0.10f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.size(18.dp * scale),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = (14 * scale).sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun QuickAppItem(
    app: InstalledApp,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
            ) {
                val icon = remember(app.packageName, app.activityName) {
                    app.icon?.asImageBitmap()
                }
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(DeepSlate, CircleShape),
                    )
                }
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = VibhorSpacing.lg,
            vertical = VibhorSpacing.md,
        ),
    )
}

/**
 * Applies the blur only on platform versions where Compose can honor it
 * (API 31+). Below that the modifier is skipped entirely rather than set to
 * a radius of 0, so no work is scheduled and behavior is identical to a
 * plain backdrop. A radius of 0 always means "disabled" on every version.
 */
private fun Modifier.blurClockBackdrop(radius: Int): Modifier =
    if (radius > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        blur(radius.dp)
    } else {
        this
    }

@Composable
private fun ClockFaceContent(
    state: HomeUiState,
    clockSizeSp: androidx.compose.ui.unit.TextUnit,
    timeModifier: Modifier,
    dateModifier: Modifier,
) {
    when (state.clockFace) {
        ClockFace.CLASSIC -> ClassicClockFace(
            timeText = state.timeText,
            dateText = state.dateText,
            baseSize = clockSizeSp,
            timeModifier = timeModifier,
            dateModifier = dateModifier,
        )
        ClockFace.SPLIT -> SplitClockFace(
            hourText = state.hourText,
            minuteText = state.minuteText,
            meridiemText = state.meridiemText,
            dateText = state.dateText,
            baseSize = clockSizeSp,
            timeModifier = timeModifier,
            dateModifier = dateModifier,
        )
        ClockFace.MINIMAL -> MinimalClockFace(
            timeText = state.timeText,
            baseSize = clockSizeSp,
            timeModifier = timeModifier,
        )
        ClockFace.BOLD -> BoldClockFace(
            timeText = state.timeText,
            dateText = state.dateText,
            baseSize = clockSizeSp,
            timeModifier = timeModifier,
            dateModifier = dateModifier,
        )
        ClockFace.ELEGANT -> ElegantClockFace(
            hourText = state.hourText,
            minuteText = state.minuteText,
            meridiemText = state.meridiemText,
            dateText = state.dateText,
            baseSize = clockSizeSp,
            timeModifier = timeModifier,
            dateModifier = dateModifier,
        )
        ClockFace.ANALOG -> AnalogClockFace(
            hour = state.hour,
            minute = state.minute,
            second = state.second,
            dateText = state.dateText,
            baseSize = clockSizeSp,
            timeModifier = timeModifier,
            dateModifier = dateModifier,
        )
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
            onLaunchApp = {},
            onOpenAppDrawer = {},
            onOpenBlackout = {},
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
            onLaunchApp = {},
            onOpenAppDrawer = {},
            onOpenBlackout = {},
        )
    }
}
