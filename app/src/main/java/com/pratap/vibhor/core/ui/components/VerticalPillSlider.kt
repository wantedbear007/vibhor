package com.pratap.vibhor.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SurfaceElevated
import kotlin.math.roundToInt

/**
 * Tall, fully rounded vertical slider used by the unified Apps rail and the
 * Music screen. The icon sits inside the pill at the top, the current
 * percentage near the bottom, and the track fills from the bottom with the
 * supplied gradient.
 */
@Composable
fun VerticalPillSlider(
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
                    .background(SurfaceElevated.copy(alpha = 0.72f))
                    .border(1.dp, PrimaryText.copy(alpha = 0.10f), trackShape)
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
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = fillColors,
                                startY = size.height - fillHeight,
                                endY = size.height,
                            ),
                            topLeft = Offset(0f, size.height - fillHeight),
                            size = Size(size.width, fillHeight),
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
                        text = stringResource(R.string.slider_value, percent),
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
