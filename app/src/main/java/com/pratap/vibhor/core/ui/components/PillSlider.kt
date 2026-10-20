package com.pratap.vibhor.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors

private val PillHeight = 48.dp

/**
 * Horizontal pill slider styled exactly like the Cockpit volume and
 * brightness controls: a thick fully rounded track, a translucent fill and an
 * accent-filled value capsule. There is no separate thumb; the capsule end is
 * the value indicator. Dragging or tapping anywhere on the track changes the
 * value; [steps] follows Material's convention (number of stops *between* the
 * endpoints).
 *
 * Used for every slider in Settings so all launcher controls share one look.
 */
@Composable
fun PillSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    onValueChangeFinished: () -> Unit = {},
) {
    val accent = VibhorThemeColors.accent
    val snap: (Float) -> Float = { raw -> snapToSteps(raw, valueRange, steps) }
    val fraction = ((value - valueRange.start) /
        (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    val fillFraction by animateFloatAsState(
        targetValue = fraction,
        label = "pillFill",
    )
    val fillColor = if (enabled) accent.copy(alpha = 0.85f) else SecondaryText
    val currentValue = snap(value)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PillHeight)
            .clip(RoundedCornerShape(percent = 50))
            .background(PrimaryText.copy(alpha = 0.12f))
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = currentValue,
                    range = valueRange,
                    steps = steps,
                )
                if (!enabled) disabled()
                setProgress { target ->
                    if (!enabled) {
                        false
                    } else {
                        onValueChange(snap(target))
                        onValueChangeFinished()
                        true
                    }
                }
            }
            .then(
                if (enabled) {
                    Modifier
                        .pointerInput(valueRange, steps) {
                            detectTapGestures { offset ->
                                onValueChange(
                                    snap(
                                        valueRange.start +
                                            (offset.x / size.width) *
                                            (valueRange.endInclusive - valueRange.start),
                                    ),
                                )
                                onValueChangeFinished()
                            }
                        }
                        .pointerInput(valueRange, steps) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    onValueChange(
                                        snap(
                                            valueRange.start +
                                                (offset.x / size.width) *
                                                (valueRange.endInclusive - valueRange.start),
                                        ),
                                    )
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    onValueChange(
                                        snap(
                                            valueRange.start +
                                                (change.position.x / size.width) *
                                                (valueRange.endInclusive - valueRange.start),
                                        ),
                                    )
                                },
                                onDragEnd = { onValueChangeFinished() },
                                onDragCancel = { onValueChangeFinished() },
                            )
                        }
                } else {
                    Modifier
                },
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillWidth = size.width * fillFraction
            if (fillWidth > 0f) {
                drawRoundRect(
                    color = fillColor,
                    size = Size(fillWidth, size.height),
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                )
            }
        }
        // Subtle top highlight, matching the Cockpit sliders.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(PrimaryText.copy(alpha = 0.18f)),
        )
    }
}
