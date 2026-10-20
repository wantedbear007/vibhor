package com.pratap.vibhor.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.LauncherSettings
import com.pratap.vibhor.core.ui.clock.AnalogClockFace
import com.pratap.vibhor.core.ui.clock.ClassicClockFace
import com.pratap.vibhor.core.ui.clock.MinimalClockFace
import com.pratap.vibhor.core.ui.clock.SplitClockFace
import com.pratap.vibhor.core.ui.theme.DeepSlate
import com.pratap.vibhor.core.ui.theme.ElectricBlue
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme

object SettingsTestTags {
    const val Page = "page_settings"
    const val ClockSlider = "settings_clock_slider"
    const val ClockFaceCardPrefix = "settings_clock_face_"
    const val ClockFormatOptionPrefix = "settings_clock_format_"
    const val IconSlider = "settings_icon_slider"
    const val Close = "settings_close"
    const val Reset = "settings_reset"
}

/**
 * Launcher settings. Every user-tunable launcher behavior is exposed here;
 * each setting persists through the SettingsStore and applies immediately
 * to the affected screen.
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onClockScaleChange: (Float) -> Unit,
    onClockFaceChange: (ClockFace) -> Unit,
    onClockFormatChange: (ClockFormat) -> Unit,
    onAppIconScaleChange: (Float) -> Unit,
    onResetToDefaults: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(SettingsTestTags.Page),
        color = Midnight,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(VibhorSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xxl),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryText,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag(SettingsTestTags.Close),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.settings_close),
                        tint = PrimaryText,
                    )
                }
            }

            ScaleSetting(
                title = stringResource(R.string.settings_clock_size),
                value = state.clockScale,
                onValueChange = onClockScaleChange,
                modifier = Modifier.testTag(SettingsTestTags.ClockSlider),
            )

            ClockFaceSetting(
                selected = state.clockFace,
                onSelect = onClockFaceChange,
            )

            ClockFormatSetting(
                selected = state.clockFormat,
                onSelect = onClockFormatChange,
            )

            ScaleSetting(
                title = stringResource(R.string.settings_app_icon_size),
                value = state.appIconScale,
                onValueChange = onAppIconScaleChange,
                modifier = Modifier.testTag(SettingsTestTags.IconSlider),
            )

            TextButton(
                onClick = onResetToDefaults,
                modifier = Modifier.testTag(SettingsTestTags.Reset),
            ) {
                Text(
                    text = stringResource(R.string.settings_reset),
                    color = ElectricBlue,
                )
            }
        }
    }
}

@Composable
private fun ScaleSetting(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }
    LaunchedEffect(value) { sliderValue = value }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
            )
            Text(
                text = stringResource(
                    R.string.settings_scale_value,
                    (sliderValue * 100).toInt(),
                ),
                style = MaterialTheme.typography.titleMedium,
                color = ElectricBlue,
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue) },
            valueRange = LauncherSettings.MIN_SCALE..LauncherSettings.MAX_SCALE,
            steps = 5,
        )
    }
}

@Composable
private fun ClockFaceSetting(
    selected: ClockFace,
    onSelect: (ClockFace) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.settings_clock_face),
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
        ) {
            ClockFace.entries.forEach { face ->
                ClockFaceCard(
                    face = face,
                    selected = face == selected,
                    onClick = { onSelect(face) },
                )
            }
        }
    }
}

@Composable
private fun ClockFaceCard(
    face: ClockFace,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = DeepSlate,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) ElectricBlue else DeepSlate,
        ),
        modifier = modifier
            .width(160.dp)
            .height(110.dp)
            .testTag(SettingsTestTags.ClockFaceCardPrefix + face.id),
    ) {
        Column(
            modifier = Modifier.padding(VibhorSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when (face) {
                    ClockFace.CLASSIC -> ClassicClockFace(
                        timeText = "10:42",
                        dateText = "",
                        baseSize = 32.sp,
                        showDate = false,
                    )
                    ClockFace.SPLIT -> SplitClockFace(
                        hourText = "10",
                        minuteText = "42",
                        meridiemText = "AM",
                        dateText = "",
                        baseSize = 48.sp,
                        showDate = false,
                    )
                    ClockFace.MINIMAL -> MinimalClockFace(
                        timeText = "10:42",
                        baseSize = 44.sp,
                    )
                    ClockFace.ANALOG -> AnalogClockFace(
                        hour = 10,
                        minute = 42,
                        second = 0,
                        dateText = "",
                        baseSize = 40.sp,
                        showDate = false,
                    )
                }
            }
            Text(
                text = stringResource(face.labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) ElectricBlue else SecondaryText,
            )
        }
    }
}

private val ClockFace.labelRes: Int
    get() = when (this) {
        ClockFace.CLASSIC -> R.string.settings_clock_face_classic
        ClockFace.SPLIT -> R.string.settings_clock_face_split
        ClockFace.MINIMAL -> R.string.settings_clock_face_minimal
        ClockFace.ANALOG -> R.string.settings_clock_face_analog
    }

@Composable
private fun ClockFormatSetting(
    selected: ClockFormat,
    onSelect: (ClockFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.settings_clock_format),
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
        ) {
            ClockFormat.entries.forEach { format ->
                FormatOptionChip(
                    format = format,
                    selected = format == selected,
                    onClick = { onSelect(format) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FormatOptionChip(
    format: ClockFormat,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (selected) ElectricBlue.copy(alpha = 0.16f) else DeepSlate,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) ElectricBlue else DeepSlate,
        ),
        modifier = modifier
            .height(48.dp)
            .testTag(SettingsTestTags.ClockFormatOptionPrefix + format.id),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(format.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) ElectricBlue else SecondaryText,
            )
        }
    }
}

private val ClockFormat.labelRes: Int
    get() = when (this) {
        ClockFormat.SYSTEM -> R.string.settings_clock_format_system
        ClockFormat.H12 -> R.string.settings_clock_format_12
        ClockFormat.H24 -> R.string.settings_clock_format_24
    }

@Preview(name = "Settings landscape", widthDp = 1024, heightDp = 600)
@Composable
private fun SettingsLandscapePreview() {
    VibhorTheme {
        SettingsScreen(
            state = SettingsUiState(
                clockScale = 1.25f,
                appIconScale = 0.75f,
                clockFace = ClockFace.SPLIT,
            ),
            onClockScaleChange = {},
            onClockFaceChange = {},
            onClockFormatChange = {},
            onAppIconScaleChange = {},
            onResetToDefaults = {},
            onClose = {},
        )
    }
}
