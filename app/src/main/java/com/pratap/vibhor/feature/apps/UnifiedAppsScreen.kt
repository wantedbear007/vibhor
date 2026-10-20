package com.pratap.vibhor.feature.apps

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import com.pratap.vibhor.core.ui.theme.toColor
import com.pratap.vibhor.feature.cockpit.CockpitUiState
import kotlin.math.roundToInt

object UnifiedAppsTestTags {
    const val ControlPanel = "apps_control_panel"
    const val Clock = "apps_control_clock"
    const val Date = "apps_control_date"
    const val SettingsButton = "apps_control_settings"
    const val VolumeSlider = "apps_volume_slider"
    const val BrightnessSlider = "apps_brightness_slider"
    const val BluetoothTile = "apps_quick_bluetooth"
    const val WifiTile = "apps_quick_wifi"
    const val NightTile = "apps_quick_night"
    const val SettingsTile = "apps_quick_settings"
    const val Divider = "apps_panel_divider"
    const val NightOverlay = "apps_night_overlay"
}

/**
 * The left-swipe screen: a fixed quick-control rail (clock, settings, sound,
 * brightness, connectivity tiles) beside the existing app launcher. The Home
 * page remains untouched and minimal.
 */
@Composable
fun UnifiedAppsScreen(
    appState: AppDrawerUiState,
    controlState: CockpitUiState,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onToggleWifi: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onToggleNightMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onTabSelected: (AppDrawerTab) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(AppDrawerTestTags.Page),
        color = controlState.background.toColor().copy(alpha = controlState.backgroundOpacity),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val landscape = maxWidth >= maxHeight
            val controlWeight = if (landscape) 0.30f else 0.38f

            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(
                            start = VibhorSpacing.xl,
                            end = VibhorSpacing.lg,
                            top = VibhorSpacing.lg,
                            bottom = VibhorSpacing.lg,
                        ),
                ) {
                    QuickControlPanel(
                        state = controlState,
                        onVolumeChange = onVolumeChange,
                        onBrightnessChange = onBrightnessChange,
                        onToggleWifi = onToggleWifi,
                        onToggleBluetooth = onToggleBluetooth,
                        onToggleNightMode = onToggleNightMode,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier
                            .weight(controlWeight)
                            .fillMaxHeight()
                            .padding(end = 20.dp),
                    )
                    PanelDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .testTag(UnifiedAppsTestTags.Divider),
                    )
                    AppDrawerContent(
                        state = appState,
                        onAppClick = onAppClick,
                        onTabSelected = onTabSelected,
                        onAppLongPress = onAppLongPress,
                        onSearchQueryChange = onSearchQueryChange,
                        headerTopPadding = 0.dp,
                        horizontalPadding = 20.dp,
                        modifier = Modifier
                            .weight(1f - controlWeight)
                            .fillMaxHeight(),
                    )
                }
                NightModeOverlay(enabled = controlState.nightMode)
            }
        }
    }
}

@Composable
private fun QuickControlPanel(
    state: CockpitUiState,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onToggleWifi: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onToggleNightMode: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.testTag(UnifiedAppsTestTags.ControlPanel),
    ) {
        val narrow = maxWidth < 260.dp
        val clockSize = (maxWidth.value * if (narrow) 0.13f else 0.155f)
            .coerceIn(if (narrow) 26f else 34f, if (narrow) 44f else 64f)
            .sp

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.timeText,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = clockSize,
                        lineHeight = clockSize * 1.04f,
                        letterSpacing = (-1.5).sp,
                        color = PrimaryText,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(UnifiedAppsTestTags.Clock),
                    )
                    Text(
                        text = state.dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag(UnifiedAppsTestTags.Date),
                    )
                }
                SettingsIconButton(
                    onClick = onOpenSettings,
                    buttonSize = if (narrow) 40.dp else 48.dp,
                    iconSize = if (narrow) 18.dp else 20.dp,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val spacing = 20.dp
                val sliderWidth = ((maxWidth - spacing) / 2).coerceAtMost(118.dp)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(
                        spacing,
                        Alignment.CenterHorizontally,
                    ),
                ) {
                    VerticalPillSlider(
                        value = state.volumeLevel,
                        onValueChange = onVolumeChange,
                        iconRes = R.drawable.ic_volume_up,
                        label = stringResource(R.string.apps_volume),
                        fillColors = listOf(
                            VibhorThemeColors.accent,
                            VibhorThemeColors.accent.copy(alpha = 0.58f),
                        ),
                        testTag = UnifiedAppsTestTags.VolumeSlider,
                        modifier = Modifier
                            .width(sliderWidth)
                            .fillMaxHeight(),
                    )
                    VerticalPillSlider(
                        value = state.brightnessLevel,
                        onValueChange = onBrightnessChange,
                        iconRes = R.drawable.ic_brightness,
                        label = stringResource(R.string.apps_brightness),
                        fillColors = listOf(
                            VibhorThemeColors.accent,
                            VibhorThemeColors.accent.copy(alpha = 0.58f),
                        ),
                        testTag = UnifiedAppsTestTags.BrightnessSlider,
                        modifier = Modifier
                            .width(sliderWidth)
                            .fillMaxHeight(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            QuickControlGrid(
                state = state,
                onToggleWifi = onToggleWifi,
                onToggleBluetooth = onToggleBluetooth,
                onToggleNightMode = onToggleNightMode,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}

@Composable
private fun SettingsIconButton(
    onClick: () -> Unit,
    buttonSize: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(buttonSize)
            .testTag(UnifiedAppsTestTags.SettingsButton),
        shape = RoundedCornerShape(14.dp),
        color = PrimaryText.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.08f)),
        contentColor = PrimaryText,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_gear),
                contentDescription = stringResource(R.string.settings_title),
                tint = SecondaryText,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
private fun VerticalPillSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    @DrawableRes iconRes: Int,
    label: String,
    fillColors: List<Color>,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val clamped = value.coerceIn(0f, 1f)
    var dragValue by remember { mutableFloatStateOf(clamped) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(clamped) {
        if (!dragging) dragValue = clamped
    }

    val fillFraction by animateFloatAsState(
        targetValue = dragValue.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 120),
        label = "pill-fill",
    )
    val percent = (dragValue.coerceIn(0f, 1f) * 100f).roundToInt()
    val trackShape = RoundedCornerShape(999.dp)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(trackShape)
                    .background(Color.Black.copy(alpha = 0.28f))
                    .border(1.dp, PrimaryText.copy(alpha = 0.08f), trackShape)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            fun updateValue(y: Float) {
                                val next = (1f - (y / size.height).coerceIn(0f, 1f))
                                    .coerceIn(0f, 1f)
                                dragValue = next
                                onValueChange(next)
                            }
                            dragging = true
                            updateValue(down.position.y)
                            down.consume()
                            try {
                                drag(down.id) { change ->
                                    updateValue(change.position.y)
                                    change.consume()
                                }
                            } finally {
                                dragging = false
                            }
                        }
                    }
                    .semantics {
                        contentDescription = label
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = dragValue.coerceIn(0f, 1f),
                            range = 0f..1f,
                        )
                        setProgress { target ->
                            val next = target.coerceIn(0f, 1f)
                            dragValue = next
                            onValueChange(next)
                            true
                        }
                    }
                    .testTag(testTag),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val fillHeight = size.height * fillFraction
                    if (fillHeight > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = fillColors,
                                startY = size.height - fillHeight,
                                endY = size.height,
                            ),
                            topLeft = Offset(0f, size.height - fillHeight),
                            size = Size(size.width, fillHeight),
                            cornerRadius = CornerRadius(size.width / 2f, size.width / 2f),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .size(38.dp)
                        .background(Midnight.copy(alpha = 0.42f), CircleShape)
                        .border(1.dp, PrimaryText.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = PrimaryText,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    shape = CircleShape,
                    color = Midnight.copy(alpha = 0.62f),
                    border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.10f)),
                ) {
                    Text(
                        text = stringResource(R.string.apps_slider_value, percent),
                        color = PrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun QuickControlGrid(
    state: CockpitUiState,
    onToggleWifi: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onToggleNightMode: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val on = stringResource(R.string.apps_quick_on)
    val off = stringResource(R.string.apps_quick_off)
    val unavailable = stringResource(R.string.apps_quick_unavailable)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickControlTile(
                iconRes = R.drawable.ic_bluetooth,
                label = stringResource(R.string.apps_quick_bluetooth),
                status = when {
                    !state.bluetoothSupported -> unavailable
                    state.bluetoothEnabled -> on
                    else -> off
                },
                active = state.bluetoothEnabled,
                enabled = state.bluetoothSupported,
                highlight = VibhorThemeColors.accent,
                onClick = onToggleBluetooth,
                testTag = UnifiedAppsTestTags.BluetoothTile,
                modifier = Modifier.weight(1f),
            )
            QuickControlTile(
                iconRes = R.drawable.ic_wifi,
                label = stringResource(R.string.apps_quick_wifi),
                status = when {
                    !state.wifiSupported -> unavailable
                    state.wifiEnabled -> on
                    else -> off
                },
                active = state.wifiEnabled,
                enabled = state.wifiSupported,
                highlight = VibhorThemeColors.accent,
                onClick = onToggleWifi,
                testTag = UnifiedAppsTestTags.WifiTile,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickControlTile(
                iconRes = R.drawable.ic_night_mode,
                label = stringResource(R.string.apps_quick_night),
                status = if (state.nightMode) on else off,
                active = state.nightMode,
                enabled = true,
                highlight = VibhorThemeColors.accent,
                onClick = onToggleNightMode,
                testTag = UnifiedAppsTestTags.NightTile,
                modifier = Modifier.weight(1f),
            )
            QuickControlTile(
                iconRes = R.drawable.ic_gear,
                label = stringResource(R.string.apps_quick_settings),
                status = stringResource(R.string.apps_quick_open),
                active = false,
                enabled = true,
                highlight = VibhorThemeColors.accent,
                onClick = onOpenSettings,
                testTag = UnifiedAppsTestTags.SettingsTile,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickControlTile(
    @DrawableRes iconRes: Int,
    label: String,
    status: String,
    active: Boolean,
    enabled: Boolean,
    highlight: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val accentOn = VibhorThemeColors.accentOn
    val containerColor by animateColorAsState(
        targetValue = when {
            active -> highlight
            enabled -> highlight.copy(alpha = 0.07f)
            else -> PrimaryText.copy(alpha = 0.035f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "quick-tile-container",
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            active -> highlight
            enabled -> highlight.copy(alpha = 0.22f)
            else -> PrimaryText.copy(alpha = 0.06f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "quick-tile-border",
    )
    val contentColor = when {
        !enabled -> SecondaryText.copy(alpha = 0.55f)
        active -> accentOn
        else -> highlight
    }
    val labelColor = if (active) accentOn else PrimaryText
    val statusColor = if (active) accentOn.copy(alpha = 0.72f) else contentColor
    val iconBackground = when {
        active -> accentOn.copy(alpha = 0.16f)
        enabled -> highlight.copy(alpha = 0.12f)
        else -> PrimaryText.copy(alpha = 0.05f)
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(74.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .testTag(testTag),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        contentColor = contentColor,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val stacked = maxWidth < 112.dp
            val compact = maxWidth < 150.dp
            if (stacked) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = iconBackground,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 11.sp,
                        color = labelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (compact) 10.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 26.dp else 32.dp)
                            .background(
                                color = iconBackground,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(if (compact) 15.dp else 18.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = if (compact) 13.sp else 15.sp,
                            color = labelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = if (compact) 10.sp else 11.sp,
                            color = statusColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    VibhorThemeColors.accent.copy(alpha = 0.30f),
                    PrimaryText.copy(alpha = 0.10f),
                    Color.Transparent,
                ),
            ),
        ),
    )
}

@Composable
private fun NightModeOverlay(enabled: Boolean) {
    val dim by animateFloatAsState(
        targetValue = if (enabled) 0.26f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "night-dimming",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = dim))
            .testTag(UnifiedAppsTestTags.NightOverlay),
    )
}

@Preview(name = "Unified apps landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun UnifiedAppsLandscapePreview() {
    VibhorTheme {
        UnifiedAppsScreen(
            appState = AppDrawerUiState(
                apps = previewApps,
                favouriteApps = previewApps.take(1),
                favouritePackageNames = setOf(previewApps.first().packageName),
                selectedTab = AppDrawerTab.All,
                isLoading = false,
                displayedApps = previewApps,
            ),
            controlState = CockpitUiState(
                timeText = "10:42",
                dateText = "Monday, 19 October",
                volumeLevel = 0.62f,
                brightnessLevel = 0.48f,
                wifiSupported = true,
                wifiEnabled = true,
                bluetoothSupported = true,
                bluetoothEnabled = true,
                nightMode = false,
                background = LauncherBackground.MIDNIGHT,
                backgroundOpacity = 1f,
            ),
            onVolumeChange = {},
            onBrightnessChange = {},
            onToggleWifi = {},
            onToggleBluetooth = {},
            onToggleNightMode = {},
            onOpenSettings = {},
            onAppClick = {},
            onTabSelected = {},
            onAppLongPress = {},
            onSearchQueryChange = {},
        )
    }
}

private val previewApps = listOf(
    InstalledApp("com.example.maps", "com.example.maps.Main", "Maps"),
    InstalledApp("com.example.music", "com.example.music.Main", "Music"),
    InstalledApp("com.example.radio", "com.example.radio.Main", "Radio"),
    InstalledApp("com.example.dialer", "com.example.dialer.Main", "Phone"),
    InstalledApp("com.example.podcasts", "com.example.podcasts.Main", "Podcasts"),
    InstalledApp("com.example.weather", "com.example.weather.Main", "Weather"),
)
