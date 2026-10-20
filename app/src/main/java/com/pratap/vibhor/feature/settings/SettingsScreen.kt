package com.pratap.vibhor.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.AccentColor
import com.pratap.vibhor.core.platform.ClockFace
import com.pratap.vibhor.core.platform.ClockFormat
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings
import com.pratap.vibhor.core.ui.clock.AnalogClockFace
import com.pratap.vibhor.core.ui.clock.BoldClockFace
import com.pratap.vibhor.core.ui.clock.ClassicClockFace
import com.pratap.vibhor.core.ui.clock.ElegantClockFace
import com.pratap.vibhor.core.ui.clock.MinimalClockFace
import com.pratap.vibhor.core.ui.clock.SplitClockFace
import com.pratap.vibhor.core.ui.components.PillSlider
import com.pratap.vibhor.core.ui.theme.DeepSlate
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.core.ui.theme.toColor

object SettingsTestTags {
    const val Page = "page_settings"
    const val ClockSlider = "settings_clock_slider"
    const val ClockFaceCardPrefix = "settings_clock_face_"
    const val ClockFormatOptionPrefix = "settings_clock_format_"
    const val ClockBlurSlider = "settings_clock_blur_slider"
    const val ClockBlurToggle = "settings_clock_blur_toggle"
    const val DarknessSlider = "settings_darkness_slider"
    const val StatusBarToggle = "settings_status_bar_toggle"
    const val QuickButtonSlider = "settings_quick_button_slider"
    const val IconSlider = "settings_icon_slider"
    const val AccentOptionPrefix = "settings_accent_"
    const val BackgroundOptionPrefix = "settings_background_"
    const val BackgroundVisibilitySlider = "settings_background_visibility"
    const val SectionClock = "settings_section_clock"
    const val SectionClockBackdrop = "settings_section_clock_backdrop"
    const val SectionLauncherBackground = "settings_section_launcher_background"
    const val SectionAppearance = "settings_section_appearance"
    const val SectionHomeApps = "settings_section_home_apps"
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
    onClockBlurChange: (Int) -> Unit,
    onBackgroundDarknessChange: (Float) -> Unit,
    onShowStatusBarChange: (Boolean) -> Unit,
    onQuickButtonScaleChange: (Float) -> Unit,
    onAppIconScaleChange: (Float) -> Unit,
    onAccentColorChange: (AccentColor) -> Unit,
    onBackgroundChange: (LauncherBackground) -> Unit,
    onBackgroundOpacityChange: (Float) -> Unit,
    onResetToDefaults: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(SettingsTestTags.Page),
        color = state.background.toColor().copy(alpha = state.backgroundOpacity),
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
                    tint = VibhorThemeColors.accent,
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

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                // Wide car displays get a dashboard-style layout: the clock
                // card spans the width, then the three appearance groups sit
                // side by side, with the sizing controls below. Narrow screens
                // simply stack every section.
                val wide = maxWidth >= 900.dp
                if (wide) {
                    Column(verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xl)) {
                        ClockSettingsSection(
                            state = state,
                            onClockScaleChange = onClockScaleChange,
                            onClockFaceChange = onClockFaceChange,
                            onClockFormatChange = onClockFormatChange,
                            modifier = Modifier.testTag(SettingsTestTags.SectionClock),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                            verticalAlignment = Alignment.Top,
                        ) {
                            ClockBackdropSettingsSection(
                                state = state,
                                onClockBlurChange = onClockBlurChange,
                                onBackgroundDarknessChange = onBackgroundDarknessChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(SettingsTestTags.SectionClockBackdrop),
                            )
                            LauncherBackgroundSettingsSection(
                                state = state,
                                onBackgroundChange = onBackgroundChange,
                                onBackgroundOpacityChange = onBackgroundOpacityChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(SettingsTestTags.SectionLauncherBackground),
                            )
                            AppearanceSettingsSection(
                                state = state,
                                onShowStatusBarChange = onShowStatusBarChange,
                                onAccentColorChange = onAccentColorChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(SettingsTestTags.SectionAppearance),
                            )
                        }
                        HomeAppsSettingsSection(
                            state = state,
                            onQuickButtonScaleChange = onQuickButtonScaleChange,
                            onAppIconScaleChange = onAppIconScaleChange,
                            modifier = Modifier.testTag(SettingsTestTags.SectionHomeApps),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xl)) {
                        ClockSettingsSection(
                            state = state,
                            onClockScaleChange = onClockScaleChange,
                            onClockFaceChange = onClockFaceChange,
                            onClockFormatChange = onClockFormatChange,
                            modifier = Modifier.testTag(SettingsTestTags.SectionClock),
                        )
                        ClockBackdropSettingsSection(
                            state = state,
                            onClockBlurChange = onClockBlurChange,
                            onBackgroundDarknessChange = onBackgroundDarknessChange,
                            modifier = Modifier.testTag(SettingsTestTags.SectionClockBackdrop),
                        )
                        LauncherBackgroundSettingsSection(
                            state = state,
                            onBackgroundChange = onBackgroundChange,
                            onBackgroundOpacityChange = onBackgroundOpacityChange,
                            modifier = Modifier.testTag(SettingsTestTags.SectionLauncherBackground),
                        )
                        AppearanceSettingsSection(
                            state = state,
                            onShowStatusBarChange = onShowStatusBarChange,
                            onAccentColorChange = onAccentColorChange,
                            modifier = Modifier.testTag(SettingsTestTags.SectionAppearance),
                        )
                        HomeAppsSettingsSection(
                            state = state,
                            onQuickButtonScaleChange = onQuickButtonScaleChange,
                            onAppIconScaleChange = onAppIconScaleChange,
                            modifier = Modifier.testTag(SettingsTestTags.SectionHomeApps),
                        )
                    }
                }
            }

            TextButton(
                onClick = onResetToDefaults,
                modifier = Modifier.testTag(SettingsTestTags.Reset),
            ) {
                Text(
                    text = stringResource(R.string.settings_reset),
                    color = VibhorThemeColors.accent,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = PrimaryText.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier.padding(VibhorSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 20.dp)
                        .background(
                            color = VibhorThemeColors.accent,
                            shape = RoundedCornerShape(2.dp),
                        ),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryText,
                )
            }
            content()
        }
    }
}

@Composable
private fun ClockSettingsSection(
    state: SettingsUiState,
    onClockScaleChange: (Float) -> Unit,
    onClockFaceChange: (ClockFace) -> Unit,
    onClockFormatChange: (ClockFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = stringResource(R.string.settings_section_clock),
        modifier = modifier,
    ) {
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
    }
}

@Composable
private fun ClockBackdropSettingsSection(
    state: SettingsUiState,
    onClockBlurChange: (Int) -> Unit,
    onBackgroundDarknessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = stringResource(R.string.settings_section_clock_backdrop),
        modifier = modifier,
    ) {
        ClockBackdropSetting(
            blurRadius = state.clockBlurRadius,
            darkness = state.backgroundDarkness,
            onBlurChange = onClockBlurChange,
            onDarknessChange = onBackgroundDarknessChange,
        )
    }
}

@Composable
private fun LauncherBackgroundSettingsSection(
    state: SettingsUiState,
    onBackgroundChange: (LauncherBackground) -> Unit,
    onBackgroundOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = stringResource(R.string.settings_section_launcher_background),
        modifier = modifier,
    ) {
        LauncherBackgroundSetting(
            selected = state.background,
            onSelect = onBackgroundChange,
            opacity = state.backgroundOpacity,
            onOpacityChange = onBackgroundOpacityChange,
        )
    }
}

@Composable
private fun AppearanceSettingsSection(
    state: SettingsUiState,
    onShowStatusBarChange: (Boolean) -> Unit,
    onAccentColorChange: (AccentColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = stringResource(R.string.settings_section_appearance),
        modifier = modifier,
    ) {
        AccentColorSetting(
            selected = state.accentColor,
            onSelect = onAccentColorChange,
        )
        StatusBarSetting(
            show = state.showStatusBar,
            onToggle = onShowStatusBarChange,
        )
    }
}

@Composable
private fun HomeAppsSettingsSection(
    state: SettingsUiState,
    onQuickButtonScaleChange: (Float) -> Unit,
    onAppIconScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = stringResource(R.string.settings_section_home_apps),
        modifier = modifier,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= 700.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                    verticalAlignment = Alignment.Top,
                ) {
                    ScaleSetting(
                        title = stringResource(R.string.settings_quick_button_size),
                        value = state.quickButtonScale,
                        onValueChange = onQuickButtonScaleChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(SettingsTestTags.QuickButtonSlider),
                    )
                    ScaleSetting(
                        title = stringResource(R.string.settings_app_icon_size),
                        value = state.appIconScale,
                        onValueChange = onAppIconScaleChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(SettingsTestTags.IconSlider),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xl)) {
                    ScaleSetting(
                        title = stringResource(R.string.settings_quick_button_size),
                        value = state.quickButtonScale,
                        onValueChange = onQuickButtonScaleChange,
                        modifier = Modifier.testTag(SettingsTestTags.QuickButtonSlider),
                    )
                    ScaleSetting(
                        title = stringResource(R.string.settings_app_icon_size),
                        value = state.appIconScale,
                        onValueChange = onAppIconScaleChange,
                        modifier = Modifier.testTag(SettingsTestTags.IconSlider),
                    )
                }
            }
        }
    }
}

@Composable
private fun ClockBackdropSetting(
    blurRadius: Int,
    darkness: Float,
    onBlurChange: (Int) -> Unit,
    onDarknessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var blurValue by remember(blurRadius) { mutableFloatStateOf(blurRadius.toFloat()) }
    var darknessValue by remember(darkness) { mutableFloatStateOf(darkness) }
    LaunchedEffect(blurRadius) { blurValue = blurRadius.toFloat() }
    LaunchedEffect(darkness) { darknessValue = darkness }

    val blurEnabled = blurValue > 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_clock_blur),
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryText,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (blurEnabled) {
                        stringResource(R.string.settings_blur_value, blurValue.toInt())
                    } else {
                        stringResource(R.string.settings_value_off)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (blurEnabled) VibhorThemeColors.accent else SecondaryText,
                )
                Switch(
                    checked = blurEnabled,
                    onCheckedChange = { enabled ->
                        val next = if (enabled) LauncherSettings.DEFAULT_BLUR_RADIUS else 0
                        blurValue = next.toFloat()
                        onBlurChange(next)
                    },
                    modifier = Modifier.testTag(SettingsTestTags.ClockBlurToggle),
                )
            }
        }
        PillSlider(
            value = blurValue,
            onValueChange = { blurValue = it },
            onValueChangeFinished = { onBlurChange(blurValue.toInt()) },
            enabled = blurEnabled,
            valueRange = 1f..LauncherSettings.MAX_BLUR_RADIUS.toFloat(),
            steps = LauncherSettings.MAX_BLUR_RADIUS - 2,
            modifier = Modifier.testTag(SettingsTestTags.ClockBlurSlider),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.settings_background_darkness),
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryText,
            )
            Text(
                text = stringResource(
                    R.string.settings_scale_value,
                    (darknessValue * 100).toInt(),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = VibhorThemeColors.accent,
            )
        }
        PillSlider(
            value = darknessValue,
            onValueChange = { darknessValue = it },
            onValueChangeFinished = { onDarknessChange(darknessValue) },
            valueRange = LauncherSettings.MIN_DARKNESS..LauncherSettings.MAX_DARKNESS,
            steps = 4,
            modifier = Modifier.testTag(SettingsTestTags.DarknessSlider),
        )
    }
}

@Composable
private fun StatusBarSetting(
    show: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_status_bar),
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
            )
            Text(
                text = stringResource(R.string.settings_status_bar_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )
        }
        Switch(
            checked = show,
            onCheckedChange = onToggle,
            modifier = Modifier.testTag(SettingsTestTags.StatusBarToggle),
        )
    }
}

@Composable
private fun AccentColorSetting(
    selected: AccentColor,
    onSelect: (AccentColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.settings_accent_color),
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText,
        )
        PaletteGrid(itemCount = AccentColor.entries.size) { index, swatchSize, itemModifier ->
            val accent = AccentColor.entries[index]
            PaletteSwatch(
                color = accent.toColor(),
                label = stringResource(accent.labelRes),
                selected = accent == selected,
                onClick = { onSelect(accent) },
                swatchSize = swatchSize,
                testTag = SettingsTestTags.AccentOptionPrefix + accent.id,
                modifier = itemModifier,
            )
        }
    }
}

@Composable
private fun LauncherBackgroundSetting(
    selected: LauncherBackground,
    onSelect: (LauncherBackground) -> Unit,
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.settings_background_color),
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText,
        )
        PaletteGrid(itemCount = LauncherBackground.entries.size) { index, swatchSize, itemModifier ->
            val background = LauncherBackground.entries[index]
            PaletteSwatch(
                color = background.toColor(),
                label = stringResource(background.labelRes),
                selected = background == selected,
                onClick = { onSelect(background) },
                swatchSize = swatchSize,
                testTag = SettingsTestTags.BackgroundOptionPrefix + background.id,
                modifier = itemModifier,
            )
        }
        BackgroundVisibilitySetting(
            value = opacity,
            onValueChange = onOpacityChange,
            modifier = Modifier.padding(top = VibhorSpacing.sm),
        )
    }
}

@Composable
private fun BackgroundVisibilitySetting(
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_background_visibility),
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
            )
            Text(
                text = if (sliderValue <= 0.001f) {
                    stringResource(R.string.settings_background_transparent)
                } else {
                    stringResource(
                        R.string.settings_scale_value,
                        (sliderValue * 100).toInt(),
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                color = VibhorThemeColors.accent,
            )
        }
        PillSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue) },
            valueRange = LauncherSettings.MIN_BACKGROUND_OPACITY..
                LauncherSettings.MAX_BACKGROUND_OPACITY,
            steps = 19,
            modifier = Modifier.testTag(SettingsTestTags.BackgroundVisibilitySlider),
        )
        Text(
            text = stringResource(R.string.settings_background_visibility_hint),
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
        )
    }
}

@Composable
private fun PaletteGrid(
    itemCount: Int,
    modifier: Modifier = Modifier,
    item: @Composable (index: Int, swatchSize: Dp, modifier: Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 480.dp) 6 else 3
        val swatchSize = (
            (maxWidth - VibhorSpacing.sm * (columns - 1)) / columns -
                VibhorSpacing.xs
            ).coerceIn(28.dp, 48.dp)
        Column(verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm)) {
            (0 until itemCount).chunked(columns).forEach { rowIndices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
                ) {
                    rowIndices.forEach { index ->
                        item(index, swatchSize, Modifier.weight(1f))
                    }
                    repeat(columns - rowIndices.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    swatchSize: Dp,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xs),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color,
            border = BorderStroke(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) {
                    PrimaryText
                } else {
                    SecondaryText.copy(alpha = 0.35f)
                },
            ),
            modifier = Modifier
                .size(swatchSize)
                .testTag(testTag)
                .semantics {
                    this.selected = selected
                    contentDescription = label
                },
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(swatchSize / 4)
                            .background(Midnight, CircleShape),
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) PrimaryText else SecondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

private val AccentColor.labelRes: Int
    get() = when (this) {
        AccentColor.BLUE -> R.string.settings_accent_blue
        AccentColor.TEAL -> R.string.settings_accent_teal
        AccentColor.GREEN -> R.string.settings_accent_green
        AccentColor.PURPLE -> R.string.settings_accent_purple
        AccentColor.ORANGE -> R.string.settings_accent_orange
        AccentColor.PINK -> R.string.settings_accent_pink
    }

private val LauncherBackground.labelRes: Int
    get() = when (this) {
        LauncherBackground.MIDNIGHT -> R.string.settings_background_midnight
        LauncherBackground.SLATE -> R.string.settings_background_slate
        LauncherBackground.GRAPHITE -> R.string.settings_background_graphite
        LauncherBackground.BLACK -> R.string.settings_background_black
        LauncherBackground.NAVY -> R.string.settings_background_navy
        LauncherBackground.OCEAN -> R.string.settings_background_ocean
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
                color = VibhorThemeColors.accent,
            )
        }
        PillSlider(
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
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
                ) {
                    ClockFace.entries.forEach { face ->
                        ClockFaceCard(
                            face = face,
                            selected = face == selected,
                            onClick = { onSelect(face) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
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
                            modifier = Modifier.width(160.dp),
                        )
                    }
                }
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
            color = if (selected) VibhorThemeColors.accent else DeepSlate,
        ),
        modifier = modifier
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
                    ClockFace.BOLD -> BoldClockFace(
                        timeText = "10:42",
                        dateText = "MON 20",
                        baseSize = 32.sp,
                        showDate = false,
                    )
                    ClockFace.ELEGANT -> ElegantClockFace(
                        hourText = "10",
                        minuteText = "42",
                        meridiemText = "",
                        dateText = "",
                        baseSize = 44.sp,
                        showDate = false,
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
                color = if (selected) VibhorThemeColors.accent else SecondaryText,
            )
        }
    }
}

private val ClockFace.labelRes: Int
    get() = when (this) {
        ClockFace.CLASSIC -> R.string.settings_clock_face_classic
        ClockFace.SPLIT -> R.string.settings_clock_face_split
        ClockFace.MINIMAL -> R.string.settings_clock_face_minimal
        ClockFace.BOLD -> R.string.settings_clock_face_bold
        ClockFace.ELEGANT -> R.string.settings_clock_face_elegant
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
        color = if (selected) VibhorThemeColors.accent.copy(alpha = 0.16f) else DeepSlate,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) VibhorThemeColors.accent else DeepSlate,
        ),
        modifier = modifier
            .height(48.dp)
            .testTag(SettingsTestTags.ClockFormatOptionPrefix + format.id),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(format.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) VibhorThemeColors.accent else SecondaryText,
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
            onClockBlurChange = {},
            onBackgroundDarknessChange = {},
            onShowStatusBarChange = {},
            onQuickButtonScaleChange = {},
            onAppIconScaleChange = {},
            onAccentColorChange = {},
            onBackgroundChange = {},
            onBackgroundOpacityChange = {},
            onResetToDefaults = {},
            onClose = {},
        )
    }
}
