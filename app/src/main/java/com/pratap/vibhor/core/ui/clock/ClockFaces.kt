package com.pratap.vibhor.core.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * The four selectable Home clock faces. Each face is stateless and sizes
 * itself from one [baseSize] budget (the Home screen's computed clock size),
 * so the same composable renders full-size on Home and as a mini preview on
 * the Settings screen. [timeModifier]/[dateModifier] let the caller attach
 * test tags or semantics to the time and date elements.
 */
@Composable
fun ClassicClockFace(
    timeText: String,
    dateText: String,
    baseSize: TextUnit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    timeModifier: Modifier = Modifier,
    dateModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.displayLarge,
            fontSize = baseSize,
            color = PrimaryText,
            modifier = timeModifier,
        )
        if (showDate) {
            Text(
                text = dateText,
                fontSize = baseSize * 0.12f,
                fontWeight = FontWeight.Medium,
                color = SecondaryText,
                modifier = dateModifier,
            )
        }
    }
}

@Composable
fun SplitClockFace(
    hourText: String,
    minuteText: String,
    meridiemText: String,
    dateText: String,
    baseSize: TextUnit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    timeModifier: Modifier = Modifier,
    dateModifier: Modifier = Modifier,
) {
    val lineSize = baseSize * 0.5f
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xs),
    ) {
        Text(
            text = hourText,
            style = MaterialTheme.typography.displayLarge,
            fontSize = lineSize,
            color = PrimaryText,
            textAlign = TextAlign.Center,
            modifier = timeModifier,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = minuteText,
                style = MaterialTheme.typography.displayLarge,
                fontSize = lineSize,
                color = PrimaryText,
            )
            if (meridiemText.isNotEmpty()) {
                Text(
                    text = meridiemText,
                    fontSize = baseSize * 0.16f,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryText,
                    modifier = Modifier.padding(
                        start = VibhorSpacing.sm,
                        bottom = VibhorSpacing.xs,
                    ),
                )
            }
        }
        if (showDate) {
            Text(
                text = dateText,
                fontSize = baseSize * 0.12f,
                fontWeight = FontWeight.Medium,
                color = SecondaryText,
                modifier = dateModifier,
            )
        }
    }
}

@Composable
fun MinimalClockFace(
    timeText: String,
    baseSize: TextUnit,
    modifier: Modifier = Modifier,
    timeModifier: Modifier = Modifier,
) {
    Text(
        text = timeText,
        style = MaterialTheme.typography.displayLarge,
        fontSize = baseSize * 0.6f,
        fontWeight = FontWeight.Thin,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = baseSize * 0.03f,
        color = PrimaryText,
        modifier = modifier.then(timeModifier),
    )
}

/**
 * Bold modern digital clock: heavy sans-serif time with a small uppercase
 * date underneath. Reads clearly at a glance on a car display.
 */
@Composable
fun BoldClockFace(
    timeText: String,
    dateText: String,
    baseSize: TextUnit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    timeModifier: Modifier = Modifier,
    dateModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        Text(
            text = timeText,
            fontSize = baseSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = baseSize * (-0.02f),
            color = PrimaryText,
            modifier = timeModifier,
        )
        if (showDate) {
            Text(
                text = dateText.uppercase(),
                fontSize = baseSize * 0.13f,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = baseSize * 0.04f,
                color = VibhorThemeColors.accent,
                modifier = dateModifier,
            )
        }
    }
}

/**
 * Elegant split-line clock: hour and minute separated by a thin accent
 * divider on a single baseline, with a smaller, lighter date beneath.
 */
@Composable
fun ElegantClockFace(
    hourText: String,
    minuteText: String,
    meridiemText: String,
    dateText: String,
    baseSize: TextUnit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    timeModifier: Modifier = Modifier,
    dateModifier: Modifier = Modifier,
) {
    val timeSize = baseSize * 0.72f
    val dividerHeight = with(LocalDensity.current) { (timeSize * 0.5f).toDp() }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = hourText,
                fontSize = timeSize,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                color = PrimaryText,
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = (baseSize.value * 0.05f).dp)
                    .size(width = 2.dp, height = (timeSize.value * 0.5f).dp)
                    .background(VibhorThemeColors.accent),
            )
            Text(
                text = minuteText,
                fontSize = timeSize,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                color = PrimaryText,
                modifier = timeModifier,
            )
            if (meridiemText.isNotEmpty()) {
                Text(
                    text = meridiemText,
                    fontSize = baseSize * 0.16f,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryText,
                    modifier = Modifier.padding(start = VibhorSpacing.sm),
                )
            }
        }
        if (showDate) {
            Text(
                text = dateText,
                fontSize = baseSize * 0.11f,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                letterSpacing = baseSize * 0.02f,
                color = SecondaryText,
                modifier = dateModifier,
            )
        }
    }
}

/**
 * Canvas-drawn analog face. The second hand keeps real time while the face
 * is composed (a passive UI animation cancelled when Home leaves the
 * screen); hour and minute come from launcher state and update per minute.
 */
@Composable
fun AnalogClockFace(
    hour: Int,
    minute: Int,
    second: Int,
    dateText: String,
    baseSize: TextUnit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    timeModifier: Modifier = Modifier,
    dateModifier: Modifier = Modifier,
) {
    var liveSecond by remember { mutableIntStateOf(second) }
    LaunchedEffect(hour, minute) {
        while (true) {
            liveSecond = LocalTime.now().second
            delay(1_000L - (System.currentTimeMillis() % 1_000L))
        }
    }
    val diameter = with(LocalDensity.current) { (baseSize * 1.1f).toDp() }
    val accent = VibhorThemeColors.accent
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
    ) {
        Canvas(
            modifier = Modifier
                .size(diameter)
                .then(timeModifier),
        ) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            for (tick in 0 until 60) {
                val angle = Math.toRadians(tick * 6.0)
                val major = tick % 5 == 0
                val dx = sin(angle).toFloat()
                val dy = -cos(angle).toFloat()
                val outer = radius * 0.95f
                val inner = if (major) radius * 0.80f else radius * 0.88f
                drawLine(
                    color = if (major) PrimaryText else SecondaryText,
                    start = center + Offset(dx * inner, dy * inner),
                    end = center + Offset(dx * outer, dy * outer),
                    strokeWidth = if (major) radius * 0.045f else radius * 0.015f,
                    cap = StrokeCap.Round,
                )
            }

            fun hand(angleDegrees: Float, length: Float, width: Float, color: Color) {
                val angle = Math.toRadians(angleDegrees.toDouble())
                val tip = center + Offset(
                    sin(angle).toFloat() * radius * length,
                    -cos(angle).toFloat() * radius * length,
                )
                drawLine(
                    color = color,
                    start = center,
                    end = tip,
                    strokeWidth = radius * width,
                    cap = StrokeCap.Round,
                )
            }

            hand(
                angleDegrees = (hour % 12) * 30f + minute * 0.5f,
                length = 0.45f,
                width = 0.09f,
                color = PrimaryText,
            )
            hand(
                angleDegrees = minute * 6f + liveSecond * 0.1f,
                length = 0.70f,
                width = 0.06f,
                color = PrimaryText,
            )
            hand(
                angleDegrees = liveSecond * 6f,
                length = 0.78f,
                width = 0.022f,
                color = accent,
            )
            drawCircle(color = accent, radius = radius * 0.05f, center = center)
        }
        if (showDate) {
            Text(
                text = dateText,
                fontSize = baseSize * 0.12f,
                fontWeight = FontWeight.Medium,
                color = SecondaryText,
                modifier = dateModifier,
            )
        }
    }
}

private operator fun Offset.plus(other: Offset): Offset =
    Offset(x + other.x, y + other.y)
