package com.pratap.vibhor.feature.music

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.platform.media.NowPlaying
import com.pratap.vibhor.core.time.formatPlaybackTime
import com.pratap.vibhor.core.ui.components.VerticalPillSlider
import com.pratap.vibhor.core.ui.theme.DeepSlate
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.SurfaceElevated
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import com.pratap.vibhor.core.ui.theme.toColor
import com.pratap.vibhor.feature.cockpit.CockpitUiState
import kotlin.math.sin

object MusicTestTags {
    const val Page = "page_music"
    const val Astronaut = "music_astronaut"
    const val Card = "music_card"
    const val AlbumArt = "music_album_art"
    const val TrackTitle = "music_track_title"
    const val TrackArtist = "music_track_artist"
    const val Progress = "music_progress"
    const val ProgressBar = "music_progress_bar"
    const val Equalizer = "music_equalizer"
    const val PlayPause = "music_play_pause"
    const val Next = "music_next"
    const val Previous = "music_previous"
    const val VolumeSlider = "music_volume_slider"
    const val BrightnessSlider = "music_brightness_slider"
    const val Favourites = "music_favourites"
    const val Idle = "music_idle"
    const val AccessAction = "music_access_action"
}

/**
 * Music page: the astronaut stage above the real media session, the device
 * volume/brightness pills on the right, and a compact favourites strip along
 * the bottom. The layout keeps that hierarchy in landscape and stacks the
 * stage, player, and pills cleanly in portrait.
 */
@Composable
fun MusicScreen(
    musicState: MusicUiState,
    controlState: CockpitUiState,
    favourites: List<InstalledApp>,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onEnableMediaAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = musicState.track
    val playing = track?.isPlaying == true

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(MusicTestTags.Page),
        color = controlState.background.toColor().copy(alpha = controlState.backgroundOpacity),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = VibhorSpacing.xl, vertical = VibhorSpacing.lg),
        ) {
            val landscape = maxWidth > maxHeight
            val sliderColumnWidth = (maxWidth * 0.20f).coerceIn(180.dp, 264.dp)
            val card: @Composable (Modifier, Boolean) -> Unit = { cardModifier, fillHeight ->
                MusicCard(
                    track = track,
                    hasAccess = musicState.hasAccess,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onEnableMediaAccess = onEnableMediaAccess,
                    fillHeight = fillHeight,
                    modifier = cardModifier,
                )
            }

            if (landscape) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(VibhorSpacing.lg),
                        ) {
                            AstronautScene(
                                playing = playing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            )
                            card(
                                Modifier.fillMaxWidth(),
                                false,
                            )
                        }
                        SliderPair(
                            state = controlState,
                            onVolumeChange = onVolumeChange,
                            onBrightnessChange = onBrightnessChange,
                            modifier = Modifier
                                .width(sliderColumnWidth)
                                .fillMaxHeight(),
                        )
                    }
                    Spacer(modifier = Modifier.height(VibhorSpacing.lg))
                    FavouritesStrip(
                        favourites = favourites,
                        onLaunchApp = onLaunchApp,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(VibhorSpacing.lg),
                ) {
                    AstronautScene(
                        playing = playing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp),
                    )
                    card(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        true,
                    )
                    SliderPair(
                        state = controlState,
                        onVolumeChange = onVolumeChange,
                        onBrightnessChange = onBrightnessChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(228.dp),
                    )
                    FavouritesStrip(
                        favourites = favourites,
                        onLaunchApp = onLaunchApp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicCard(
    track: NowPlaying?,
    hasAccess: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onEnableMediaAccess: () -> Unit,
    fillHeight: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag(MusicTestTags.Card),
        shape = RoundedCornerShape(28.dp),
        color = DeepSlate.copy(alpha = 0.74f),
        border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier
                .then(if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                .padding(horizontal = 26.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (track == null) {
                IdlePlayer(
                    hasAccess = hasAccess,
                    onEnableMediaAccess = onEnableMediaAccess,
                )
            } else {
                TrackPlayer(
                    track = track,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                )
            }
        }
    }
}

@Composable
private fun TrackPlayer(
    track: NowPlaying,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlbumArtwork(
                art = track.albumArt,
                modifier = Modifier.size(112.dp),
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title.ifBlank { stringResource(R.string.music_unknown_title) },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(MusicTestTags.TrackTitle),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artist ?: stringResource(R.string.music_unknown_artist),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(MusicTestTags.TrackArtist),
                )
                Spacer(modifier = Modifier.height(14.dp))
                EqualizerBars(
                    playing = track.isPlaying,
                    modifier = Modifier
                        .width(58.dp)
                        .height(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        PlaybackProgress(
            positionMs = track.positionMs,
            durationMs = track.durationMs,
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransportButton(
                iconRes = R.drawable.ic_skip_previous,
                contentDescription = stringResource(R.string.music_previous),
                enabled = track.canSkipPrevious,
                onClick = onPrevious,
                testTag = MusicTestTags.Previous,
            )
            Spacer(modifier = Modifier.width(22.dp))
            PlayPauseButton(
                playing = track.isPlaying,
                enabled = track.canPlayPause,
                onClick = onPlayPause,
            )
            Spacer(modifier = Modifier.width(22.dp))
            TransportButton(
                iconRes = R.drawable.ic_skip_next,
                contentDescription = stringResource(R.string.music_next),
                enabled = track.canSkipNext,
                onClick = onNext,
                testTag = MusicTestTags.Next,
            )
        }
    }
}

@Composable
private fun IdlePlayer(
    hasAccess: Boolean,
    onEnableMediaAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlbumArtwork(
            art = null,
            modifier = Modifier.size(92.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.music_idle_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
            modifier = Modifier.testTag(MusicTestTags.Idle),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(
                if (hasAccess) R.string.music_idle_body else R.string.music_access_body,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center,
        )
        if (!hasAccess) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onEnableMediaAccess,
                modifier = Modifier.testTag(MusicTestTags.AccessAction),
            ) {
                Text(text = stringResource(R.string.music_access_action))
            }
        }
    }
}

@Composable
private fun AlbumArtwork(
    art: Bitmap?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    if (art != null) {
        Image(
            bitmap = art.asImageBitmap(),
            contentDescription = stringResource(R.string.music_album_art),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(shape)
                .testTag(MusicTestTags.AlbumArt),
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(SurfaceElevated, DeepSlate),
                    ),
                )
                .testTag(MusicTestTags.AlbumArt),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_music_note),
                contentDescription = null,
                tint = SecondaryText.copy(alpha = 0.8f),
                modifier = Modifier.size(38.dp),
            )
        }
    }
}

@Composable
private fun PlaybackProgress(
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    val target = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val fraction by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 1_000, easing = LinearEasing),
        label = "playback-progress",
    )
    val accent = VibhorThemeColors.accent
    val trackShape = RoundedCornerShape(999.dp)

    Column(modifier = modifier.testTag(MusicTestTags.Progress)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(trackShape)
                .background(PrimaryText.copy(alpha = 0.12f))
                .testTag(MusicTestTags.ProgressBar),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(trackShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accent, accent.copy(alpha = 0.62f)),
                        ),
                    ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatPlaybackTime(positionMs),
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText,
            )
            if (durationMs > 0L) {
                Text(
                    text = formatPlaybackTime(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun PlayPauseButton(
    playing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = VibhorThemeColors.accent
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(62.dp)
            .testTag(MusicTestTags.PlayPause),
        shape = CircleShape,
        color = if (enabled) accent else SurfaceElevated,
        contentColor = if (enabled) VibhorThemeColors.accentOn else SecondaryText,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    if (playing) R.drawable.ic_pause else R.drawable.ic_media,
                ),
                contentDescription = stringResource(
                    if (playing) R.string.music_pause else R.string.music_play,
                ),
                tint = if (enabled) VibhorThemeColors.accentOn else SecondaryText,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun TransportButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(52.dp)
            .testTag(testTag),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (enabled) PrimaryText else SecondaryText.copy(alpha = 0.35f),
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun EqualizerBars(
    playing: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_300, easing = LinearEasing),
        ),
        label = "sweep",
    )
    val accent = VibhorThemeColors.accent

    Canvas(modifier = modifier.testTag(MusicTestTags.Equalizer)) {
        val count = 5
        val gap = size.width * 0.10f / (count - 1)
        val barWidth = (size.width - gap * (count - 1)) / count
        val resting = floatArrayOf(0.24f, 0.44f, 0.30f, 0.52f, 0.28f)

        for (index in 0 until count) {
            val level = if (playing) {
                0.22f + 0.78f * ((sin(sweep * EQ_TWO_PI * 2f + index * 0.9f) + 1f) / 2f)
            } else {
                resting[index]
            }
            val barHeight = size.height * level
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(accent, accent.copy(alpha = 0.45f)),
                    startY = size.height - barHeight,
                    endY = size.height,
                ),
                topLeft = Offset(index * (barWidth + gap), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f),
            )
        }
    }
}

@Composable
private fun SliderPair(
    state: CockpitUiState,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = VibhorThemeColors.accent
    BoxWithConstraints(modifier = modifier) {
        val gap = 20.dp
        val sliderWidth = ((maxWidth - gap) / 2).coerceAtMost(132.dp)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VerticalPillSlider(
                value = state.volumeLevel,
                onValueChange = onVolumeChange,
                iconRes = R.drawable.ic_volume_up,
                label = stringResource(R.string.apps_volume),
                fillColors = listOf(accent, accent.copy(alpha = 0.58f)),
                testTag = MusicTestTags.VolumeSlider,
                modifier = Modifier
                    .width(sliderWidth)
                    .fillMaxHeight(),
            )
            VerticalPillSlider(
                value = state.brightnessLevel,
                onValueChange = onBrightnessChange,
                iconRes = R.drawable.ic_brightness,
                label = stringResource(R.string.apps_brightness),
                fillColors = listOf(accent, accent.copy(alpha = 0.58f)),
                testTag = MusicTestTags.BrightnessSlider,
                modifier = Modifier
                    .width(sliderWidth)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun FavouritesStrip(
    favourites: List<InstalledApp>,
    onLaunchApp: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MusicTestTags.Favourites),
        shape = RoundedCornerShape(20.dp),
        color = PrimaryText.copy(alpha = 0.045f),
        border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.07f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.music_favourites),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = SecondaryText,
            )
            Spacer(modifier = Modifier.width(16.dp))
            if (favourites.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_quick_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    favourites.forEach { app ->
                        FavouriteChip(
                            app = app,
                            onClick = { onLaunchApp(app) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouriteChip(
    app: InstalledApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = PrimaryText.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, PrimaryText.copy(alpha = 0.06f)),
        contentColor = PrimaryText,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = app.icon
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryText.copy(alpha = 0.12f)),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp),
            )
        }
    }
}

private val EQ_TWO_PI = (2.0 * Math.PI).toFloat()

@Preview(name = "Music landscape", widthDp = 1024, heightDp = 600)
@Composable
private fun MusicLandscapePreview() {
    VibhorTheme {
        MusicScreen(
            musicState = MusicUiState(
                hasAccess = true,
                track = PreviewTrack,
            ),
            controlState = CockpitUiState(),
            favourites = emptyList(),
            onPlayPause = {},
            onNext = {},
            onPrevious = {},
            onVolumeChange = {},
            onBrightnessChange = {},
            onLaunchApp = {},
            onEnableMediaAccess = {},
        )
    }
}

@Preview(name = "Music idle portrait", widthDp = 412, heightDp = 915)
@Composable
private fun MusicIdlePreview() {
    VibhorTheme {
        MusicScreen(
            musicState = MusicUiState(hasAccess = false),
            controlState = CockpitUiState(),
            favourites = emptyList(),
            onPlayPause = {},
            onNext = {},
            onPrevious = {},
            onVolumeChange = {},
            onBrightnessChange = {},
            onLaunchApp = {},
            onEnableMediaAccess = {},
        )
    }
}

private val PreviewTrack = NowPlaying(
    title = "Midnight City",
    artist = "M83",
    albumArt = null,
    isPlaying = true,
    positionMs = 74_000L,
    durationMs = 244_000L,
    canPlayPause = true,
    canSkipNext = true,
    canSkipPrevious = true,
)
