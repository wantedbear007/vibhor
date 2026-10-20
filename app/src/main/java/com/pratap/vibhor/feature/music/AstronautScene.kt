package com.pratap.vibhor.feature.music

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

private val MoonLight = Color(0xFFE9EDF4)
private val MoonShadow = Color(0xFFA8B2C0)
private val Crater = Color(0xFF8C97A8)
private val Suit = Color(0xFFF2F6FB)
private val SuitShade = Color(0xFFC9D2DE)
private val Backpack = Color(0xFF6C7787)
private val VisorTop = Color(0xFF0B1626)
private val VisorBottom = Color(0xFF1D3A5C)
private val HeadphonePink = Color(0xFFF78DBF)

private data class Star(val x: Float, val y: Float, val radius: Float, val phase: Float)

private val Stars = listOf(
    Star(0.08f, 0.18f, 0.0060f, 0.2f),
    Star(0.16f, 0.34f, 0.0045f, 1.7f),
    Star(0.23f, 0.10f, 0.0050f, 3.1f),
    Star(0.31f, 0.27f, 0.0038f, 4.6f),
    Star(0.38f, 0.07f, 0.0044f, 5.3f),
    Star(0.46f, 0.21f, 0.0034f, 2.4f),
    Star(0.55f, 0.09f, 0.0052f, 0.9f),
    Star(0.63f, 0.24f, 0.0040f, 4.0f),
    Star(0.71f, 0.12f, 0.0056f, 2.9f),
    Star(0.79f, 0.30f, 0.0042f, 5.8f),
    Star(0.86f, 0.16f, 0.0050f, 1.2f),
    Star(0.93f, 0.35f, 0.0036f, 3.7f),
    Star(0.12f, 0.48f, 0.0038f, 4.9f),
    Star(0.88f, 0.50f, 0.0044f, 0.6f),
    Star(0.68f, 0.44f, 0.0032f, 2.1f),
    Star(0.27f, 0.44f, 0.0030f, 5.5f),
)

private val Sparkles = listOf(
    Star(0.19f, 0.24f, 0.020f, 0.8f),
    Star(0.77f, 0.18f, 0.024f, 3.4f),
    Star(0.60f, 0.33f, 0.015f, 5.1f),
)

/**
 * Hand-drawn astronaut sitting on a moon, wearing headphones. All motion is
 * derived from infinite transitions that are only read inside the draw pass
 * while [playing] is true, so a paused page performs no rendering work and
 * the illustration itself stays a cheap Canvas layer.
 */
@Composable
fun AstronautScene(playing: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "astronaut")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )
    val nod by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nod",
    )
    val tap by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "tap",
    )
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "twinkle",
    )
    val glow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )
    val accent = VibhorThemeColors.accent

    Canvas(modifier = modifier.clipToBounds().testTag(MusicTestTags.Astronaut)) {
        val side = min(size.width, size.height * 1.02f)
        val left = (size.width - side) / 2f
        val top = size.height - side
        fun nx(fraction: Float) = left + fraction * side
        fun ny(fraction: Float) = top + fraction * side
        fun n(fraction: Float) = fraction * side

        // Ambient stage lighting. Two soft radial washes, never a blur pass.
        val pulse = if (playing) glow else 0f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = 0.11f + 0.05f * pulse),
                    Color.Transparent,
                ),
                center = Offset(nx(0.5f), ny(0.46f)),
                radius = n(0.56f),
            ),
            radius = n(0.56f),
            center = Offset(nx(0.5f), ny(0.46f)),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    HeadphonePink.copy(alpha = 0.06f + 0.04f * pulse),
                    Color.Transparent,
                ),
                center = Offset(nx(0.34f), ny(0.62f)),
                radius = n(0.42f),
            ),
            radius = n(0.42f),
            center = Offset(nx(0.34f), ny(0.62f)),
        )

        // Stars and sparkles span the whole column, not just the stage square,
        // so tall layouts keep a populated sky above the astronaut.
        val starRadius = min(size.width, size.height) * 0.012f
        Stars.forEach { star ->
            val alpha = if (playing) {
                0.30f + 0.50f * ((sin(twinkle * TWO_PI + star.phase) + 1f) / 2f)
            } else {
                0.45f
            }
            drawCircle(
                color = PrimaryText.copy(alpha = alpha),
                radius = starRadius * (star.radius / 0.0045f),
                center = Offset(size.width * star.x, size.height * star.y),
            )
        }
        Sparkles.forEach { star ->
            val alpha = if (playing) {
                0.26f + 0.34f * ((sin(twinkle * TWO_PI + star.phase) + 1f) / 2f)
            } else {
                0.30f
            }
            sparkle(
                center = Offset(size.width * star.x, size.height * star.y),
                radius = starRadius * (star.radius / 0.0045f),
                color = PrimaryText.copy(alpha = alpha),
            )
        }

        // Moon.
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(MoonLight, MoonShadow),
                startY = ny(0.66f),
                endY = ny(1.06f),
            ),
            topLeft = Offset(nx(0.5f) - n(0.80f), ny(1.06f) - n(0.40f)),
            size = Size(n(1.60f), n(0.80f)),
        )
        drawOval(
            color = MoonLight.copy(alpha = 0.55f),
            topLeft = Offset(nx(0.16f), ny(0.78f)),
            size = Size(n(0.30f), n(0.055f)),
        )
        drawOval(
            color = Crater.copy(alpha = 0.30f),
            topLeft = Offset(nx(0.20f), ny(0.86f)),
            size = Size(n(0.11f), n(0.040f)),
        )
        drawOval(
            color = Crater.copy(alpha = 0.26f),
            topLeft = Offset(nx(0.72f), ny(0.92f)),
            size = Size(n(0.08f), n(0.028f)),
        )
        drawOval(
            color = Crater.copy(alpha = 0.20f),
            topLeft = Offset(nx(0.62f), ny(0.80f)),
            size = Size(n(0.06f), n(0.020f)),
        )

        // Contact shadow stays on the moon while the body bobs above it.
        drawOval(
            color = Color.Black.copy(alpha = 0.18f),
            topLeft = Offset(nx(0.5f) - n(0.19f), ny(0.672f)),
            size = Size(n(0.38f), n(0.040f)),
        )

        val bobOffset = if (playing) (bob - 0.5f) * 2f * n(0.012f) else 0f
        val nodAngle = if (playing) (nod - 0.5f) * 2f * 3.2f else 0f
        val tapAngle = if (playing) sin(tap * TWO_PI) * 9f else 0f

        translate(top = bobOffset) {
            // Feet stay at the moon contact point; the figure is drawn at
            // 1.32x so it stays the clear focal point of the stage.
            drawAstronaut(
                centerX = nx(0.5f),
                centerY = ny(0.469f),
                unit = n(1.32f),
                accent = accent,
                nodAngle = nodAngle,
                tapAngle = tapAngle,
            )
        }
    }
}

private fun DrawScope.drawAstronaut(
    centerX: Float,
    centerY: Float,
    unit: Float,
    accent: Color,
    nodAngle: Float,
    tapAngle: Float,
) {
    fun u(fraction: Float) = fraction * unit

    // Backpack peeking out behind the suit.
    drawRoundRect(
        color = Backpack,
        topLeft = Offset(centerX - u(0.050f), centerY - u(0.098f)),
        size = Size(u(0.168f), u(0.190f)),
        cornerRadius = CornerRadius(u(0.050f)),
    )

    // Legs.
    drawLine(
        color = Suit,
        start = Offset(centerX - u(0.050f), centerY + u(0.088f)),
        end = Offset(centerX - u(0.062f), centerY + u(0.125f)),
        strokeWidth = u(0.046f),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Suit,
        start = Offset(centerX + u(0.050f), centerY + u(0.088f)),
        end = Offset(centerX + u(0.062f), centerY + u(0.125f)),
        strokeWidth = u(0.046f),
        cap = StrokeCap.Round,
    )

    // Feet tap around the ankle while music plays.
    drawFoot(centerX - u(0.062f), centerY + u(0.125f), u(1f), u(0.075f), u(0.034f), tapAngle)
    drawFoot(centerX + u(0.062f), centerY + u(0.125f), u(1f), u(0.075f), u(0.034f), -tapAngle * 0.7f)

    // Torso.
    drawRoundRect(
        color = Suit,
        topLeft = Offset(centerX - u(0.095f), centerY - u(0.100f)),
        size = Size(u(0.190f), u(0.200f)),
        cornerRadius = CornerRadius(u(0.068f)),
    )
    drawRoundRect(
        color = SuitShade.copy(alpha = 0.55f),
        topLeft = Offset(centerX - u(0.095f), centerY - u(0.100f)),
        size = Size(u(0.190f), u(0.200f)),
        cornerRadius = CornerRadius(u(0.068f)),
        style = Stroke(width = u(0.0035f)),
    )
    // Chest control panel.
    drawRoundRect(
        color = SuitShade,
        topLeft = Offset(centerX - u(0.046f), centerY + u(0.002f)),
        size = Size(u(0.092f), u(0.046f)),
        cornerRadius = CornerRadius(u(0.016f)),
    )
    drawCircle(
        color = accent,
        radius = u(0.010f),
        center = Offset(centerX - u(0.022f), centerY + u(0.025f)),
    )
    drawCircle(
        color = HeadphonePink.copy(alpha = 0.9f),
        radius = u(0.006f),
        center = Offset(centerX + u(0.020f), centerY + u(0.025f)),
    )

    // Arms resting on the knees.
    drawLine(
        color = Suit,
        start = Offset(centerX - u(0.080f), centerY - u(0.055f)),
        end = Offset(centerX - u(0.128f), centerY + u(0.040f)),
        strokeWidth = u(0.043f),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Suit,
        start = Offset(centerX + u(0.080f), centerY - u(0.055f)),
        end = Offset(centerX + u(0.128f), centerY + u(0.040f)),
        strokeWidth = u(0.043f),
        cap = StrokeCap.Round,
    )
    drawCircle(Suit, radius = u(0.025f), center = Offset(centerX - u(0.128f), centerY + u(0.042f)))
    drawCircle(Suit, radius = u(0.025f), center = Offset(centerX + u(0.128f), centerY + u(0.042f)))

    // Helmet nods gently around the neck.
    rotate(degrees = nodAngle, pivot = Offset(centerX, centerY - u(0.090f))) {
        val helmetCenter = Offset(centerX, centerY - u(0.185f))
        drawCircle(color = Suit, radius = u(0.116f), center = helmetCenter)
        drawCircle(
            color = SuitShade,
            radius = u(0.116f),
            center = helmetCenter,
            style = Stroke(width = u(0.0035f)),
        )
        // Visor.
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(VisorTop, VisorBottom),
                start = Offset(centerX - u(0.085f), centerY - u(0.225f)),
                end = Offset(centerX + u(0.085f), centerY - u(0.115f)),
            ),
            topLeft = Offset(centerX - u(0.085f), centerY - u(0.225f)),
            size = Size(u(0.170f), u(0.106f)),
            cornerRadius = CornerRadius(u(0.053f)),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.16f),
            radius = u(0.026f),
            center = Offset(centerX - u(0.038f), centerY - u(0.194f)),
        )
        drawCircle(
            color = accent.copy(alpha = 0.55f),
            radius = u(0.007f),
            center = Offset(centerX - u(0.028f), centerY - u(0.170f)),
        )
        drawCircle(
            color = accent.copy(alpha = 0.55f),
            radius = u(0.007f),
            center = Offset(centerX + u(0.016f), centerY - u(0.172f)),
        )

        // Headphones: band over the helmet plus two ear cups.
        drawArc(
            color = accent,
            startAngle = 195f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(centerX - u(0.137f), centerY - u(0.312f)),
            size = Size(u(0.274f), u(0.274f)),
            style = Stroke(width = u(0.020f), cap = StrokeCap.Round),
        )
        drawEarCup(centerX - u(0.152f), centerY - u(0.230f), u(1f), accent)
        drawEarCup(centerX + u(0.102f), centerY - u(0.230f), u(1f), accent)
    }
}

private fun DrawScope.drawFoot(
    ankleX: Float,
    ankleY: Float,
    unit: Float,
    width: Float,
    height: Float,
    angle: Float,
) {
    rotate(degrees = angle, pivot = Offset(ankleX, ankleY)) {
        drawRoundRect(
            color = Suit,
            topLeft = Offset(ankleX - width / 2f, ankleY),
            size = Size(width, height),
            cornerRadius = CornerRadius(height / 2f),
        )
        drawRoundRect(
            color = SuitShade,
            topLeft = Offset(ankleX - width / 2f, ankleY + height * 0.62f),
            size = Size(width, height * 0.30f),
            cornerRadius = CornerRadius(height * 0.15f),
        )
    }
}

private fun DrawScope.drawEarCup(
    left: Float,
    top: Float,
    unit: Float,
    accent: Color,
) {
    drawRoundRect(
        color = accent,
        topLeft = Offset(left, top),
        size = Size(unit * 0.050f, unit * 0.090f),
        cornerRadius = CornerRadius(unit * 0.024f),
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.28f),
        topLeft = Offset(left + unit * 0.010f, top + unit * 0.014f),
        size = Size(unit * 0.030f, unit * 0.062f),
        cornerRadius = CornerRadius(unit * 0.015f),
    )
}

private fun DrawScope.sparkle(center: Offset, radius: Float, color: Color) {
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = radius * 0.22f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = radius * 0.22f,
        cap = StrokeCap.Round,
    )
}

private val TWO_PI = (2.0 * PI).toFloat()
