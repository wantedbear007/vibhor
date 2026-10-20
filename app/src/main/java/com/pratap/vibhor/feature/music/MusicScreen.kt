package com.pratap.vibhor.feature.music

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sin

object MusicTestTags {
    const val Page = "page_music"
    const val Astronaut = "music_astronaut"
    const val Card = "music_card"
    const val PlayerContent = "music_player_content"
    const val ThumbnailColumn = "music_thumbnail_column"
    const val AlbumArt = "music_album_art"
    const val TrackTitle = "music_track_title"
    const val TrackArtist = "music_track_artist"
    const val Progress = "music_progress"
    const val ProgressBar = "music_progress_bar"
    const val ProgressTrack = "music_progress_track"
    const val Equalizer = "music_equalizer"
    const val PlayPause = "music_play_pause"
    const val Next = "music_next"
    const val Previous = "music_previous"
    const val VolumeSlider = "music_volume_slider"
    const val Idle = "music_idle"
    const val AccessAction = "music_access_action"
}

/**
 * Music page: the astronaut stage above the real media session and the device
 * volume pill on the right. The layout keeps that hierarchy in
 * landscape and stacks the stage, player, and pill cleanly in portrait.
 */
@Composable
fun MusicScreen(
    musicState: MusicUiState,
    controlState: CockpitUiState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
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
                    onSeek = onSeek,
                    onEnableMediaAccess = onEnableMediaAccess,
                    fillHeight = fillHeight,
                    modifier = cardModifier,
                )
            }

            if (landscape) {
                val playerMinHeight = maxHeight * 0.50f
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
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = playerMinHeight),
                                false,
                            )
                        }
                        VolumePill(
                            state = controlState,
                            onVolumeChange = onVolumeChange,
                            modifier = Modifier
                                .width(sliderColumnWidth)
                                .fillMaxHeight(),
                        )
                    }
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
                            .height(156.dp),
                    )
                    card(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        true,
                    )
                    VolumePill(
                        state = controlState,
                        onVolumeChange = onVolumeChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(228.dp),
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
    onSeek: (Long) -> Unit,
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
                    onSeek = onSeek,
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
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MusicTestTags.PlayerContent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(0.30f)
                .testTag(MusicTestTags.ThumbnailColumn),
            contentAlignment = Alignment.Center,
        ) {
            AlbumArtwork(
                art = track.albumArt,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
        }
        Column(
            modifier = Modifier
                .weight(0.70f)
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                }
                Spacer(modifier = Modifier.width(16.dp))
                EqualizerBars(
                    playing = track.isPlaying,
                    modifier = Modifier
                        .width(58.dp)
                        .height(24.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PlaybackProgress(
                positionMs = track.positionMs,
                durationMs = track.durationMs,
                canSeek = track.canSeek,
                onSeek = onSeek,
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    VibhorSpacing.xl,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransportButton(
                    iconRes = R.drawable.ic_skip_previous,
                    contentDescription = stringResource(R.string.music_previous),
                    enabled = track.canSkipPrevious,
                    onClick = onPrevious,
                    testTag = MusicTestTags.Previous,
                )
                PlayPauseButton(
                    playing = track.isPlaying,
                    enabled = track.canPlayPause,
                    onClick = onPlayPause,
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackProgress(
    positionMs: Long,
    durationMs: Long,
    canSeek: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = durationMs.coerceAtLeast(0L)
    val seekable = canSeek && duration > 0L
    val target = if (duration > 0L) {
        (positionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    var dragging by remember { mutableStateOf(false) }
    var pendingFraction by remember(durationMs) { mutableStateOf<Float?>(null) }
    // Snap straight to the dragged value; only animate ordinary position ticks.
    // This keeps the thumb under the finger and avoids a snap back to the old
    // spot when the session confirms the seek.
    val fraction by animateFloatAsState(
        targetValue = pendingFraction ?: target,
        animationSpec = if (pendingFraction != null) {
            snap()
        } else {
            tween(durationMillis = 1_000, easing = LinearEasing)
        },
        label = "playback-progress",
    )

    // Keep the chosen position on screen until the real session reports the
    // seeked position, so the thumb does not snap back between updates.
    LaunchedEffect(positionMs, pendingFraction, dragging) {
        val pending = pendingFraction ?: return@LaunchedEffect
        val pendingMs = (pending * duration).roundToLong()
        if (!dragging && abs(positionMs - pendingMs) <= SEEK_MATCH_WINDOW_MS) {
            pendingFraction = null
        }
    }
    LaunchedEffect(pendingFraction, dragging) {
        if (pendingFraction != null && !dragging) {
            delay(SEEK_TIMEOUT_MILLIS)
            if (!dragging) pendingFraction = null
        }
    }

    val shownPositionMs = if (dragging || pendingFraction != null) {
        (fraction * duration).roundToLong()
    } else {
        positionMs
    }
    val accent = VibhorThemeColors.accent
    val trackShape = RoundedCornerShape(999.dp)

    Column(modifier = modifier) {
        Slider(
            value = fraction,
            onValueChange = { value ->
                dragging = true
                pendingFraction = value
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(MusicTestTags.Progress),
            enabled = seekable,
            onValueChangeFinished = {
                val value = pendingFraction
                if (seekable && value != null) {
                    onSeek((value * duration).roundToLong())
                }
                dragging = false
            },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (seekable) accent else SecondaryText.copy(alpha = 0.45f),
                        )
                        .border(4.dp, DeepSlate, CircleShape),
                )
            },
            track = { sliderState ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(trackShape)
                            .background(PrimaryText.copy(alpha = 0.12f))
                            .testTag(MusicTestTags.ProgressTrack),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(sliderState.coercedValueAsFraction)
                            .height(24.dp)
                            .clip(trackShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (seekable) {
                                        listOf(accent, accent.copy(alpha = 0.62f))
                                    } else {
                                        listOf(
                                            SecondaryText.copy(alpha = 0.5f),
                                            SecondaryText.copy(alpha = 0.3f),
                                        )
                                    },
                                ),
                            )
                            .testTag(MusicTestTags.ProgressBar),
                    )
                }
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatPlaybackTime(shownPositionMs),
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText,
            )
            if (duration > 0L) {
                Text(
                    text = formatPlaybackTime(duration),
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
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(80.dp)
            .testTag(MusicTestTags.PlayPause),
        shape = RoundedCornerShape(24.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = VibhorThemeColors.accent,
            contentColor = VibhorThemeColors.accentOn,
            disabledContainerColor = SurfaceElevated,
            disabledContentColor = SecondaryText.copy(alpha = 0.55f),
        ),
    ) {
        Icon(
            painter = painterResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_media,
            ),
            contentDescription = stringResource(
                if (playing) R.string.music_pause else R.string.music_play,
            ),
            modifier = Modifier.size(48.dp),
        )
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
            .size(72.dp)
            .testTag(testTag),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = PrimaryText,
            disabledContentColor = SecondaryText.copy(alpha = 0.35f),
        ),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(44.dp),
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
private fun VolumePill(
    state: CockpitUiState,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = VibhorThemeColors.accent
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        VerticalPillSlider(
            value = state.volumeLevel,
            onValueChange = onVolumeChange,
            iconRes = R.drawable.ic_volume_up,
            label = stringResource(R.string.apps_volume),
            fillColors = listOf(accent, accent.copy(alpha = 0.58f)),
            testTag = MusicTestTags.VolumeSlider,
            modifier = Modifier
                .width(132.dp)
                .fillMaxHeight(),
        )
    }
}

private val EQ_TWO_PI = (2.0 * Math.PI).toFloat()
private const val SEEK_MATCH_WINDOW_MS = 1_000L
private const val SEEK_TIMEOUT_MILLIS = 3_000L

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
            onPlayPause = {},
            onNext = {},
            onPrevious = {},
            onSeek = {},
            onVolumeChange = {},
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
            onPlayPause = {},
            onNext = {},
            onPrevious = {},
            onSeek = {},
            onVolumeChange = {},
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
    canSeek = true,
)
