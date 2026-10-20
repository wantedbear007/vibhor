package com.pratap.vibhor.feature.cockpit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme

object CockpitTestTags {
    const val Page = "page_cockpit"
    const val VolumeSlider = "cockpit_volume_slider"
    const val BrightnessSlider = "cockpit_brightness_slider"
    const val RailClock = "cockpit_rail_clock"
    const val RailDate = "cockpit_rail_date"
    const val SettingsButton = "cockpit_settings_button"
}

@Composable
fun CockpitScreen(
    state: CockpitUiState,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(CockpitTestTags.Page),
        color = Midnight,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(VibhorSpacing.xl),
        ) {
            if (maxWidth > maxHeight) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CockpitSideRail(
                        state = state,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier
                            .width(148.dp)
                            .fillMaxHeight(),
                    )
                    ControlCluster(
                        volume = state.volumeLevel,
                        brightness = state.brightnessLevel,
                        onVolumeChange = onVolumeChange,
                        onBrightnessChange = onBrightnessChange,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                ) {
                    CockpitSideRail(
                        state = state,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = VibhorSpacing.sm),
                    )
                    ControlCluster(
                        volume = state.volumeLevel,
                        brightness = state.brightnessLevel,
                        onVolumeChange = onVolumeChange,
                        onBrightnessChange = onBrightnessChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun CockpitSideRail(
    state: CockpitUiState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = state.timeText,
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryText,
            modifier = Modifier.testTag(CockpitTestTags.RailClock),
        )
        Text(
            text = state.dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            modifier = Modifier.testTag(CockpitTestTags.RailDate),
        )
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .padding(top = VibhorSpacing.md)
                .testTag(CockpitTestTags.SettingsButton),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings_title),
                tint = SecondaryText,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun ControlCluster(
    volume: Float,
    brightness: Float,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoundControl(
            value = volume,
            onValueChange = onVolumeChange,
        )
        BrightnessControl(
            value = brightness,
            onValueChange = onBrightnessChange,
        )
    }
}

@Composable
private fun SoundControl(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalControlSlider(
        value = value,
        onValueChange = onValueChange,
        iconRes = R.drawable.ic_volume_up,
        modifier = modifier,
        testTag = CockpitTestTags.VolumeSlider,
    )
}

@Composable
private fun BrightnessControl(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalControlSlider(
        value = value,
        onValueChange = onValueChange,
        iconRes = R.drawable.ic_brightness,
        modifier = modifier,
        testTag = CockpitTestTags.BrightnessSlider,
    )
}

@Composable
private fun VerticalControlSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    @androidx.annotation.DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
    val clamped = value.coerceIn(0f, 1f)
    val animated by animateFloatAsState(targetValue = clamped, label = "slider")
    var dragValue by remember { mutableFloatStateOf(clamped) }
    LaunchedEffect(clamped) { if (!dragValue.isNaN()) dragValue = clamped }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = PrimaryText,
            modifier = Modifier.size(28.dp),
        )
        Box(
            modifier = Modifier
                .width(104.dp)
                .height(480.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(PrimaryText.copy(alpha = 0.12f))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Map vertical position to value (top = 1, bottom = 0)
                            val v = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                            dragValue = v
                            onValueChange(v)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val v = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                            dragValue = v
                            onValueChange(v)
                        },
                    )
                }
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val fillFraction = animateFloatAsState(targetValue = dragValue.coerceIn(0f, 1f), label = "fill")
            val fillColor = VibhorThemeColors.accent
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val trackHeight = size.height
                val trackWidth = size.width
                val fillHeight = trackHeight * fillFraction.value
                drawRoundRect(
                    color = fillColor.copy(alpha = 0.85f),
                    topLeft = Offset(0f, trackHeight - fillHeight),
                    size = Size(trackWidth, fillHeight),
                    cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f),
                )
            }
            // Subtle top highlight for iOS look
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PrimaryText.copy(alpha = 0.18f)),
            )
        }
    }
}

@Preview(name = "Cockpit landscape", widthDp = 1024, heightDp = 600)
@Composable
private fun CockpitLandscapePreview() {
    VibhorTheme {
        CockpitScreen(
            state = CockpitUiState(
                timeText = "10:42",
                dateText = "Monday, 19 October",
            ),
            onVolumeChange = {},
            onBrightnessChange = {},
            onOpenSettings = {},
        )
    }
}

@Preview(name = "Cockpit empty", widthDp = 412, heightDp = 915)
@Composable
private fun CockpitPortraitPreview() {
    VibhorTheme {
        CockpitScreen(
            state = CockpitUiState(
                timeText = "10:42",
                dateText = "Monday, 19 October",
            ),
            onVolumeChange = {},
            onBrightnessChange = {},
            onOpenSettings = {},
        )
    }
}
